package com.careeros.api.auth.google;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;

import com.careeros.api.auth.persistence.UserEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleConnectionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleConnectionService.class);

	private final GoogleConnectionRepository connectionRepository;
	private final TokenEncryptionService tokenEncryptionService;
	private final GoogleTokenRevocationClient tokenRevocationClient;
	private final Clock clock;

	public GoogleConnectionService(
			GoogleConnectionRepository connectionRepository,
			TokenEncryptionService tokenEncryptionService,
			GoogleTokenRevocationClient tokenRevocationClient,
			Clock clock) {
		this.connectionRepository = connectionRepository;
		this.tokenEncryptionService = tokenEncryptionService;
		this.tokenRevocationClient = tokenRevocationClient;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public GoogleConnectionResponse getConnection(UserEntity user) {
		return connectionRepository.findByUserId(user.getId())
				.map(GoogleConnectionResponse::from)
				.orElseGet(GoogleConnectionResponse::disconnected);
	}

	@Transactional(readOnly = true)
	public String requireRefreshToken(UserEntity user) {
		GoogleConnectionEntity connection = connectionRepository.findByUserId(user.getId())
				.orElseThrow(GmailConnectionRequiredException::new);
		return tokenEncryptionService.decrypt(
				connection.getEncryptedRefreshToken(),
				user.getId());
	}

	@Transactional
	public void connect(
			UserEntity user,
			String googleSubject,
			String gmailAddress,
			String refreshToken,
			Set<String> grantedScopes) {
		if (!user.getGoogleSubject().equals(googleSubject)) {
			throw new IllegalArgumentException("The Gmail account must match the signed-in Google account");
		}

		Instant now = Instant.now(clock);
		String encryptedToken = tokenEncryptionService.encrypt(refreshToken, user.getId());
		String scopes = String.join(" ", new TreeSet<>(grantedScopes));

		connectionRepository.findByUserId(user.getId())
				.ifPresentOrElse(
						connection -> connection.reconnect(gmailAddress, encryptedToken, scopes, now),
						() -> connectionRepository.save(new GoogleConnectionEntity(
								user,
								googleSubject,
								gmailAddress,
								encryptedToken,
								scopes,
								now)));
	}

	@Transactional
	public void disconnect(UserEntity user) {
		connectionRepository.findByUserId(user.getId()).ifPresent(connection -> {
			try {
				String refreshToken = tokenEncryptionService.decrypt(
						connection.getEncryptedRefreshToken(),
						user.getId());
				tokenRevocationClient.revoke(refreshToken);
			}
			catch (RuntimeException exception) {
				LOGGER.warn(
						"Google token revocation failed for user {}; removing the local Gmail connection",
						user.getId());
			}
			connectionRepository.delete(connection);
		});
	}
}
