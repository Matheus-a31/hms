package com.hms.dto;

import java.util.List;

public record OccupancyDTO(
		long medicoId,
		String nomeMedico,
		String especialidade,
		int totalVagas,
		int vagasOcupadas,
		int vagasDisponiveis,
		List<String> horariosOcupados) {
}
