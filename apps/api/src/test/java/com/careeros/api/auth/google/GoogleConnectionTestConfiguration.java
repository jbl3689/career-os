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
		private boolean failRevocation;

		@Override
		public void revoke(String refreshToken) {
			this.revokedToken = refreshToken;
			if (failRevocation) {
				throw new IllegalStateException("Google rejected the token revocation");
			}
		}

		String getRevokedToken() {
			return revokedToken;
		}

		void failNextRevocation() {
			failRevocation = true;
		}

		void reset() {
			revokedToken = null;
			failRevocation = false;
		}
	}
}
