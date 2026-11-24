package com.hms.dto;

import java.util.List;

public record DashboardSummaryDTO(
		int receitaTotal,
		int despesasTotal,
		int lucroLiquido,
		double margemLucro,
		int ticketMedio,
		int faturasPendentes,
		int custoMedioLeito,
		String statusFinanceiro,
		List<MonthlyMetricDTO> evolucaoMensal,
		List<RevenueSourceDTO> origemReceita) {

	public record MonthlyMetricDTO(String mes, int receita, int despesas, int lucro) {
	}

	public record RevenueSourceDTO(String categoria, int percentual) {
	}
}
