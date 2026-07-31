package com.careeros.api.gmail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record GmailApplicationDraft(
		String companyName,
		String roleTitle) {

	private static final Pattern ROLE_BEFORE_EVENT = Pattern.compile(
			"(?i)^(.{2,100}?)\\s+(?:application|interview|assessment|offer|rejection)\\b");
	private static final Pattern ROLE_AFTER_FOR = Pattern.compile(
			"(?i)\\bfor\\s+(?:the\\s+)?(.{2,100}?)\\s+(?:role|position|vacancy)\\b");
	private static final Pattern COMPANY_SUFFIX = Pattern.compile(
			"(?i)\\s+(?:careers?|recruiting|recruitment|talent(?: acquisition)?(?: team)?|jobs?)$");
	private static final Pattern GENERIC_SENDER = Pattern.compile(
			"(?i)^(?:recruiter|recruiting|recruitment|careers?|talent|jobs?|no[ -]?reply)$");

	public static GmailApplicationDraft from(GmailMessageMetadata message) {
		return new GmailApplicationDraft(
				companyFrom(message.sender()),
				roleFrom(message.subject(), message.excerpt()));
	}

	private static String companyFrom(String sender) {
		String displayName = sender.replaceFirst("\\s*<.*$", "")
				.replace("\"", "")
				.trim();
		displayName = COMPANY_SUFFIX.matcher(displayName).replaceFirst("").trim();
		return displayName.contains("@") || GENERIC_SENDER.matcher(displayName).matches()
				? ""
				: displayName;
	}

	private static String roleFrom(String subject, String excerpt) {
		String normalizedSubject = subject.replaceFirst("(?i)^(?:re|fw|fwd):\\s*", "").trim();
		Matcher subjectMatch = ROLE_BEFORE_EVENT.matcher(normalizedSubject);
		if (subjectMatch.find()) {
			return cleanRole(subjectMatch.group(1));
		}

		Matcher excerptMatch = ROLE_AFTER_FOR.matcher(excerpt);
		return excerptMatch.find() ? cleanRole(excerptMatch.group(1)) : "";
	}

	private static String cleanRole(String role) {
		String cleaned = role.replaceFirst("(?i)^(?:your|the)\\s+", "").trim();
		return cleaned.matches("(?i)^(?:a|the|this|your)$") ? "" : cleaned;
	}
}
