package com.careeros.api.gmail;

import java.time.Instant;

public record GmailMessageMetadata(
		String gmailMessageId,
		String gmailThreadId,
		String sender,
		String subject,
		String excerpt,
		Instant receivedAt) {

	public static final int MAXIMUM_EXCERPT_LENGTH = 500;

	public GmailMessageMetadata {
		excerpt = normalizeExcerpt(excerpt);
	}

	private static String normalizeExcerpt(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}

		String normalized = value.replaceAll("\\s+", " ").trim();
		return normalized.length() <= MAXIMUM_EXCERPT_LENGTH
				? normalized
				: normalized.substring(0, MAXIMUM_EXCERPT_LENGTH);
	}
}
