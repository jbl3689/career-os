package com.careeros.api.auth.google;

import java.time.Instant;

public record GoogleConnectionResponse(
		boolean connected,
		String gmailAddress,
		Instant connectedAt) {

	static GoogleConnectionResponse disconnected() {
		return new GoogleConnectionResponse(false, null, null);
	}

	static GoogleConnectionResponse from(GoogleConnectionEntity connection) {
		return new GoogleConnectionResponse(
				true,
				connection.getGmailAddress(),
				connection.getConnectedAt());
	}
}
