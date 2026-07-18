package com.careeros.api.gmail;

import jakarta.validation.constraints.Positive;

public record MatchGmailReviewRequest(
		@Positive(message = "Application ID must be positive")
		long applicationId) {
}
