package com.careeros.api.application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.careeros.api.application.persistence.CompanyRepository;
import com.careeros.api.application.persistence.JobApplicationRepository;
import com.careeros.api.application.persistence.JobEventRepository;
import com.careeros.api.PostgresIntegrationTest;
import com.careeros.api.auth.persistence.UserRepository;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class JobApplicationControllerTests extends PostgresIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private JobApplicationRepository applicationRepository;

	@Autowired
	private JobEventRepository eventRepository;

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
	void listsApplications() throws Exception {
		mockMvc.perform(get("/api/v1/applications").with(authenticatedUser()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}

	@Test
	void requiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/applications"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void createsAnApplication() throws Exception {
		String requestBody = """
				{
				  "companyName": "Acme Ltd",
				  "roleTitle": "Software Engineer",
				  "status": "APPLIED",
				  "applicationDate": "2026-07-16",
				  "notes": "Applied through the company website."
				}
				""";

		mockMvc.perform(post("/api/v1/applications")
						.with(authenticatedUser())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.companyName").value("Acme Ltd"))
				.andExpect(jsonPath("$.roleTitle").value("Software Engineer"))
				.andExpect(jsonPath("$.status").value("APPLIED"))
				.andExpect(jsonPath("$.applicationDate").value("2026-07-16"))
				.andExpect(jsonPath("$.lastActivityDate").value("2026-07-16"));
	}

	@Test
	void rejectsMissingRequiredFields() throws Exception {
		mockMvc.perform(post("/api/v1/applications")
						.with(authenticatedUser())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Validation failed"))
				.andExpect(jsonPath("$.fieldErrors.companyName").value("Company name is required"))
				.andExpect(jsonPath("$.fieldErrors.roleTitle").value("Role title is required"))
				.andExpect(jsonPath("$.fieldErrors.status").value("Status is required"))
				.andExpect(jsonPath("$.fieldErrors.applicationDate").value("Application date is required"));
	}

	@Test
	void rejectsAnUnknownStatus() throws Exception {
		String requestBody = """
				{
				  "companyName": "Acme Ltd",
				  "roleTitle": "Software Engineer",
				  "status": "PENDING",
				  "applicationDate": "2026-07-16"
				}
				""";

		mockMvc.perform(post("/api/v1/applications")
						.with(authenticatedUser())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Request body contains an invalid value"));
	}

	@Test
	void getsAnApplicationById() throws Exception {
		long id = createApplication();

		mockMvc.perform(get("/api/v1/applications/{id}", id).with(authenticatedUser()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.companyName").value("Acme Ltd"))
				.andExpect(jsonPath("$.roleTitle").value("Software Engineer"));
	}

	@Test
	void updatesStatusAndNotes() throws Exception {
		long id = createApplication();
		String requestBody = """
				{
				  "status": "INTERVIEWING",
				  "notes": "First interview booked."
				}
				""";

		mockMvc.perform(patch("/api/v1/applications/{id}", id)
						.with(authenticatedUser())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("INTERVIEWING"))
				.andExpect(jsonPath("$.notes").value("First interview booked."))
				.andExpect(jsonPath("$.lastActivityDate").exists());
	}

	@Test
	void returnsNotFoundForAnUnknownApplication() throws Exception {
		mockMvc.perform(get("/api/v1/applications/999").with(authenticatedUser()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Job application 999 was not found"));
	}

	@Test
	void returnsNotFoundWhenUpdatingAnUnknownApplication() throws Exception {
		String requestBody = """
				{
				  "status": "INTERVIEWING",
				  "notes": "First interview booked."
				}
				""";

		mockMvc.perform(patch("/api/v1/applications/999")
						.with(authenticatedUser())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Job application 999 was not found"));
	}

	@Test
	void rejectsAnUpdateWithoutAStatus() throws Exception {
		long id = createApplication();

		mockMvc.perform(patch("/api/v1/applications/{id}", id)
						.with(authenticatedUser())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"notes\":\"First interview booked.\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.status").value("Status is required"));
	}

	private long createApplication() throws Exception {
		String requestBody = """
				{
				  "companyName": "Acme Ltd",
				  "roleTitle": "Software Engineer",
				  "status": "APPLIED",
				  "applicationDate": "2026-07-16",
				  "notes": "Applied through the company website."
				}
				""";

		String responseBody = mockMvc.perform(post("/api/v1/applications")
						.with(authenticatedUser())
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		return ((Number) JsonPath.read(responseBody, "$.id")).longValue();
	}

	private RequestPostProcessor authenticatedUser() {
		return oidcLogin().idToken(token -> token
				.subject("google-subject")
				.claim("email", "developer@example.com")
				.claim("name", "Career OS Developer"));
	}
}
