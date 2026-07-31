package com.careeros.api.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateJobApplicationRequest(
		@NotNull(message = "Status is required")
		ApplicationStatus status,

		@Size(max = 100, message = "Application source must be 100 characters or fewer")
		String source,

		@Size(max = 5000, message = "Notes must be 5000 characters or fewer")
		String notes) {
}
