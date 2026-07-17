package com.careeros.api.gmail;

import java.util.List;

public interface GmailClient {

	List<GmailMessageMetadata> findCandidateMessages(
			String accessToken,
			String query,
			int maximumResults);
}
