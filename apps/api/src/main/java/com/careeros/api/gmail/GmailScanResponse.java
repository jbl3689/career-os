package com.careeros.api.gmail;

import java.time.Instant;
import java.util.List;

public record GmailScanResponse(
		Instant scannedAt,
		int candidatesFound,
		List<GmailMessageMetadata> candidates) {
}
