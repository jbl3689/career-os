package com.careeros.api.gmail.persistence;

public enum GmailScanResultStatus {
	PENDING_CLASSIFICATION,
	READY_FOR_MATCHING,
	PENDING_REVIEW,
	IGNORED
}
