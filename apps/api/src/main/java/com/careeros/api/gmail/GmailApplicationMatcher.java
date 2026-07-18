package com.careeros.api.gmail;

import java.util.List;
import java.util.Optional;

import com.careeros.api.application.persistence.JobApplicationEntity;

public interface GmailApplicationMatcher {

	Optional<GmailApplicationMatch> findSuggestion(
			GmailMessageMetadata message,
			List<JobApplicationEntity> applications);
}
