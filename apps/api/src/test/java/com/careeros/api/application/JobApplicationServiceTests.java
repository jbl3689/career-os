package com.careeros.api.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.careeros.api.PostgresIntegrationTest;
import com.careeros.api.application.persistence.CompanyRepository;
import com.careeros.api.application.persistence.JobApplicationRepository;
import com.careeros.api.application.persistence.JobEventEntity;
import com.careeros.api.application.persistence.JobEventRepository;
import com.careeros.api.application.persistence.JobEventType;
import com.careeros.api.auth.persistence.UserEntity;
import com.careeros.api.auth.persistence.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class JobApplicationServiceTests extends PostgresIntegrationTest {

	@Autowired
	private JobApplicationService service;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private JobApplicationRepository applicationRepository;

	@Autowired
	private JobEventRepository eventRepository;

	@Autowired
	private UserRepository userRepository;

	private UserEntity user;

	@BeforeEach
	void clearDatabase() {
		eventRepository.deleteAll();
		applicationRepository.deleteAll();
		companyRepository.deleteAll();
		userRepository.deleteAll();
		user = userRepository.save(new UserEntity(
				"google-subject-1",
				"developer@example.com",
				"Career OS Developer",
				null,
				Instant.parse("2026-07-20T10:00:00Z")));
	}

	@Test
	void createsAndListsAnApplication() {
		LocalDate applicationDate = LocalDate.of(2026, 7, 16);
		CreateJobApplicationRequest request = new CreateJobApplicationRequest(
				"  Acme Ltd  ",
				"  Software Engineer  ",
				ApplicationStatus.APPLIED,
				applicationDate,
				"  Applied through the company website.  ");

		JobApplicationResponse created = service.createApplication(user, request);

		assertThat(created.id()).isPositive();
		assertThat(created.companyName()).isEqualTo("Acme Ltd");
		assertThat(created.roleTitle()).isEqualTo("Software Engineer");
		assertThat(created.notes()).isEqualTo("Applied through the company website.");
		assertThat(created.lastActivityDate()).isEqualTo(applicationDate);
		assertThat(service.listApplications(user)).containsExactly(created);
		assertThat(eventTypesFor(created.id())).containsExactly(JobEventType.APPLICATION_CREATED);
	}

	@Test
	void reusesAnExistingCompany() {
		createApplication();
		service.createApplication(user, new CreateJobApplicationRequest(
				"Acme Ltd",
				"Senior Software Engineer",
				ApplicationStatus.APPLIED,
				LocalDate.of(2026, 7, 18),
				null));

		assertThat(companyRepository.count()).isEqualTo(1);
		assertThat(applicationRepository.count()).isEqualTo(2);
	}

	@Test
	void replacesMissingNotesWithAnEmptyString() {
		CreateJobApplicationRequest request = new CreateJobApplicationRequest(
				"Acme Ltd",
				"Software Engineer",
				ApplicationStatus.APPLIED,
				LocalDate.of(2026, 7, 16),
				null);

		JobApplicationResponse created = service.createApplication(user, request);

		assertThat(created.notes()).isEmpty();
	}

	@Test
	void getsAnApplicationById() {
		JobApplicationResponse created = createApplication();

		JobApplicationResponse found = service.getApplication(user, created.id());

		assertThat(found).isEqualTo(created);
	}

	@Test
	void updatesStatusNotesAndLastActivityDate() {
		JobApplicationResponse created = createApplication();
		UpdateJobApplicationRequest request = new UpdateJobApplicationRequest(
				ApplicationStatus.INTERVIEWING,
				"  First interview booked.  ");

		JobApplicationResponse updated = service.updateApplication(user, created.id(), request);

		assertThat(updated.status()).isEqualTo(ApplicationStatus.INTERVIEWING);
		assertThat(updated.notes()).isEqualTo("First interview booked.");
		assertThat(updated.lastActivityDate()).isEqualTo(LocalDate.of(2026, 7, 20));
		assertThat(service.getApplication(user, created.id())).isEqualTo(updated);
		assertThat(eventTypesFor(created.id())).containsExactly(
				JobEventType.APPLICATION_CREATED,
				JobEventType.STATUS_CHANGED,
				JobEventType.NOTES_UPDATED);
	}

	@Test
	void preservesNotesWhenTheyAreOmittedFromAnUpdate() {
		JobApplicationResponse created = createApplication();

		JobApplicationResponse updated = service.updateApplication(
				user,
				created.id(),
				new UpdateJobApplicationRequest(ApplicationStatus.INTERVIEWING, null));

		assertThat(updated.notes()).isEqualTo(created.notes());
		assertThat(eventTypesFor(created.id())).containsExactly(
				JobEventType.APPLICATION_CREATED,
				JobEventType.STATUS_CHANGED);
	}

	@Test
	void rejectsAnUnknownApplicationId() {
		assertThatThrownBy(() -> service.getApplication(user, 999))
				.isInstanceOf(JobApplicationNotFoundException.class)
				.hasMessage("Job application 999 was not found");
	}

	@Test
	void doesNotExposeAnotherUsersApplication() {
		JobApplicationResponse created = createApplication();
		UserEntity anotherUser = userRepository.save(new UserEntity(
				"google-subject-2",
				"another@example.com",
				"Another User",
				null,
				Instant.parse("2026-07-20T10:00:00Z")));

		assertThat(service.listApplications(anotherUser)).isEmpty();
		assertThatThrownBy(() -> service.getApplication(anotherUser, created.id()))
				.isInstanceOf(JobApplicationNotFoundException.class);
	}

	private JobApplicationResponse createApplication() {
		return service.createApplication(user, new CreateJobApplicationRequest(
				"Acme Ltd",
				"Software Engineer",
				ApplicationStatus.APPLIED,
				LocalDate.of(2026, 7, 16),
				"Applied through the company website."));
	}

	private List<JobEventType> eventTypesFor(long applicationId) {
		return eventRepository.findAllByJobApplicationIdOrderByEventDateAscIdAsc(applicationId).stream()
				.map(JobEventEntity::getEventType)
				.toList();
	}
}
