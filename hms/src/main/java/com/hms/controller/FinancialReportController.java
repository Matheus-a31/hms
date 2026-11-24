package com.hms.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hms.dto.DashboardSummaryDTO;
import com.hms.dto.RelatorioMedicoDTO;
import com.hms.service.FinancialReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/hms")
@Tag(name = "Relatórios Financeiros (Solicitação 11)", description = "Lucro por médico (RF31) e Dashboard Financeiro")
public class FinancialReportController {

	private final FinancialReportService financialReportService;

	public FinancialReportController(FinancialReportService financialReportService) {
		this.financialReportService = financialReportService;
	}

	@GetMapping("/doctors/financial-report")
	@Operation(summary = "Relatório financeiro e lucro consolidado de todos os médicos (RF31)")
	public ResponseEntity<List<RelatorioMedicoDTO>> getAllDoctorsReport() {
		return ResponseEntity.ok(financialReportService.getAllDoctorsReport());
	}

	@GetMapping("/doctors/{id}/financial-report")
	@Operation(summary = "Relatório financeiro e histórico de consultas por médico (RF31)")
	public ResponseEntity<RelatorioMedicoDTO> getDoctorReport(@PathVariable("id") long id) {
		return ResponseEntity.ok(financialReportService.getDoctorReport(id));
	}

	@GetMapping("/dashboard/summary")
	@Operation(summary = "Indicadores consolidados do Dashboard Financeiro & Lucros")
	public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
		return ResponseEntity.ok(financialReportService.getDashboardSummary());
	}
}
