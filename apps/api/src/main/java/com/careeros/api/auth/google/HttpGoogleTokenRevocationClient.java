package com.careeros.api.auth.google;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpGoogleTokenRevocationClient implements GoogleTokenRevocationClient {

	private final RestClient restClient;

	public HttpGoogleTokenRevocationClient() {
		this.restClient = RestClient.builder()
				.baseUrl("https://oauth2.googleapis.com")
				.build();
	}

	@Override
	public void revoke(String refreshToken) {
		String body = "token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

		restClient.post()
				.uri("/revoke")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(body)
				.retrieve()
				.toBodilessEntity();
	}
}
