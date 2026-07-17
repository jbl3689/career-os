package com.careeros.api.gmail;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.careeros.api.auth.google.GoogleAccessTokenClient;
import com.careeros.api.auth.google.GoogleAccessTokenException;
import com.careeros.api.auth.google.GoogleConnectionService;
import com.careeros.api.auth.persistence.UserEntity;

import org.springframework.stereotype.Service;

@Service
public class GmailScanService {

	static final String CANDIDATE_QUERY =
			"newer_than:1y "
			+ "(subject:application OR subject:interview OR subject:assessment "
			+ "OR subject:offer OR subject:rejection OR subject:recruiter "
			+ "OR \"thanks for applying\" OR \"thank you for applying\")";
	static final int MAXIMUM_RESULTS = 10;

	private final GoogleConnectionService connectionService;
	private final GoogleAccessTokenClient accessTokenClient;
	private final GmailClient gmailClient;
	private final GmailScanPersistenceService persistenceService;
	private final Clock clock;

	public GmailScanService(
			GoogleConnectionService connectionService,
			GoogleAccessTokenClient accessTokenClient,
			GmailClient gmailClient,
			GmailScanPersistenceService persistenceService,
			Clock clock) {
		this.connectionService = connectionService;
		this.accessTokenClient = accessTokenClient;
		this.gmailClient = gmailClient;
		this.persistenceService = persistenceService;
		this.clock = clock;
	}

	public GmailScanResponse scan(UserEntity user) {
		String refreshToken = connectionService.requireRefreshToken(user);

		try {
			String accessToken = accessTokenClient.refreshAccessToken(refreshToken);
			List<GmailMessageMetadata> candidates = gmailClient.findCandidateMessages(
					accessToken,
					CANDIDATE_QUERY,
					MAXIMUM_RESULTS);
			Instant scannedAt = Instant.now(clock);
			List<GmailCandidateResponse> persistedCandidates =
					persistenceService.persistCandidates(user, candidates, scannedAt);
			return new GmailScanResponse(
					scannedAt,
					persistedCandidates.size(),
					(int) persistedCandidates.stream()
							.filter(GmailCandidateResponse::newlyDiscovered)
							.count(),
					persistedCandidates);
		}
		catch (GoogleAccessTokenException exception) {
			throw new GmailScanFailedException(
					"Gmail access expired. Disconnect and reconnect Gmail, then try again.",
					exception);
		}
		catch (GmailClientException exception) {
			throw new GmailScanFailedException(
					exception.getMessage(),
					exception);
		}
	}
}
