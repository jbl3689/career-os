package com.careeros.api.auth.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.SecureRandom;
import java.util.Base64;

import org.junit.jupiter.api.Test;

class TokenEncryptionServiceTests {

	private static final String KEY =
			"MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

	private final TokenEncryptionService encryptionService =
			new TokenEncryptionService(KEY, new SecureRandom());

	@Test
	void encryptsAndDecryptsATokenForItsOwningUser() {
		String encrypted = encryptionService.encrypt("refresh-token-value", 42L);

		assertThat(encrypted)
				.startsWith("v1:")
				.doesNotContain("refresh-token-value");
		assertThat(encryptionService.decrypt(encrypted, 42L))
				.isEqualTo("refresh-token-value");
	}

	@Test
	void willNotDecryptATokenForAnotherUser() {
		String encrypted = encryptionService.encrypt("refresh-token-value", 42L);

		assertThatThrownBy(() -> encryptionService.decrypt(encrypted, 99L))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Could not decrypt the Google refresh token");
	}

	@Test
	void detectsWhenEncryptedDataHasBeenChanged() {
		String encrypted = encryptionService.encrypt("refresh-token-value", 42L);
		byte[] envelope = Base64.getDecoder().decode(encrypted.substring("v1:".length()));
		envelope[envelope.length - 1] ^= 1;
		String tampered = "v1:" + Base64.getEncoder().encodeToString(envelope);

		assertThatThrownBy(() -> encryptionService.decrypt(tampered, 42L))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Could not decrypt the Google refresh token");
	}
}
