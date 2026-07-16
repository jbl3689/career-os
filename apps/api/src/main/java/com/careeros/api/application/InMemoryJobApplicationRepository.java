package com.careeros.api.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

@Repository
public class InMemoryJobApplicationRepository {

	private final AtomicLong nextId = new AtomicLong(1);
	private final Map<Long, JobApplication> applications = new LinkedHashMap<>();

	public synchronized JobApplication create(JobApplication application) {
		long id = nextId.getAndIncrement();
		JobApplication savedApplication = new JobApplication(
				id,
				application.companyName(),
				application.roleTitle(),
				application.status(),
				application.applicationDate(),
				application.notes(),
				application.lastActivityDate());
		applications.put(id, savedApplication);
		return savedApplication;
	}

	public synchronized List<JobApplication> findAll() {
		return new ArrayList<>(applications.values());
	}

	public synchronized Optional<JobApplication> findById(long id) {
		return Optional.ofNullable(applications.get(id));
	}

	public synchronized JobApplication update(JobApplication application) {
		applications.put(application.id(), application);
		return application;
	}

	synchronized void clear() {
		applications.clear();
		nextId.set(1);
	}
}
