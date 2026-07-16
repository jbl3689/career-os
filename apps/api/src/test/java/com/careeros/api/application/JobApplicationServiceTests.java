package com.careeros.api.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class JobApplicationServiceTests {

	private final InMemoryJobApplicationRepository repository = new InMemoryJobApplicationRepository();
	private final Clock clock = Clock.fixed(Instant.parse("2026-07-20T10:00:00Z"), ZoneOffset.UTC);
	private final JobApplicationService service = new JobApplicationService(repository, clock);

	@Test
	void createsAndListsAnApplication() {
		LocalDate applicationDate = LocalDate.of(2026, 7, 16);
		CreateJobApplicationRequest request = new CreateJobApplicationRequest(
				"  Acme Ltd  ",
				"  Software Engineer  ",
				ApplicationStatus.APPLIED,
				applicationDate,
				"  Applied through the company website.  ");

		JobApplicationResponse created = service.createApplication(request);

		assertThat(created.id()).isEqualTo(1);
		assertThat(created.companyName()).isEqualTo("Acme Ltd");
		assertThat(created.roleTitle()).isEqualTo("Software Engineer");
		assertThat(created.notes()).isEqualTo("Applied through the company website.");
		assertThat(created.lastActivityDate()).isEqualTo(applicationDate);
		assertThat(service.listApplications()).containsExactly(created);
	}

	@Test
	void replacesMissingNotesWithAnEmptyString() {
		CreateJobApplicationRequest request = new CreateJobApplicationRequest(
				"Acme Ltd",
				"Software Engineer",
				ApplicationStatus.APPLIED,
				LocalDate.of(2026, 7, 16),
				null);

		JobApplicationResponse created = service.createApplication(request);

		assertThat(created.notes()).isEmpty();
	}

	@Test
	void getsAnApplicationById() {
		JobApplicationResponse created = createApplication();

		JobApplicationResponse found = service.getApplication(created.id());

		assertThat(found).isEqualTo(created);
	}

	@Test
	void updatesStatusNotesAndLastActivityDate() {
		JobApplicationResponse created = createApplication();
		UpdateJobApplicationRequest request = new UpdateJobApplicationRequest(
				ApplicationStatus.INTERVIEWING,
				"  First interview booked.  ");

		JobApplicationResponse updated = service.updateApplication(created.id(), request);

		assertThat(updated.status()).isEqualTo(ApplicationStatus.INTERVIEWING);
		assertThat(updated.notes()).isEqualTo("First interview booked.");
		assertThat(updated.lastActivityDate()).isEqualTo(LocalDate.of(2026, 7, 20));
		assertThat(service.getApplication(created.id())).isEqualTo(updated);
	}

	@Test
	void preservesNotesWhenTheyAreOmittedFromAnUpdate() {
		JobApplicationResponse created = createApplication();

		JobApplicationResponse updated = service.updateApplication(
				created.id(),
				new UpdateJobApplicationRequest(ApplicationStatus.INTERVIEWING, null));

		assertThat(updated.notes()).isEqualTo(created.notes());
	}

	@Test
	void rejectsAnUnknownApplicationId() {
		assertThatThrownBy(() -> service.getApplication(999))
				.isInstanceOf(JobApplicationNotFoundException.class)
				.hasMessage("Job application 999 was not found");
	}

	private JobApplicationResponse createApplication() {
		return service.createApplication(new CreateJobApplicationRequest(
				"Acme Ltd",
				"Software Engineer",
				ApplicationStatus.APPLIED,
				LocalDate.of(2026, 7, 16),
				"Applied through the company website."));
	}
}
