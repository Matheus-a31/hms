package com.hms.security;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hms.exception.ForbiddenException;
import com.hms.model.UserAccount;
import com.hms.model.UserRole;
import com.hms.repository.UserAccountRepository;

@Service
public class AuthenticatedUserService {

	private final UserAccountRepository userAccountRepository;

	public AuthenticatedUserService(UserAccountRepository userAccountRepository) {
		this.userAccountRepository = userAccountRepository;
	}

	/** Returns the authenticated UserAccount if present in the security context. */
	public Optional<UserAccount> getCurrentUserOptional() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
			return Optional.empty();
		}
		return userAccountRepository.findByEmailIgnoreCase(auth.getName());
	}

	/** Returns true when the current authenticated user has the PACIENTE role. */
	public boolean isPatientRole() {
		return getCurrentUserOptional()
				.map(user -> user.getRole() == UserRole.PACIENTE)
				.orElse(false);
	}

	/** Returns the linked Patient ID for the current user, or null if not linked. */
	public Long getCurrentPatientId() {
		return getCurrentUserOptional()
				.map(UserAccount::getPatient)
				.map(patient -> patient.getId())
				.orElse(null);
	}

	/** Ensures a PACIENTE user can only access records belonging to their own Patient entity. */
	public void assertCanAccessPatient(Long targetPatientId) {
		Optional<UserAccount> current = getCurrentUserOptional();
		if (current.isEmpty() || current.get().getRole() != UserRole.PACIENTE) {
			return;
		}
		Long ownPatientId = current.get().getPatient() != null ? current.get().getPatient().getId() : null;
		if (ownPatientId == null || targetPatientId == null || !ownPatientId.equals(targetPatientId)) {
			throw new ForbiddenException("Acesso negado: você só pode acessar seus próprios registros.");
		}
	}
}
