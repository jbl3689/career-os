package com.careeros.api.application.persistence;

import java.time.LocalDate;

import com.careeros.api.application.ApplicationStatus;
import com.careeros.api.auth.persistence.UserEntity;

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
@Table(name = "job_applications")
public class JobApplicationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private CompanyEntity company;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(name = "role_title", nullable = false, length = 200)
	private String roleTitle;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ApplicationStatus status;

	@Column(name = "application_date", nullable = false)
	private LocalDate applicationDate;

	@Column(nullable = false, length = 100)
	private String source;

	@Column(nullable = false, length = 5000)
	private String notes;

	@Column(name = "last_activity_date", nullable = false)
	private LocalDate lastActivityDate;

	protected JobApplicationEntity() {
	}

	public JobApplicationEntity(
			UserEntity user,
			CompanyEntity company,
			String roleTitle,
			ApplicationStatus status,
			LocalDate applicationDate,
			String source,
			String notes,
			LocalDate lastActivityDate) {
		this.user = user;
		this.company = company;
		this.roleTitle = roleTitle;
		this.status = status;
		this.applicationDate = applicationDate;
		this.source = source;
		this.notes = notes;
		this.lastActivityDate = lastActivityDate;
	}

	public Long getId() {
		return id;
	}

	public CompanyEntity getCompany() {
		return company;
	}

	public UserEntity getUser() {
		return user;
	}

	public String getRoleTitle() {
		return roleTitle;
	}

	public ApplicationStatus getStatus() {
		return status;
	}

	public LocalDate getApplicationDate() {
		return applicationDate;
	}

	public String getSource() {
		return source;
	}

	public String getNotes() {
		return notes;
	}

	public LocalDate getLastActivityDate() {
		return lastActivityDate;
	}

	public void update(
			ApplicationStatus status,
			String source,
			String notes,
			LocalDate lastActivityDate) {
		this.status = status;
		this.source = source;
		this.notes = notes;
		this.lastActivityDate = lastActivityDate;
	}
}
