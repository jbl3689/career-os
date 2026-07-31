package com.careeros.api.gmail;

import com.careeros.api.application.JobApplicationResponse;

public record CreateApplicationFromGmailReviewResponse(
		GmailCandidateResponse review,
		JobApplicationResponse application) {
}
