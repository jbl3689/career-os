package com.careeros.api.gmail;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.careeros.api.application.ApplicationStatus;
import com.careeros.api.application.persistence.CompanyEntity;
import com.careeros.api.application.persistence.JobApplicationEntity;
import com.careeros.api.auth.persistence.UserEntity;

import org.junit.jupiter.api.Test;

class RuleBasedGmailApplicationMatcherTests {

	private final RuleBasedGmailApplicationMatcher matcher =
			new RuleBasedGmailApplicationMatcher();

	@Test
	void suggestsAnApplicationWhenItsCompanyAndRoleAppear() {
		JobApplicationEntity application = application("Acme Ltd", "Software Engineer");

		var suggestion = matcher.findSuggestion(
				message("Careers at Acme Ltd", "Software Engineer interview"),
				List.of(application));

		assertThat(suggestion).hasValueSatisfying(match -> {
			assertThat(match.application()).isSameAs(application);
			assertThat(match.confidenceScore()).isEqualTo(100);
			assertThat(match.reason())
					.isEqualTo("Company and role appear in the sender or subject");
		});
	}

	@Test
	void usesALowerScoreForACompanyOnlyMatch() {
		var suggestion = matcher.findSuggestion(
				message("Acme Ltd Careers", "Interview invitation"),
				List.of(application("Acme Ltd", "Software Engineer")));

		assertThat(suggestion)
				.hasValueSatisfying(match -> assertThat(match.confidenceScore()).isEqualTo(70));
	}

	@Test
	void doesNotGuessWhenOnlyTheRoleMatches() {
		var suggestion = matcher.findSuggestion(
				message("Recruiter", "Software Engineer interview"),
				List.of(application("Acme Ltd", "Software Engineer")));

		assertThat(suggestion).isEmpty();
	}

	@Test
	void doesNotGuessWhenTwoApplicationsHaveTheSameBestScore() {
		var suggestion = matcher.findSuggestion(
				message("Acme Ltd Careers", "Interview invitation"),
				List.of(
						application("Acme Ltd", "Software Engineer"),
						application("Acme Ltd", "Platform Engineer")));

		assertThat(suggestion).isEmpty();
	}

	private GmailMessageMetadata message(String sender, String subject) {
		return new GmailMessageMetadata(
				"message-1",
				"thread-1",
				sender,
				subject,
				"",
				Instant.parse("2026-07-19T14:30:00Z"));
	}

	private JobApplicationEntity application(String companyName, String roleTitle) {
		UserEntity user = new UserEntity(
				"google-subject",
				"developer@example.com",
				"Developer",
				null,
				Instant.parse("2026-07-20T10:00:00Z"));
		return new JobApplicationEntity(
				user,
				new CompanyEntity(companyName),
				roleTitle,
				ApplicationStatus.APPLIED,
				LocalDate.of(2026, 7, 1),
				"",
				LocalDate.of(2026, 7, 1));
	}
}
