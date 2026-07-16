package com.careeros.api.application.persistence;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_events")
public class JobEventEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "job_application_id", nullable = false)
	private JobApplicationEntity jobApplication;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 50)
	private JobEventType eventType;

	@Column(name = "event_date", nullable = false)
	private LocalDate eventDate;

	@Column(length = 5000)
	private String details;

	protected JobEventEntity() {
	}

	public JobEventEntity(
			JobApplicationEntity jobApplication,
			JobEventType eventType,
			LocalDate eventDate,
			String details) {
		this.jobApplication = jobApplication;
		this.eventType = eventType;
		this.eventDate = eventDate;
		this.details = details;
	}

	public Long getId() {
		return id;
	}

	public JobApplicationEntity getJobApplication() {
		return jobApplication;
	}

	public JobEventType getEventType() {
		return eventType;
	}

	public LocalDate getEventDate() {
		return eventDate;
	}

	public String getDetails() {
		return details;
	}
}
