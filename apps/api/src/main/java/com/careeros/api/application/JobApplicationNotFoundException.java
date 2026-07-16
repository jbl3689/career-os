package com.careeros.api.application;

public class JobApplicationNotFoundException extends RuntimeException {

	public JobApplicationNotFoundException(long id) {
		super("Job application " + id + " was not found");
	}
}
