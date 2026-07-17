package com.careeros.api.auth.google;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenEncryptionService {

	private static final String ENVELOPE_VERSION = "v1";
	private static final int KEY_LENGTH_BYTES = 32;
	private static final int NONCE_LENGTH_BYTES = 12;
	private static final int TAG_LENGTH_BITS = 128;

	private final SecretKey key;
	private final SecureRandom secureRandom;

	@Autowired
	public TokenEncryptionService(@Value("${career-os.token-encryption-key}") String encodedKey) {
		this(encodedKey, new SecureRandom());
	}

	TokenEncryptionService(String encodedKey, SecureRandom secureRandom) {
		byte[] keyBytes;
		try {
			keyBytes = Base64.getDecoder().decode(encodedKey);
		}
		catch (IllegalArgumentException exception) {
			throw new IllegalStateException("TOKEN_ENCRYPTION_KEY must be valid Base64", exception);
		}

		if (keyBytes.length != KEY_LENGTH_BYTES) {
			throw new IllegalStateException("TOKEN_ENCRYPTION_KEY must decode to exactly 32 bytes");
		}

		this.key = new SecretKeySpec(keyBytes, "AES");
		this.secureRandom = secureRandom;
	}

	public String encrypt(String plaintext, Long userId) {
		byte[] nonce = new byte[NONCE_LENGTH_BYTES];
		secureRandom.nextBytes(nonce);

		try {
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
			cipher.updateAAD(userId.toString().getBytes(StandardCharsets.UTF_8));
			byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
			byte[] envelope = ByteBuffer.allocate(nonce.length + ciphertext.length)
					.put(nonce)
					.put(ciphertext)
					.array();
			return ENVELOPE_VERSION + ":" + Base64.getEncoder().encodeToString(envelope);
		}
		catch (GeneralSecurityException exception) {
			throw new IllegalStateException("Could not encrypt the Google refresh token", exception);
		}
	}

	public String decrypt(String encryptedValue, Long userId) {
		String prefix = ENVELOPE_VERSION + ":";
		if (!encryptedValue.startsWith(prefix)) {
			throw new IllegalStateException("Unsupported encrypted token version");
		}

		try {
			byte[] envelope = Base64.getDecoder().decode(encryptedValue.substring(prefix.length()));
			if (envelope.length <= NONCE_LENGTH_BYTES) {
				throw new IllegalStateException("Encrypted token is malformed");
			}

			byte[] nonce = new byte[NONCE_LENGTH_BYTES];
			byte[] ciphertext = new byte[envelope.length - NONCE_LENGTH_BYTES];
			ByteBuffer.wrap(envelope).get(nonce).get(ciphertext);

			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
			cipher.updateAAD(userId.toString().getBytes(StandardCharsets.UTF_8));
			return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
		}
		catch (GeneralSecurityException | IllegalArgumentException exception) {
			throw new IllegalStateException("Could not decrypt the Google refresh token", exception);
		}
	}
}
