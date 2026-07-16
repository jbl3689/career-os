package com.careeros.api.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateJobApplicationRequest(
		@NotNull(message = "Status is required")
		ApplicationStatus status,

		@Size(max = 5000, message = "Notes must be 5000 characters or fewer")
		String notes) {
}
