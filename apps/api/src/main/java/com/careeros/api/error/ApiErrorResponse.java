package com.careeros.api.error;

import java.util.Map;

public record ApiErrorResponse(
		int status,
		String error,
		Map<String, String> fieldErrors) {
}
