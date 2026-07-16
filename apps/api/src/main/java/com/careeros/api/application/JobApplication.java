package com.careeros.api.application;

import java.time.LocalDate;

public record JobApplication(
		long id,
		String companyName,
		String roleTitle,
		ApplicationStatus status,
		LocalDate applicationDate,
		String notes,
		LocalDate lastActivityDate) {
}
