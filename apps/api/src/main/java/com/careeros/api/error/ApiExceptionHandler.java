package com.careeros.api.error;

import java.util.LinkedHashMap;
import java.util.Map;

import com.careeros.api.application.JobApplicationNotFoundException;
import com.careeros.api.auth.google.GmailConnectionRequiredException;
import com.careeros.api.gmail.GmailScanFailedException;
import com.careeros.api.gmail.GmailReviewNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(fieldError ->
				fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));

		return ResponseEntity.badRequest()
				.body(new ApiErrorResponse(400, "Validation failed", fieldErrors));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(HttpMessageNotReadableException exception) {
		return ResponseEntity.badRequest()
				.body(new ApiErrorResponse(400, "Request body contains an invalid value", Map.of()));
	}

	@ExceptionHandler(JobApplicationNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(JobApplicationNotFoundException exception) {
		return ResponseEntity.status(404)
				.body(new ApiErrorResponse(404, exception.getMessage(), Map.of()));
	}

	@ExceptionHandler(GmailReviewNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleGmailReviewNotFound(
			GmailReviewNotFoundException exception) {
		return ResponseEntity.status(404)
				.body(new ApiErrorResponse(404, exception.getMessage(), Map.of()));
	}

	@ExceptionHandler(GmailConnectionRequiredException.class)
	public ResponseEntity<ApiErrorResponse> handleGmailConnectionRequired(
			GmailConnectionRequiredException exception) {
		return ResponseEntity.status(409)
				.body(new ApiErrorResponse(409, exception.getMessage(), Map.of()));
	}

	@ExceptionHandler(GmailScanFailedException.class)
	public ResponseEntity<ApiErrorResponse> handleGmailScanFailed(GmailScanFailedException exception) {
		return ResponseEntity.status(502)
				.body(new ApiErrorResponse(502, exception.getMessage(), Map.of()));
	}
}
