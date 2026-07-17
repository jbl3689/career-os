package com.careeros.api.gmail;

public class GmailScanFailedException extends RuntimeException {

	public GmailScanFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
