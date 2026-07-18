package com.careeros.api.gmail;

public class GmailReviewNotFoundException extends RuntimeException {

	public GmailReviewNotFoundException(long reviewId) {
		super("Gmail review result " + reviewId + " was not found");
	}
}
