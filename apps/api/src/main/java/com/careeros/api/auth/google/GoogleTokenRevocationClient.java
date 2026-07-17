package com.careeros.api.auth.google;

public interface GoogleTokenRevocationClient {

	void revoke(String refreshToken);
}
