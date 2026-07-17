package com.careeros.api.auth.google;

public class GmailConnectionRequiredException extends RuntimeException {

	public GmailConnectionRequiredException() {
		super("Connect Gmail before starting a scan");
	}
}
