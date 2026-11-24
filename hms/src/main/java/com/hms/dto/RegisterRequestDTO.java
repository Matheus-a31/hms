package com.hms.dto;

import com.hms.model.UserRole;

public record RegisterRequestDTO(String name, String email, String password, UserRole role) {
}
