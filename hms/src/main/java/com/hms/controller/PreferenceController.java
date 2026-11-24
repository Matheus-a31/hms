package com.hms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hms.dto.UserPreferenceDTO;
import com.hms.service.UserPreferenceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/hms/preferences")
@Tag(name = "Acessibilidade (Solicitação 14)", description = "Preferências de Alto Contraste, Fonte e Foco por Teclado (RF32)")
public class PreferenceController {

	private final UserPreferenceService preferenceService;

	public PreferenceController(UserPreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}

	@GetMapping
	@Operation(summary = "Obter preferências de acessibilidade (RF32)")
	public ResponseEntity<UserPreferenceDTO> getPreferences(Authentication authentication) {
		return ResponseEntity.ok(preferenceService.getPreferences(authentication.getName()));
	}

	@PutMapping
	@Operation(summary = "Salvar preferências de acessibilidade (RF32)")
	public ResponseEntity<UserPreferenceDTO> updatePreferences(
			Authentication authentication,
			@RequestBody UserPreferenceDTO preferenceDTO) {
		return ResponseEntity.ok(preferenceService.updatePreferences(authentication.getName(), preferenceDTO));
	}
}
