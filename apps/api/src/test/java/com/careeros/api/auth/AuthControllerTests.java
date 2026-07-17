package com.careeros.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.careeros.api.PostgresIntegrationTest;
import com.careeros.api.application.persistence.CompanyRepository;
import com.careeros.api.application.persistence.JobApplicationRepository;
import com.careeros.api.application.persistence.JobEventRepository;
import com.careeros.api.auth.persistence.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class AuthControllerTests extends PostgresIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JobEventRepository eventRepository;

	@Autowired
	private JobApplicationRepository applicationRepository;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void clearDatabase() {
		eventRepository.deleteAll();
		applicationRepository.deleteAll();
		companyRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void returnsTheSignedInUserAndPersistsTheirCareerOsAccount() throws Exception {
		mockMvc.perform(get("/api/v1/auth/me").with(oidcLogin().idToken(token -> token
						.subject("google-subject")
						.claim("email", "developer@example.com")
						.claim("name", "Career OS Developer")
						.claim("picture", "https://example.com/avatar.png"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.email").value("developer@example.com"))
				.andExpect(jsonPath("$.displayName").value("Career OS Developer"))
				.andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.png"));

		assertThat(userRepository.findByGoogleSubject("google-subject"))
				.isPresent()
				.get()
				.extracting(user -> user.getEmail())
				.isEqualTo("developer@example.com");
	}

	@Test
	void rejectsCurrentUserRequestsWithoutASession() throws Exception {
		mockMvc.perform(get("/api/v1/auth/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void providesACsrfTokenBeforeSignIn() throws Exception {
		mockMvc.perform(get("/api/v1/auth/csrf"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void signsOutWithCsrfProtection() throws Exception {
		mockMvc.perform(post("/api/v1/auth/logout")
						.with(oidcLogin())
						.with(csrf()))
				.andExpect(status().isNoContent());
	}

	@Test
	void rejectsSignOutWithoutACsrfToken() throws Exception {
		mockMvc.perform(post("/api/v1/auth/logout").with(oidcLogin()))
				.andExpect(status().isForbidden());
	}

	@Test
	void startsGmailAuthorizationSeparatelyWithOfflineAccess() throws Exception {
		mockMvc.perform(get("/oauth2/authorization/google-gmail")
						.with(oidcLogin().idToken(token -> token
								.subject("google-subject")
								.claim("email", "developer@example.com"))))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", containsString("access_type=offline")))
				.andExpect(header().string("Location", containsString("prompt=consent")))
				.andExpect(header().string(
						"Location",
						containsString("gmail.readonly")));
	}
}
