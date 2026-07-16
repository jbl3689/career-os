package com.careeros.api.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class JobApplicationService {

	private final InMemoryJobApplicationRepository repository;
	private final Clock clock;

	public JobApplicationService(InMemoryJobApplicationRepository repository, Clock clock) {
		this.repository = repository;
		this.clock = clock;
	}

	public List<JobApplicationResponse> listApplications() {
		return repository.findAll().stream()
				.map(JobApplicationResponse::from)
				.toList();
	}

	public JobApplicationResponse createApplication(CreateJobApplicationRequest request) {
		String notes = request.notes() == null ? "" : request.notes().trim();
		JobApplication application = new JobApplication(
				0,
				request.companyName().trim(),
				request.roleTitle().trim(),
				request.status(),
				request.applicationDate(),
				notes,
				request.applicationDate());

		return JobApplicationResponse.from(repository.create(application));
	}

	public JobApplicationResponse getApplication(long id) {
		return JobApplicationResponse.from(findApplication(id));
	}

	public JobApplicationResponse updateApplication(long id, UpdateJobApplicationRequest request) {
		JobApplication existingApplication = findApplication(id);
		String notes = request.notes() == null ? existingApplication.notes() : request.notes().trim();
		JobApplication updatedApplication = new JobApplication(
				existingApplication.id(),
				existingApplication.companyName(),
				existingApplication.roleTitle(),
				request.status(),
				existingApplication.applicationDate(),
				notes,
				LocalDate.now(clock));

		return JobApplicationResponse.from(repository.update(updatedApplication));
	}

	private JobApplication findApplication(long id) {
		return repository.findById(id)
				.orElseThrow(() -> new JobApplicationNotFoundException(id));
	}
}
