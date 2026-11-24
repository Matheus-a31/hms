package com.hms.dto;

import java.time.LocalDateTime;

public record ConsultaFinanceiraDTO(
		long consultaId,
		LocalDateTime dataHora,
		String pacienteNome,
		int valorBruto,
		int custoOperacional,
		int lucroAtendimento) {
}
