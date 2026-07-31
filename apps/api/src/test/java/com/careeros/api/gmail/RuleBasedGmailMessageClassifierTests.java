package com.careeros.api.gmail;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class RuleBasedGmailMessageClassifierTests {

	private final GmailMessageClassifier classifier =
			new RuleBasedGmailMessageClassifier();

	@Test
	void classifiesInterviewMessagesAsJobRelated() {
		GmailMessageClassification result = classifier.classify(message(
				"Recruiter <recruiter@example.com>",
				"Interview invitation for Software Engineer role",
				""));

		assertThat(result.category()).isEqualTo(GmailClassificationCategory.JOB_RELATED);
		assertThat(result.eventType()).isEqualTo(GmailEventType.INTERVIEW);
		assertThat(result.confidenceScore()).isGreaterThanOrEqualTo(80);
	}

	@Test
	void classifiesApplicantTrackingSystemMessagesAsJobRelated() {
		GmailMessageClassification result = classifier.classify(message(
				"notifications@company.greenhouse.io",
				"Application update",
				""));

		assertThat(result.category()).isEqualTo(GmailClassificationCategory.JOB_RELATED);
		assertThat(result.eventType()).isEqualTo(GmailEventType.APPLICATION);
	}

	@Test
	void rejectsVisaApplicationFalsePositives() {
		GmailMessageClassification result = classifier.classify(message(
				"UK Visas and Immigration <notifications@example.gov.uk>",
				"Your visa application has been received",
				""));

		assertThat(result.category()).isEqualTo(GmailClassificationCategory.NOT_JOB_RELATED);
		assertThat(result.eventType()).isEqualTo(GmailEventType.UNKNOWN);
	}

	@Test
	void keepsGenericApplicationMessagesUncertain() {
		GmailMessageClassification result = classifier.classify(message(
				"notifications@example.com",
				"Application update",
				""));

		assertThat(result.category()).isEqualTo(GmailClassificationCategory.UNCERTAIN);
		assertThat(result.eventType()).isEqualTo(GmailEventType.APPLICATION);
	}

	@Test
	void keepsMessagesWithJobAndVisaSignalsUncertain() {
		GmailMessageClassification result = classifier.classify(message(
				"Recruiter <recruiter@example.com>",
				"Visa requirements before your interview",
				""));

		assertThat(result.category()).isEqualTo(GmailClassificationCategory.UNCERTAIN);
		assertThat(result.eventType()).isEqualTo(GmailEventType.INTERVIEW);
	}

	@Test
	void identifiesARejectionFromTheExcerptWhenTheSubjectIsVague() {
		GmailMessageClassification result = classifier.classify(message(
				"Acme Talent <talent@acme.example>",
				"An update on your application",
				"We have decided not to move forward with your application."));

		assertThat(result.category()).isEqualTo(GmailClassificationCategory.JOB_RELATED);
		assertThat(result.eventType()).isEqualTo(GmailEventType.REJECTION);
		assertThat(result.reason()).isEqualTo("Application rejection terminology was found");
	}

	private GmailMessageMetadata message(
			String sender,
			String subject,
			String excerpt) {
		return new GmailMessageMetadata(
				"message-1",
				"thread-1",
				sender,
				subject,
				excerpt,
				Instant.parse("2026-07-19T14:30:00Z"));
	}
}
