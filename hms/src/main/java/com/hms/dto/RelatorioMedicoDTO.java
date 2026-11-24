package com.hms.dto;

import java.util.List;

public record RelatorioMedicoDTO(
		long medicoId,
		String nomeMedico,
		String especialidade,
		int totalConsultas,
		int receitaBruta,
		int custoOperacional,
		int valorTotalArrecadado,
		int lucroLiquido,
		List<ConsultaFinanceiraDTO> consultas) {
}
