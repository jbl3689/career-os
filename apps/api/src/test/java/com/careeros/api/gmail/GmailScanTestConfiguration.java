package com.careeros.api.gmail;

import java.time.Instant;
import java.util.List;

import com.careeros.api.auth.google.GoogleAccessTokenClient;
import com.careeros.api.auth.google.GoogleAccessTokenException;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
class GmailScanTestConfiguration {

	@Bean
	@Primary
	RecordingGoogleAccessTokenClient recordingGoogleAccessTokenClient() {
		return new RecordingGoogleAccessTokenClient();
	}

	@Bean
	@Primary
	RecordingGmailClient recordingGmailClient() {
		return new RecordingGmailClient();
	}

	@Bean
	@Primary
	RecordingGmailMessageClassifier recordingGmailMessageClassifier() {
		return new RecordingGmailMessageClassifier();
	}

	static final class RecordingGoogleAccessTokenClient implements GoogleAccessTokenClient {

		private String receivedRefreshToken;
		private boolean shouldFail;

		@Override
		public String refreshAccessToken(String refreshToken) {
			receivedRefreshToken = refreshToken;
			if (shouldFail) {
				throw new GoogleAccessTokenException("Token refresh failed", null);
			}
			return "temporary-access-token";
		}

		void failNextRequest() {
			shouldFail = true;
		}

		String getReceivedRefreshToken() {
			return receivedRefreshToken;
		}

		void reset() {
			receivedRefreshToken = null;
			shouldFail = false;
		}
	}

	static final class RecordingGmailClient implements GmailClient {

		private String receivedAccessToken;
		private String receivedQuery;
		private int receivedMaximumResults;
		private boolean shouldFail;

		@Override
		public List<GmailMessageMetadata> findCandidateMessages(
				String accessToken,
				String query,
				int maximumResults) {
			receivedAccessToken = accessToken;
			receivedQuery = query;
			receivedMaximumResults = maximumResults;
			if (shouldFail) {
				throw new GmailClientException(
						"Google denied the Gmail scan. Confirm that the Gmail API is enabled in the same Google Cloud project.",
						null);
			}
			return List.of(new GmailMessageMetadata(
					"message-1",
					"thread-1",
					"Recruiter <recruiter@example.com>",
					"Interview invitation",
					Instant.parse("2026-07-19T14:30:00Z")));
		}

		void failNextRequest() {
			shouldFail = true;
		}

		String getReceivedAccessToken() {
			return receivedAccessToken;
		}

		String getReceivedQuery() {
			return receivedQuery;
		}

		int getReceivedMaximumResults() {
			return receivedMaximumResults;
		}

		void reset() {
			receivedAccessToken = null;
			receivedQuery = null;
			receivedMaximumResults = 0;
			shouldFail = false;
		}
	}

	static final class RecordingGmailMessageClassifier implements GmailMessageClassifier {

		private int invocationCount;

		@Override
		public GmailMessageClassification classify(GmailMessageMetadata message) {
			invocationCount++;
			return new GmailMessageClassification(
					GmailClassificationCategory.JOB_RELATED,
					GmailEventType.INTERVIEW,
					90,
					"Interview terminology was found");
		}

		int getInvocationCount() {
			return invocationCount;
		}

		void reset() {
			invocationCount = 0;
		}
	}
}
