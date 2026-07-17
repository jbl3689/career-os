package com.careeros.api.auth;

import java.time.Clock;
import java.time.Instant;

import com.careeros.api.auth.persistence.UserEntity;
import com.careeros.api.auth.persistence.UserRepository;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserService {

	private final UserRepository userRepository;
	private final Clock clock;

	public CurrentUserService(UserRepository userRepository, Clock clock) {
		this.userRepository = userRepository;
		this.clock = clock;
	}

	@Transactional
	public UserEntity resolve(OidcUser oidcUser) {
		String subject = oidcUser.getSubject();
		String email = oidcUser.getEmail();
		String displayName = oidcUser.getFullName();
		String avatarUrl = oidcUser.getPicture();
		Instant now = Instant.now(clock);

		return userRepository.findByGoogleSubject(subject)
				.map(user -> {
					user.updateProfile(email, displayName, avatarUrl, now);
					return user;
				})
				.orElseGet(() -> userRepository.save(new UserEntity(
						subject,
						email,
						displayName,
						avatarUrl,
						now)));
	}
}
