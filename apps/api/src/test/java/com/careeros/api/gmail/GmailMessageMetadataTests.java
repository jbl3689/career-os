package com.careeros.api.gmail;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class GmailMessageMetadataTests {

	@Test
	void normalizesAndLimitsTheStoredExcerpt() {
		String longExcerpt = "  rejection\n\tupdate  "
				+ "x".repeat(GmailMessageMetadata.MAXIMUM_EXCERPT_LENGTH);

		GmailMessageMetadata message = new GmailMessageMetadata(
				"message-1",
				"thread-1",
				"sender@example.com",
				"Application update",
				longExcerpt,
				Instant.parse("2026-07-19T14:30:00Z"));

		assertThat(message.excerpt())
				.startsWith("rejection update ")
				.hasSize(GmailMessageMetadata.MAXIMUM_EXCERPT_LENGTH)
				.doesNotContain("\n", "\t");
	}
}
