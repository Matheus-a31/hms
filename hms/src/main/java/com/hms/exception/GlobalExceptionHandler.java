package com.hms.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hms.dto.ApiErrorDTO;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorDTO> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiErrorDTO> handleConflict(ConflictException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiErrorDTO> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiErrorDTO> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
	}

	private ResponseEntity<ApiErrorDTO> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
		ApiErrorDTO error = new ApiErrorDTO(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI());
		return ResponseEntity.status(status).body(error);
	}
}
