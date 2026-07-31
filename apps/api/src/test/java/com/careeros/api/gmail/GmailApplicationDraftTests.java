package com.careeros.api.gmail;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class GmailApplicationDraftTests {

	@Test
	void proposesCompanyAndRoleFromUsefulMetadata() {
		GmailApplicationDraft draft = GmailApplicationDraft.from(message(
				"Acme Careers <careers@acme.example>",
				"Software Engineer interview",
				"We would like to arrange an interview."));

		assertThat(draft.companyName()).isEqualTo("Acme");
		assertThat(draft.roleTitle()).isEqualTo("Software Engineer");
	}

	@Test
	void leavesGenericCompanyBlankAndFindsRoleInExcerpt() {
		GmailApplicationDraft draft = GmailApplicationDraft.from(message(
				"Recruiter <recruiter@example.com>",
				"Interview invitation",
				"We would like to interview you for the Senior Engineer role."));

		assertThat(draft.companyName()).isEmpty();
		assertThat(draft.roleTitle()).isEqualTo("Senior Engineer");
	}

	private GmailMessageMetadata message(String sender, String subject, String excerpt) {
		return new GmailMessageMetadata(
				"message-1",
				"thread-1",
				sender,
				subject,
				excerpt,
				Instant.parse("2026-07-19T14:30:00Z"));
	}
}
