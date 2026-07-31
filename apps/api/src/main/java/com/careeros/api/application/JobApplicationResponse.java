package com.careeros.api.application;

import java.time.LocalDate;

import com.careeros.api.application.persistence.JobApplicationEntity;

public record JobApplicationResponse(
		long id,
		String companyName,
		String roleTitle,
		ApplicationStatus status,
		LocalDate applicationDate,
		String source,
		String notes,
		LocalDate lastActivityDate) {

	public static JobApplicationResponse from(JobApplicationEntity application) {
		return new JobApplicationResponse(
				application.getId(),
				application.getCompany().getName(),
				application.getRoleTitle(),
				application.getStatus(),
				application.getApplicationDate(),
				application.getSource(),
				application.getNotes(),
				application.getLastActivityDate());
	}
}
