package com.hms.dto;

import com.hms.model.UserRole;

public record AuthResponseDTO(
		String accessToken,
		String refreshToken,
		String tokenType,
		long expiresInSeconds,
		long userId,
		String name,
		String email,
		UserRole role) {
}
