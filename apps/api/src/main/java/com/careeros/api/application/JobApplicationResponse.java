package com.careeros.api.application;

import java.time.LocalDate;

public record JobApplicationResponse(
		long id,
		String companyName,
		String roleTitle,
		ApplicationStatus status,
		LocalDate applicationDate,
		String notes,
		LocalDate lastActivityDate) {

	public static JobApplicationResponse from(JobApplication application) {
		return new JobApplicationResponse(
				application.id(),
				application.companyName(),
				application.roleTitle(),
				application.status(),
				application.applicationDate(),
				application.notes(),
				application.lastActivityDate());
	}
}
