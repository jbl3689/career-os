package com.careeros.api.gmail;

import java.time.Instant;

public record GmailMessageMetadata(
		String gmailMessageId,
		String gmailThreadId,
		String sender,
		String subject,
		Instant receivedAt) {
}
