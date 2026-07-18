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
		String classificationReason,
		long reviewId,
		GmailReviewStatus reviewStatus,
		ApplicationMatchSuggestion suggestedApplication,
		Long selectedApplicationId) {

	static GmailCandidateResponse from(
			GmailMessageMetadata message,
			boolean newlyDiscovered,
			com.careeros.api.gmail.persistence.GmailScanResultEntity scanResult) {
		GmailMessageClassification classification = scanResult.classificationResult();
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
				classification.reason(),
				scanResult.getId(),
				scanResult.getReviewStatus(),
				scanResult.matchSuggestion(),
				scanResult.getSelectedApplicationId());
	}

	static GmailCandidateResponse from(
			com.careeros.api.gmail.persistence.GmailScanResultEntity scanResult) {
		var message = scanResult.getEmailMessage();
		return from(new GmailMessageMetadata(
				message.getGmailMessageId(),
				message.getGmailThreadId(),
				message.getSender(),
				message.getSubject(),
				message.getReceivedAt()), false, scanResult);
	}
}
