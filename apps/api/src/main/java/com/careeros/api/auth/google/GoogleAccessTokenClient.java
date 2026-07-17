package com.careeros.api.auth.google;

public interface GoogleAccessTokenClient {

	String refreshAccessToken(String refreshToken);
}
