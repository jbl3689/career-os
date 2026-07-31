package com.careeros.api.application;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateJobApplicationRequest(
		@NotBlank(message = "Company name is required")
		@Size(max = 200, message = "Company name must be 200 characters or fewer")
		String companyName,

		@NotBlank(message = "Role title is required")
		@Size(max = 200, message = "Role title must be 200 characters or fewer")
		String roleTitle,

		@NotNull(message = "Status is required")
		ApplicationStatus status,

		@NotNull(message = "Application date is required")
		LocalDate applicationDate,

		@Size(max = 100, message = "Application source must be 100 characters or fewer")
		String source,

		@Size(max = 5000, message = "Notes must be 5000 characters or fewer")
		String notes) {
}
