package com.careeros.api.gmail;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpGmailClientTests {

	@Test
	void retrievesEveryPageOfMatchingMessages() {
		RestClient.Builder restClientBuilder = RestClient.builder()
				.baseUrl("https://gmail.googleapis.com");
		MockRestServiceServer server = MockRestServiceServer
				.bindTo(restClientBuilder)
				.build();
		HttpGmailClient client = new HttpGmailClient(restClientBuilder.build());

		server.expect(request -> assertListRequest(
				request.getURI(),
				"newer_than:3m subject:interview",
				"500",
				null))
				.andRespond(withSuccess("""
						{
						  "messages": [{"id": "message-1", "threadId": "thread-1"}],
						  "nextPageToken": "next-page"
						}
						""", MediaType.APPLICATION_JSON));
		server.expect(request -> assertThat(request.getURI().getPath())
				.isEqualTo("/gmail/v1/users/me/messages/message-1"))
				.andRespond(withSuccess(messageResponse(
						"message-1",
						"thread-1",
						"First interview"), MediaType.APPLICATION_JSON));
		server.expect(request -> assertListRequest(
				request.getURI(),
				"newer_than:3m subject:interview",
				"500",
				"next-page"))
				.andRespond(withSuccess("""
						{
						  "messages": [{"id": "message-2", "threadId": "thread-2"}]
						}
						""", MediaType.APPLICATION_JSON));
		server.expect(request -> assertThat(request.getURI().getPath())
				.isEqualTo("/gmail/v1/users/me/messages/message-2"))
				.andRespond(withSuccess(messageResponse(
						"message-2",
						"thread-2",
						"Second interview"), MediaType.APPLICATION_JSON));

		var messages = client.findCandidateMessages(
				"temporary-token",
				"newer_than:3m subject:interview",
				500);

		assertThat(messages)
				.extracting(GmailMessageMetadata::gmailMessageId)
				.containsExactly("message-1", "message-2");
		server.verify();
	}

	private void assertListRequest(
			URI uri,
			String query,
			String resultsPerPage,
			String pageToken) {
		var queryParams = UriComponentsBuilder.fromUri(uri)
				.build()
				.getQueryParams();
		assertThat(uri.getPath()).isEqualTo("/gmail/v1/users/me/messages");
		assertThat(UriUtils.decode(
				queryParams.getFirst("q"),
				StandardCharsets.UTF_8)).isEqualTo(query);
		assertThat(queryParams.getFirst("maxResults")).isEqualTo(resultsPerPage);
		assertThat(queryParams.getFirst("pageToken")).isEqualTo(pageToken);
	}

	private String messageResponse(String messageId, String threadId, String subject) {
		return """
				{
				  "id": "%s",
				  "threadId": "%s",
				  "internalDate": "1784467800000",
				  "snippet": "We would like to invite you to interview.",
				  "payload": {
				    "headers": [
				      {"name": "From", "value": "Recruiter <recruiter@example.com>"},
				      {"name": "Subject", "value": "%s"}
				    ]
				  }
				}
				""".formatted(messageId, threadId, subject);
	}
}
