package com.careeros.api.auth.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Set;

import com.careeros.api.PostgresIntegrationTest;
import com.careeros.api.application.persistence.CompanyRepository;
import com.careeros.api.application.persistence.JobApplicationRepository;
import com.careeros.api.application.persistence.JobEventRepository;
import com.careeros.api.auth.persistence.UserEntity;
import com.careeros.api.auth.persistence.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import(GoogleConnectionTestConfiguration.class)
class GoogleConnectionControllerTests extends PostgresIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private GoogleConnectionService connectionService;

	@Autowired
	private GoogleConnectionRepository connectionRepository;

	@Autowired
	private JobEventRepository eventRepository;

	@Autowired
	private JobApplicationRepository applicationRepository;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private GoogleConnectionTestConfiguration.RecordingGoogleTokenRevocationClient
			tokenRevocationClient;

	@BeforeEach
	void clearDatabase() {
		eventRepository.deleteAll();
		applicationRepository.deleteAll();
		companyRepository.deleteAll();
		connectionRepository.deleteAll();
		userRepository.deleteAll();
		tokenRevocationClient.reset();
	}

	@Test
	void returnsDisconnectedWhenTheUserHasNotConnectedGmail() throws Exception {
		mockMvc.perform(get("/api/v1/google-connection").with(authenticatedUser()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.connected").value(false))
				.andExpect(jsonPath("$.gmailAddress").doesNotExist())
				.andExpect(jsonPath("$.connectedAt").doesNotExist());
	}

	@Test
	void returnsTheConnectedMailboxWithoutExposingTokens() throws Exception {
		UserEntity user = saveUser();
		connectionService.connect(
				user,
				"google-subject",
				"developer@example.com",
				"private-refresh-token",
				Set.of("openid", "https://www.googleapis.com/auth/gmail.readonly"));

		mockMvc.perform(get("/api/v1/google-connection").with(authenticatedUser()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.connected").value(true))
				.andExpect(jsonPath("$.gmailAddress").value("developer@example.com"))
				.andExpect(jsonPath("$.connectedAt").value("2026-07-20T10:00:00Z"))
				.andExpect(jsonPath("$.refreshToken").doesNotExist())
				.andExpect(jsonPath("$.encryptedRefreshToken").doesNotExist());

		assertThat(connectionRepository.findByUserId(user.getId()))
				.isPresent()
				.get()
				.extracting(GoogleConnectionEntity::getEncryptedRefreshToken)
				.asString()
				.doesNotContain("private-refresh-token");
	}

	@Test
	void revokesAndDeletesTheConnection() throws Exception {
		UserEntity user = saveUser();
		connectionService.connect(
				user,
				"google-subject",
				"developer@example.com",
				"private-refresh-token",
				Set.of("https://www.googleapis.com/auth/gmail.readonly"));

		mockMvc.perform(delete("/api/v1/google-connection")
						.with(authenticatedUser())
						.with(csrf()))
				.andExpect(status().isNoContent());

		assertThat(tokenRevocationClient.getRevokedToken()).isEqualTo("private-refresh-token");
		assertThat(connectionRepository.findByUserId(user.getId())).isEmpty();
	}

	@Test
	void protectsDisconnectWithCsrf() throws Exception {
		mockMvc.perform(delete("/api/v1/google-connection").with(authenticatedUser()))
				.andExpect(status().isForbidden());
	}

	@Test
	void requiresAnAuthenticatedSession() throws Exception {
		mockMvc.perform(get("/api/v1/google-connection"))
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

	private org.springframework.test.web.servlet.request.RequestPostProcessor authenticatedUser() {
		return oidcLogin().idToken(token -> token
				.subject("google-subject")
				.claim("email", "developer@example.com")
				.claim("name", "Career OS Developer"));
	}
}
