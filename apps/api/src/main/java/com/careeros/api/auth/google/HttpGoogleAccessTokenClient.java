package com.careeros.api.auth.google;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpGoogleAccessTokenClient implements GoogleAccessTokenClient {

	private final RestClient restClient;
	private final String clientId;
	private final String clientSecret;

	public HttpGoogleAccessTokenClient(
			@Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId,
			@Value("${spring.security.oauth2.client.registration.google.client-secret}") String clientSecret) {
		this.restClient = RestClient.builder()
				.baseUrl("https://oauth2.googleapis.com")
				.build();
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	@Override
	public String refreshAccessToken(String refreshToken) {
		String body = "client_id=" + encode(clientId)
				+ "&client_secret=" + encode(clientSecret)
				+ "&refresh_token=" + encode(refreshToken)
				+ "&grant_type=refresh_token";

		try {
			TokenResponse response = restClient.post()
					.uri("/token")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(body)
					.retrieve()
					.body(TokenResponse.class);

			if (response == null || response.access_token() == null) {
				throw new GoogleAccessTokenException(
						"Google returned no access token",
						null);
			}
			return response.access_token();
		}
		catch (RestClientException exception) {
			throw new GoogleAccessTokenException(
					"Google could not refresh Gmail access",
					exception);
		}
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private record TokenResponse(String access_token) {
	}
}
