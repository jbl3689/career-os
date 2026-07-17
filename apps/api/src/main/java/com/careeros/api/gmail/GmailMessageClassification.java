package com.careeros.api.gmail;

public record GmailMessageClassification(
		GmailClassificationCategory category,
		GmailEventType eventType,
		int confidenceScore,
		String reason) {
}
