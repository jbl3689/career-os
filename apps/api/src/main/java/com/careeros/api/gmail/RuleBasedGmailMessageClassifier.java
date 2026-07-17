package com.careeros.api.gmail;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class RuleBasedGmailMessageClassifier implements GmailMessageClassifier {

	private static final List<String> APPLICANT_TRACKING_SYSTEMS = List.of(
			"greenhouse.io",
			"lever.co",
			"myworkday.com",
			"smartrecruiters.com",
			"ashbyhq.com",
			"teamtailor.com",
			"icims.com",
			"successfactors.com");

	private static final List<String> NON_EMPLOYMENT_TERMS = List.of(
			"visa",
			"immigration",
			"passport",
			"residence permit",
			"residency application",
			"citizenship application");

	private static final List<String> APPLICATION_CONFIRMATION_TERMS = List.of(
			"thank you for applying",
			"thanks for applying",
			"application received",
			"application has been received",
			"we received your application");

	private static final List<String> INTERVIEW_TERMS = List.of(
			"interview",
			"phone screen",
			"screening call");

	private static final List<String> ASSESSMENT_TERMS = List.of(
			"assessment",
			"coding challenge",
			"technical test",
			"take-home");

	private static final List<String> OFFER_TERMS = List.of(
			"job offer",
			"offer letter",
			"pleased to offer");

	private static final List<String> REJECTION_TERMS = List.of(
			"not moving forward",
			"other candidates",
			"position has been filled",
			"unsuccessful application");

	private static final List<String> RECRUITER_TERMS = List.of(
			"recruiter",
			"talent acquisition",
			"recruitment team");

	private static final List<String> EMPLOYMENT_CONTEXT_TERMS = List.of(
			"candidate",
			"job application",
			"position",
			"role");

	@Override
	public GmailMessageClassification classify(GmailMessageMetadata message) {
		String sender = normalize(message.sender());
		String subject = normalize(message.subject());
		String searchableText = sender + " " + subject;

		int positiveScore = 0;
		int negativeScore = 0;
		int employmentSpecificScore = 0;
		boolean applicantTrackingSystem =
				containsAny(sender, APPLICANT_TRACKING_SYSTEMS);
		boolean applicationConfirmation =
				containsAny(subject, APPLICATION_CONFIRMATION_TERMS);

		if (applicantTrackingSystem) {
			positiveScore += 4;
			employmentSpecificScore += 4;
		}
		if (applicationConfirmation) {
			positiveScore += 4;
		}
		if (containsAny(searchableText, INTERVIEW_TERMS)) {
			positiveScore += 4;
			employmentSpecificScore += 4;
		}
		if (containsAny(searchableText, ASSESSMENT_TERMS)) {
			positiveScore += 4;
			employmentSpecificScore += 4;
		}
		if (containsAny(subject, OFFER_TERMS)) {
			positiveScore += 4;
			employmentSpecificScore += 4;
		}
		if (containsAny(subject, REJECTION_TERMS)) {
			positiveScore += 4;
			employmentSpecificScore += 4;
		}
		if (containsAny(searchableText, RECRUITER_TERMS)) {
			positiveScore += 3;
			employmentSpecificScore += 3;
		}
		if (containsAny(searchableText, EMPLOYMENT_CONTEXT_TERMS)) {
			positiveScore += 2;
			employmentSpecificScore += 2;
		}
		if (subject.contains("application")) {
			positiveScore += 1;
		}
		if (containsAny(searchableText, NON_EMPLOYMENT_TERMS)) {
			negativeScore += 6;
		}

		GmailEventType eventType = eventType(subject, searchableText);
		if (negativeScore > 0 && employmentSpecificScore == 0) {
			return new GmailMessageClassification(
					GmailClassificationCategory.NOT_JOB_RELATED,
					GmailEventType.UNKNOWN,
					confidenceScore(negativeScore, positiveScore),
					"Non-employment application terminology was found");
		}
		if ((negativeScore == 0 && positiveScore >= 4)
				|| (employmentSpecificScore >= 8 && positiveScore > negativeScore)) {
			return new GmailMessageClassification(
					GmailClassificationCategory.JOB_RELATED,
					eventType,
					confidenceScore(positiveScore, negativeScore),
					reasonFor(
							eventType,
							applicantTrackingSystem,
							applicationConfirmation));
		}
		return new GmailMessageClassification(
				GmailClassificationCategory.UNCERTAIN,
				eventType,
				Math.min(65, 50 + Math.abs(positiveScore - negativeScore) * 3),
				"Only broad or conflicting application terminology was found");
	}

	private GmailEventType eventType(String subject, String searchableText) {
		if (containsAny(subject, REJECTION_TERMS)) {
			return GmailEventType.REJECTION;
		}
		if (containsAny(subject, OFFER_TERMS)) {
			return GmailEventType.OFFER;
		}
		if (containsAny(searchableText, INTERVIEW_TERMS)) {
			return GmailEventType.INTERVIEW;
		}
		if (containsAny(searchableText, ASSESSMENT_TERMS)) {
			return GmailEventType.ASSESSMENT;
		}
		if (containsAny(subject, APPLICATION_CONFIRMATION_TERMS)
				|| subject.contains("application")) {
			return GmailEventType.APPLICATION;
		}
		if (containsAny(searchableText, RECRUITER_TERMS)) {
			return GmailEventType.RECRUITER_CONTACT;
		}
		return GmailEventType.UNKNOWN;
	}

	private int confidenceScore(int supportingScore, int opposingScore) {
		return Math.min(95, 70 + supportingScore * 4 - opposingScore * 2);
	}

	private String reasonFor(
			GmailEventType eventType,
			boolean applicantTrackingSystem,
			boolean applicationConfirmation) {
		return switch (eventType) {
			case APPLICATION -> applicationConfirmation
					? "Job application confirmation terminology was found"
					: "A known recruiting-system sender and application terminology were found";
			case INTERVIEW -> "Interview terminology was found";
			case ASSESSMENT -> "Candidate assessment terminology was found";
			case OFFER -> "Job offer terminology was found";
			case REJECTION -> "Application rejection terminology was found";
			case RECRUITER_CONTACT -> "Recruiter terminology was found";
			case UNKNOWN -> applicantTrackingSystem
					? "A known recruiting-system sender was found"
					: "Strong employment terminology was found";
		};
	}

	private boolean containsAny(String value, List<String> terms) {
		return terms.stream().anyMatch(value::contains);
	}

	private String normalize(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}
}
