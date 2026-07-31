package com.careeros.api.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.careeros.api.application.persistence.CompanyEntity;
import com.careeros.api.application.persistence.CompanyRepository;
import com.careeros.api.application.persistence.JobApplicationEntity;
import com.careeros.api.application.persistence.JobApplicationRepository;
import com.careeros.api.application.persistence.JobEventEntity;
import com.careeros.api.application.persistence.JobEventRepository;
import com.careeros.api.application.persistence.JobEventType;
import com.careeros.api.auth.persistence.UserEntity;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JobApplicationService {

	private final CompanyRepository companyRepository;
	private final JobApplicationRepository applicationRepository;
	private final JobEventRepository eventRepository;
	private final Clock clock;

	public JobApplicationService(
			CompanyRepository companyRepository,
			JobApplicationRepository applicationRepository,
			JobEventRepository eventRepository,
			Clock clock) {
		this.companyRepository = companyRepository;
		this.applicationRepository = applicationRepository;
		this.eventRepository = eventRepository;
		this.clock = clock;
	}

	public List<JobApplicationResponse> listApplications(UserEntity user) {
		return applicationRepository.findAllByUserIdOrderByIdAsc(user.getId()).stream()
				.map(JobApplicationResponse::from)
				.toList();
	}

	@Transactional
	public JobApplicationResponse createApplication(UserEntity user, CreateJobApplicationRequest request) {
		String companyName = request.companyName().trim();
		String source = request.source() == null ? "" : request.source().trim();
		String notes = request.notes() == null ? "" : request.notes().trim();
		CompanyEntity company = companyRepository.findByName(companyName)
				.orElseGet(() -> companyRepository.save(new CompanyEntity(companyName)));
		JobApplicationEntity application = applicationRepository.save(new JobApplicationEntity(
				user,
				company,
				request.roleTitle().trim(),
				request.status(),
				request.applicationDate(),
				source,
				notes,
				request.applicationDate()));
		eventRepository.save(new JobEventEntity(
				application,
				JobEventType.APPLICATION_CREATED,
				request.applicationDate(),
				"Created with status " + request.status()));

		return JobApplicationResponse.from(application);
	}

	public JobApplicationResponse getApplication(UserEntity user, long id) {
		return JobApplicationResponse.from(findApplication(user, id));
	}

	@Transactional
	public JobApplicationResponse updateApplication(
			UserEntity user,
			long id,
			UpdateJobApplicationRequest request) {
		JobApplicationEntity application = findApplication(user, id);
		ApplicationStatus previousStatus = application.getStatus();
		String previousNotes = application.getNotes();
		String source = request.source() == null
				? application.getSource()
				: request.source().trim();
		String notes = request.notes() == null ? previousNotes : request.notes().trim();
		LocalDate eventDate = LocalDate.now(clock);

		application.update(request.status(), source, notes, eventDate);
		applicationRepository.save(application);

		if (previousStatus != request.status()) {
			eventRepository.save(new JobEventEntity(
					application,
					JobEventType.STATUS_CHANGED,
					eventDate,
					previousStatus + " -> " + request.status()));
		}
		if (!Objects.equals(previousNotes, notes)) {
			eventRepository.save(new JobEventEntity(
					application,
					JobEventType.NOTES_UPDATED,
					eventDate,
					"Notes updated"));
		}

		return JobApplicationResponse.from(application);
	}

	private JobApplicationEntity findApplication(UserEntity user, long id) {
		return applicationRepository.findByIdAndUserId(id, user.getId())
				.orElseThrow(() -> new JobApplicationNotFoundException(id));
	}
}
