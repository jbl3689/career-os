package com.careeros.api.application.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import com.careeros.api.PostgresIntegrationTest;
import com.careeros.api.application.ApplicationStatus;

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

	@BeforeEach
	void clearDatabase() {
		eventRepository.deleteAll();
		applicationRepository.deleteAll();
		companyRepository.deleteAll();
	}

	@Test
	void storesAnApplicationAndItsEvent() {
		CompanyEntity company = companyRepository.save(new CompanyEntity("Acme Ltd"));
		JobApplicationEntity application = applicationRepository.save(new JobApplicationEntity(
				company,
				"Software Engineer",
				ApplicationStatus.APPLIED,
				LocalDate.of(2026, 7, 16),
				"Applied through the company website.",
				LocalDate.of(2026, 7, 16)));
		eventRepository.save(new JobEventEntity(
				application,
				JobEventType.APPLICATION_CREATED,
				LocalDate.of(2026, 7, 16),
				"Created with status APPLIED"));

		JobApplicationEntity storedApplication = applicationRepository.findById(application.getId()).orElseThrow();

		assertThat(storedApplication.getCompany().getName()).isEqualTo("Acme Ltd");
		assertThat(storedApplication.getRoleTitle()).isEqualTo("Software Engineer");
		assertThat(eventRepository.findAllByJobApplicationIdOrderByEventDateAscIdAsc(application.getId()))
				.singleElement()
				.extracting(JobEventEntity::getEventType)
				.isEqualTo(JobEventType.APPLICATION_CREATED);
	}
}
