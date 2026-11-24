package com.hms.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.dto.AuthRequestDTO;
import com.hms.dto.AuthResponseDTO;
import com.hms.dto.RefreshRequestDTO;
import com.hms.dto.RegisterRequestDTO;
import com.hms.exception.BadRequestException;
import com.hms.exception.ConflictException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.exception.UnauthorizedException;
import com.hms.model.RefreshToken;
import com.hms.model.UserAccount;
import com.hms.model.UserRole;
import com.hms.repository.RefreshTokenRepository;
import com.hms.repository.UserAccountRepository;
import com.hms.security.JwtService;

@Service
public class AuthService {

	private static final String BEARER = "Bearer";

	private final UserAccountRepository userAccountRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserPreferenceService userPreferenceService;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final long refreshExpirationMs;

	public AuthService(
			UserAccountRepository userAccountRepository,
			RefreshTokenRepository refreshTokenRepository,
			UserPreferenceService userPreferenceService,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			@Value("${hms.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
		this.userAccountRepository = userAccountRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.userPreferenceService = userPreferenceService;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshExpirationMs = refreshExpirationMs;
	}

	@Transactional
	public AuthResponseDTO register(RegisterRequestDTO request) {
		if (request == null || isBlank(request.name()) || isBlank(request.email()) || isBlank(request.password())) {
			throw new BadRequestException("Nome, e-mail e senha são obrigatórios.");
		}
		if (request.password().length() < 6) {
			throw new BadRequestException("A senha deve ter no mínimo 6 caracteres.");
		}
		String normalizedEmail = request.email().trim().toLowerCase();
		if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
			throw new ConflictException("Já existe um usuário cadastrado com este e-mail.");
		}

		UserAccount user = new UserAccount();
		user.setName(request.name().trim());
		user.setEmail(normalizedEmail);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setRole(request.role() != null ? request.role() : UserRole.PACIENTE);
		user = userAccountRepository.save(user);
		userPreferenceService.createDefault(user);

		return buildAuthResponse(user);
	}

	@Transactional
	public AuthResponseDTO login(AuthRequestDTO request) {
		if (request == null || isBlank(request.email()) || isBlank(request.password())) {
			throw new BadRequestException("E-mail e senha são obrigatórios.");
		}
		UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.email().trim())
				.orElseThrow(() -> new UnauthorizedException("Credenciais inválidas."));

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new UnauthorizedException("Credenciais inválidas.");
		}
		return buildAuthResponse(user);
	}

	@Transactional
	public AuthResponseDTO refresh(RefreshRequestDTO request) {
		if (request == null || isBlank(request.refreshToken())) {
			throw new UnauthorizedException("Refresh token é obrigatório.");
		}
		RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
				.orElseThrow(() -> new UnauthorizedException("Refresh token inválido."));

		if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
			throw new UnauthorizedException("Refresh token expirado ou revogado.");
		}

		stored.setRevoked(true);
		refreshTokenRepository.save(stored);
		return buildAuthResponse(stored.getUser());
	}

	@Transactional
	public void logout(RefreshRequestDTO request) {
		if (request == null || isBlank(request.refreshToken())) {
			return;
		}
		refreshTokenRepository.findByToken(request.refreshToken()).ifPresent(token -> {
			token.setRevoked(true);
			refreshTokenRepository.save(token);
		});
	}

	public UserAccount findByEmail(String email) {
		return userAccountRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
	}

	private AuthResponseDTO buildAuthResponse(UserAccount user) {
		String accessToken = jwtService.generateAccessToken(user);
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setToken(UUID.randomUUID() + "-" + UUID.randomUUID());
		refreshToken.setUser(user);
		refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
		refreshToken.setRevoked(false);
		refreshTokenRepository.save(refreshToken);

		return new AuthResponseDTO(
				accessToken,
				refreshToken.getToken(),
				BEARER,
				jwtService.getAccessExpirationSeconds(),
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole());
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
