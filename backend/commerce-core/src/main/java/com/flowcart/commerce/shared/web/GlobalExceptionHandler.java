package com.flowcart.commerce.shared.web;

import com.flowcart.commerce.shared.domain.ConflictException;
import com.flowcart.commerce.shared.domain.DomainException;
import com.flowcart.commerce.shared.domain.InvalidStateTransitionException;
import com.flowcart.commerce.shared.domain.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), List.of());
	}

	@ExceptionHandler({InvalidStateTransitionException.class, ConflictException.class})
	public ResponseEntity<ApiError> handleConflict(DomainException ex, HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), List.of());
	}

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ApiError> handleDomain(DomainException ex, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), List.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.toList();
		return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), details);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request.getRequestURI(),
				List.of(ex.getClass().getSimpleName() + ": " + ex.getMessage()));
	}

	private ResponseEntity<ApiError> build(HttpStatus status, String message, String path, List<String> details) {
		ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path, details);
		return ResponseEntity.status(status).body(body);
	}
}
