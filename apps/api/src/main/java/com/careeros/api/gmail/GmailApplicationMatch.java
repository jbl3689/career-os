package com.careeros.api.gmail;

import com.careeros.api.application.persistence.JobApplicationEntity;

public record GmailApplicationMatch(
		JobApplicationEntity application,
		int confidenceScore,
		String reason) {
}
