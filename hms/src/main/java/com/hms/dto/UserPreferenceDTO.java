package com.hms.dto;

import com.hms.model.UserPreference;

public record UserPreferenceDTO(
		long usuarioId,
		boolean altoContraste,
		String tamanhoFonte,
		boolean focoAcentuado) {

	public static UserPreferenceDTO fromEntity(UserPreference pref) {
		return new UserPreferenceDTO(
				pref.getUser().getId(),
				pref.isHighContrast(),
				pref.getFontSize(),
				pref.isEnhancedFocus());
	}
}
