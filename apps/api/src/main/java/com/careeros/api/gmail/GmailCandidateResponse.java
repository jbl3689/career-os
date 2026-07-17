package com.careeros.api.gmail;

import java.time.Instant;

public record GmailCandidateResponse(
		String gmailMessageId,
		String gmailThreadId,
		String sender,
		String subject,
		Instant receivedAt,
		boolean newlyDiscovered,
		GmailClassificationCategory classification,
		GmailEventType eventType,
		int confidenceScore,
		String classificationReason) {

	static GmailCandidateResponse from(
			GmailMessageMetadata message,
			boolean newlyDiscovered,
			GmailMessageClassification classification) {
		return new GmailCandidateResponse(
				message.gmailMessageId(),
				message.gmailThreadId(),
				message.sender(),
				message.subject(),
				message.receivedAt(),
				newlyDiscovered,
				classification.category(),
				classification.eventType(),
				classification.confidenceScore(),
				classification.reason());
	}
}
