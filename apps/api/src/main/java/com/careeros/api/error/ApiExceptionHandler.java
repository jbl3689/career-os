package com.careeros.api.error;

import java.util.LinkedHashMap;
import java.util.Map;

import com.careeros.api.application.JobApplicationNotFoundException;
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
}
