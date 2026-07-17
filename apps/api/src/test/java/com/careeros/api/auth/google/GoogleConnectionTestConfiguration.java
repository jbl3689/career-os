package com.careeros.api.auth.google;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
class GoogleConnectionTestConfiguration {

	@Bean
	@Primary
	RecordingGoogleTokenRevocationClient recordingGoogleTokenRevocationClient() {
		return new RecordingGoogleTokenRevocationClient();
	}

	static final class RecordingGoogleTokenRevocationClient implements GoogleTokenRevocationClient {

		private String revokedToken;

		@Override
		public void revoke(String refreshToken) {
			this.revokedToken = refreshToken;
		}

		String getRevokedToken() {
			return revokedToken;
		}

		void reset() {
			revokedToken = null;
		}
	}
}
