package com.careeros.api.gmail;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class HttpGmailClient implements GmailClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpGmailClient.class);

	private final RestClient restClient;

	public HttpGmailClient() {
		this.restClient = RestClient.builder()
				.baseUrl("https://gmail.googleapis.com")
				.build();
	}

	@Override
	public List<GmailMessageMetadata> findCandidateMessages(
			String accessToken,
			String query,
			int maximumResults) {
		try {
			MessageListResponse response = restClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/gmail/v1/users/me/messages")
							.queryParam("q", query)
							.queryParam("maxResults", maximumResults)
							.build())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.retrieve()
					.body(MessageListResponse.class);

			if (response == null || response.messages() == null) {
				return List.of();
			}

			return response.messages().stream()
					.map(message -> getMetadata(accessToken, message.id()))
					.toList();
		}
		catch (RestClientResponseException exception) {
			LOGGER.warn(
					"Gmail API request failed with status {}: {}",
					exception.getStatusCode().value(),
					exception.getResponseBodyAsString());
			throw new GmailClientException(
					messageFor(exception.getStatusCode()),
					exception);
		}
		catch (RestClientException exception) {
			throw new GmailClientException(
					"Google could not complete the Gmail scan",
					exception);
		}
	}

	private String messageFor(HttpStatusCode status) {
		return switch (status.value()) {
			case 401 -> "Google rejected the Gmail authorization. Disconnect and reconnect Gmail, then try again.";
			case 403 -> "Google denied the Gmail scan. Confirm that the Gmail API is enabled in the same Google Cloud project.";
			case 429 -> "Gmail's request limit was reached. Wait a moment and try again.";
			default -> status.is5xxServerError()
					? "Gmail is temporarily unavailable. Please try again."
					: "Google could not complete the Gmail scan";
		};
	}

	private GmailMessageMetadata getMetadata(String accessToken, String messageId) {
		MessageResponse message = restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/gmail/v1/users/me/messages/{messageId}")
						.queryParam("format", "metadata")
						.queryParam("metadataHeaders", "From", "Subject")
						.build(messageId))
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.retrieve()
				.body(MessageResponse.class);

		if (message == null) {
			throw new GmailClientException("Google returned an empty Gmail message", null);
		}

		return new GmailMessageMetadata(
				message.id(),
				message.threadId(),
				header(message, "From"),
				header(message, "Subject"),
				Instant.ofEpochMilli(Long.parseLong(message.internalDate())));
	}

	private String header(MessageResponse message, String name) {
		if (message.payload() == null || message.payload().headers() == null) {
			return "";
		}

		return Arrays.stream(message.payload().headers())
				.filter(header -> name.equalsIgnoreCase(header.name()))
				.map(HeaderResponse::value)
				.findFirst()
				.orElse("");
	}

	private record MessageListResponse(List<MessageReference> messages) {
	}

	private record MessageReference(String id, String threadId) {
	}

	private record MessageResponse(
			String id,
			String threadId,
			String internalDate,
			MessagePayload payload) {
	}

	private record MessagePayload(HeaderResponse[] headers) {
	}

	private record HeaderResponse(String name, String value) {
	}
}
