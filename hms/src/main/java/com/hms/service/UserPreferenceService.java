package com.hms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.dto.UserPreferenceDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.model.UserAccount;
import com.hms.model.UserPreference;
import com.hms.repository.UserAccountRepository;
import com.hms.repository.UserPreferenceRepository;

@Service
public class UserPreferenceService {

	private final UserAccountRepository userAccountRepository;
	private final UserPreferenceRepository userPreferenceRepository;

	public UserPreferenceService(
			UserAccountRepository userAccountRepository,
			UserPreferenceRepository userPreferenceRepository) {
		this.userAccountRepository = userAccountRepository;
		this.userPreferenceRepository = userPreferenceRepository;
	}

	@Transactional
	public UserPreferenceDTO getPreferences(String email) {
		UserAccount user = findUser(email);
		UserPreference preference = userPreferenceRepository.findByUserId(user.getId())
				.orElseGet(() -> saveDefault(user));
		return UserPreferenceDTO.fromEntity(preference);
	}

	@Transactional
	public UserPreferenceDTO updatePreferences(String email, UserPreferenceDTO dto) {
		UserAccount user = findUser(email);
		UserPreference preference = userPreferenceRepository.findByUserId(user.getId())
				.orElseGet(() -> saveDefault(user));
		preference.setHighContrast(dto.altoContraste());
		preference.setFontSize("large".equalsIgnoreCase(dto.tamanhoFonte()) ? "large" : "normal");
		preference.setEnhancedFocus(dto.focoAcentuado());
		return UserPreferenceDTO.fromEntity(userPreferenceRepository.save(preference));
	}

	@Transactional
	public UserPreference createDefault(UserAccount user) {
		return saveDefault(user);
	}

	private UserPreference saveDefault(UserAccount user) {
		UserPreference created = new UserPreference();
		created.setUser(user);
		created.setHighContrast(false);
		created.setFontSize("normal");
		created.setEnhancedFocus(true);
		return userPreferenceRepository.save(created);
	}

	private UserAccount findUser(String email) {
		return userAccountRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
	}
}
