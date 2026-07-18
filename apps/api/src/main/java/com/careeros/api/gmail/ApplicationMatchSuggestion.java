package com.careeros.api.gmail;

import com.careeros.api.application.persistence.JobApplicationEntity;

public record ApplicationMatchSuggestion(
		long applicationId,
		String companyName,
		String roleTitle,
		int confidenceScore,
		String reason) {

	public static ApplicationMatchSuggestion from(
			JobApplicationEntity application,
			int confidenceScore,
			String reason) {
		return new ApplicationMatchSuggestion(
				application.getId(),
				application.getCompany().getName(),
				application.getRoleTitle(),
				confidenceScore,
				reason);
	}
}
