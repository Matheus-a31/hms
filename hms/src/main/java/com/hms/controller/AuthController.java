package com.hms.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hms.dto.AuthRequestDTO;
import com.hms.dto.AuthResponseDTO;
import com.hms.dto.RefreshRequestDTO;
import com.hms.dto.RegisterRequestDTO;
import com.hms.model.UserAccount;
import com.hms.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/hms/auth")
@Tag(name = "Autenticação (T09)", description = "JWT Access Token (15 min) + Refresh Token rotativo")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/csrf")
	@Operation(summary = "Emitir token CSRF stateless assinado para requisições mutáveis")
	public ResponseEntity<Map<String, String>> csrf(CsrfToken token) {
		return ResponseEntity.ok(Map.of(
				"headerName", token.getHeaderName(),
				"token", token.getToken()));
	}

	@PostMapping("/register")
	@Operation(summary = "Cadastrar novo usuário e emitir Access Token (15 min) + Refresh Token")
	public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	@PostMapping("/login")
	@Operation(summary = "Autenticar usuário e emitir Access Token (15 min) + Refresh Token")
	public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/refresh")
	@Operation(summary = "Renovar Access Token (15 min) usando Refresh Token válido")
	public ResponseEntity<AuthResponseDTO> refresh(@RequestBody RefreshRequestDTO request) {
		return ResponseEntity.ok(authService.refresh(request));
	}

	@PostMapping("/logout")
	@Operation(summary = "Revogar Refresh Token atual")
	public ResponseEntity<Void> logout(@RequestBody RefreshRequestDTO request) {
		authService.logout(request);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	@Operation(summary = "Obter perfil do usuário autenticado")
	public ResponseEntity<AuthResponseDTO> me(Authentication authentication) {
		UserAccount user = authService.findByEmail(authentication.getName());
		return ResponseEntity.ok(new AuthResponseDTO(
				null, null, "Bearer", 900L, user.getId(), user.getName(), user.getEmail(), user.getRole()));
	}
}
