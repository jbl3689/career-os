package com.careeros.api.gmail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Set;

import com.careeros.api.PostgresIntegrationTest;
import com.careeros.api.auth.google.GoogleConnectionRepository;
import com.careeros.api.auth.google.GoogleConnectionService;
import com.careeros.api.auth.persistence.UserEntity;
import com.careeros.api.auth.persistence.UserRepository;
import com.careeros.api.gmail.persistence.EmailMessageRepository;
import com.careeros.api.gmail.persistence.GmailScanResultRepository;
import com.careeros.api.gmail.persistence.GmailScanResultStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import(GmailScanTestConfiguration.class)
class GmailScanControllerTests extends PostgresIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private GoogleConnectionService connectionService;

	@Autowired
	private GoogleConnectionRepository connectionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EmailMessageRepository emailMessageRepository;

	@Autowired
	private GmailScanResultRepository scanResultRepository;

	@Autowired
	private GmailScanTestConfiguration.RecordingGoogleAccessTokenClient accessTokenClient;

	@Autowired
	private GmailScanTestConfiguration.RecordingGmailClient gmailClient;

	@Autowired
	private GmailScanTestConfiguration.RecordingGmailMessageClassifier classifier;

	@BeforeEach
	void clearDatabase() {
		scanResultRepository.deleteAll();
		emailMessageRepository.deleteAll();
		connectionRepository.deleteAll();
		userRepository.deleteAll();
		accessTokenClient.reset();
		gmailClient.reset();
		classifier.reset();
	}

	@Test
	void scansConnectedGmailAndReturnsOnlyCandidateMetadata() throws Exception {
		UserEntity user = saveUser();
		connectGmail(user);

		mockMvc.perform(post("/api/v1/gmail/scan")
						.with(authenticatedUser())
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.scannedAt").value("2026-07-20T10:00:00Z"))
				.andExpect(jsonPath("$.candidatesFound").value(1))
				.andExpect(jsonPath("$.newCandidatesFound").value(1))
				.andExpect(jsonPath("$.candidates[0].gmailMessageId").value("message-1"))
				.andExpect(jsonPath("$.candidates[0].gmailThreadId").value("thread-1"))
				.andExpect(jsonPath("$.candidates[0].sender")
						.value("Recruiter <recruiter@example.com>"))
				.andExpect(jsonPath("$.candidates[0].subject").value("Interview invitation"))
				.andExpect(jsonPath("$.candidates[0].receivedAt")
						.value("2026-07-19T14:30:00Z"))
				.andExpect(jsonPath("$.candidates[0].newlyDiscovered").value(true))
				.andExpect(jsonPath("$.candidates[0].classification").value("JOB_RELATED"))
				.andExpect(jsonPath("$.candidates[0].eventType").value("INTERVIEW"))
				.andExpect(jsonPath("$.candidates[0].confidenceScore").isNumber())
				.andExpect(jsonPath("$.candidates[0].classificationReason")
						.value("Interview terminology was found"))
				.andExpect(jsonPath("$.candidates[0].body").doesNotExist())
				.andExpect(jsonPath("$.accessToken").doesNotExist())
				.andExpect(jsonPath("$.refreshToken").doesNotExist());

		assertThat(accessTokenClient.getReceivedRefreshToken()).isEqualTo("private-refresh-token");
		assertThat(gmailClient.getReceivedAccessToken()).isEqualTo("temporary-access-token");
		assertThat(gmailClient.getReceivedQuery())
				.contains("newer_than:1y")
				.contains("subject:interview");
		assertThat(gmailClient.getReceivedMaximumResults()).isEqualTo(10);
		assertThat(emailMessageRepository.count()).isEqualTo(1);
		assertThat(scanResultRepository.findAll())
				.singleElement()
				.satisfies(result -> {
					assertThat(result.getStatus())
							.isEqualTo(GmailScanResultStatus.READY_FOR_MATCHING);
					assertThat(result.getClassification())
							.isEqualTo(GmailClassificationCategory.JOB_RELATED);
					assertThat(result.getEventType())
							.isEqualTo(GmailEventType.INTERVIEW);
				});
	}

	@Test
	void rescanningTheSameGmailMessageDoesNotCreateDuplicates() throws Exception {
		UserEntity user = saveUser();
		connectGmail(user);

		mockMvc.perform(post("/api/v1/gmail/scan")
						.with(authenticatedUser())
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.newCandidatesFound").value(1))
				.andExpect(jsonPath("$.candidates[0].newlyDiscovered").value(true));

		mockMvc.perform(post("/api/v1/gmail/scan")
						.with(authenticatedUser())
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.candidatesFound").value(1))
				.andExpect(jsonPath("$.newCandidatesFound").value(0))
				.andExpect(jsonPath("$.candidates[0].newlyDiscovered").value(false));

		assertThat(emailMessageRepository.count()).isEqualTo(1);
		assertThat(scanResultRepository.count()).isEqualTo(1);
		assertThat(classifier.getInvocationCount()).isEqualTo(1);
	}

	@Test
	void requiresAGmailConnection() throws Exception {
		saveUser();

		mockMvc.perform(post("/api/v1/gmail/scan")
						.with(authenticatedUser())
						.with(csrf()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("Connect Gmail before starting a scan"));
	}

	@Test
	void returnsAnUnderstandableErrorWhenTheGrantHasExpired() throws Exception {
		UserEntity user = saveUser();
		connectGmail(user);
		accessTokenClient.failNextRequest();

		mockMvc.perform(post("/api/v1/gmail/scan")
						.with(authenticatedUser())
						.with(csrf()))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.error").value(
						"Gmail access expired. Disconnect and reconnect Gmail, then try again."));
	}

	@Test
	void returnsTheSafeGmailApiErrorWhenGoogleRejectsTheScan() throws Exception {
		UserEntity user = saveUser();
		connectGmail(user);
		gmailClient.failNextRequest();

		mockMvc.perform(post("/api/v1/gmail/scan")
						.with(authenticatedUser())
						.with(csrf()))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.error").value(
						"Google denied the Gmail scan. Confirm that the Gmail API is enabled in the same Google Cloud project."));
	}

	@Test
	void protectsScansWithCsrf() throws Exception {
		mockMvc.perform(post("/api/v1/gmail/scan").with(authenticatedUser()))
				.andExpect(status().isForbidden());
	}

	@Test
	void requiresAnAuthenticatedSession() throws Exception {
		mockMvc.perform(post("/api/v1/gmail/scan").with(csrf()))
				.andExpect(status().isUnauthorized());
	}

	private UserEntity saveUser() {
		return userRepository.save(new UserEntity(
				"google-subject",
				"developer@example.com",
				"Career OS Developer",
				null,
				Instant.parse("2026-07-20T10:00:00Z")));
	}

	private void connectGmail(UserEntity user) {
		connectionService.connect(
				user,
				"google-subject",
				"developer@example.com",
				"private-refresh-token",
				Set.of("https://www.googleapis.com/auth/gmail.readonly"));
	}

	private org.springframework.test.web.servlet.request.RequestPostProcessor authenticatedUser() {
		return oidcLogin().idToken(token -> token
				.subject("google-subject")
				.claim("email", "developer@example.com")
				.claim("name", "Career OS Developer"));
	}
}
