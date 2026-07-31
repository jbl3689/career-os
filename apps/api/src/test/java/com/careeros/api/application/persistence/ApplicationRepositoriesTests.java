package com.careeros.api.application.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;

import com.careeros.api.PostgresIntegrationTest;
import com.careeros.api.application.ApplicationStatus;
import com.careeros.api.auth.persistence.UserEntity;
import com.careeros.api.auth.persistence.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ApplicationRepositoriesTests extends PostgresIntegrationTest {

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
	void storesAnApplicationAndItsEvent() {
		UserEntity user = userRepository.save(new UserEntity(
				"google-subject",
				"developer@example.com",
				"Career OS Developer",
				null,
				Instant.parse("2026-07-20T10:00:00Z")));
		CompanyEntity company = companyRepository.save(new CompanyEntity("Acme Ltd"));
		JobApplicationEntity application = applicationRepository.save(new JobApplicationEntity(
				user,
				company,
				"Software Engineer",
				ApplicationStatus.APPLIED,
				LocalDate.of(2026, 7, 16),
				"LinkedIn job post",
				"Applied through the company website.",
				LocalDate.of(2026, 7, 16)));
		eventRepository.save(new JobEventEntity(
				application,
				JobEventType.APPLICATION_CREATED,
				LocalDate.of(2026, 7, 16),
				"Created with status APPLIED"));

		JobApplicationEntity storedApplication = applicationRepository
				.findByIdAndUserId(application.getId(), user.getId())
				.orElseThrow();

		assertThat(storedApplication.getCompany().getName()).isEqualTo("Acme Ltd");
		assertThat(storedApplication.getUser().getEmail()).isEqualTo("developer@example.com");
		assertThat(storedApplication.getRoleTitle()).isEqualTo("Software Engineer");
		assertThat(storedApplication.getSource()).isEqualTo("LinkedIn job post");
		assertThat(eventRepository.findAllByJobApplicationIdOrderByEventDateAscIdAsc(application.getId()))
				.singleElement()
				.extracting(JobEventEntity::getEventType)
				.isEqualTo(JobEventType.APPLICATION_CREATED);
	}
}
