package com.hms.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hms.dto.ConsultaFinanceiraDTO;
import com.hms.dto.DashboardSummaryDTO;
import com.hms.dto.RelatorioMedicoDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.model.Appointment;
import com.hms.model.AppointmentStatus;
import com.hms.model.Doctor;
import com.hms.model.Invoice;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.InvoiceRepository;
import com.hms.repository.PatientRepository;

@Service
public class FinancialReportService {

	private static final double OPERATIONAL_COST_RATE = 0.25;
	private static final double HOSPITAL_EXPENSE_RATE = 0.30;

	private final DoctorRepository doctorRepository;
	private final PatientRepository patientRepository;
	private final InvoiceRepository invoiceRepository;
	private final AppointmentRepository appointmentRepository;

	public FinancialReportService(
			DoctorRepository doctorRepository,
			PatientRepository patientRepository,
			InvoiceRepository invoiceRepository,
			AppointmentRepository appointmentRepository) {
		this.doctorRepository = doctorRepository;
		this.patientRepository = patientRepository;
		this.invoiceRepository = invoiceRepository;
		this.appointmentRepository = appointmentRepository;
	}

	public RelatorioMedicoDTO getDoctorReport(long doctorId) {
		Doctor doctor = doctorRepository.findById(doctorId)
				.orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado: " + doctorId));
		return buildDoctorReport(doctor);
	}

	public List<RelatorioMedicoDTO> getAllDoctorsReport() {
		return doctorRepository.findAll().stream()
				.map(this::buildDoctorReport)
				.toList();
	}

	public DashboardSummaryDTO getDashboardSummary() {
		List<Invoice> invoices = invoiceRepository.findAll();
		int invoiceRevenue = invoices.stream().mapToInt(Invoice::getTotalAmount).sum();
		int consultationRevenue = appointmentRepository.findAll().stream()
				.filter(a -> a.getStatus() != AppointmentStatus.CANCELADO)
				.mapToInt(Appointment::getConsultationFee)
				.sum();

		int receitaTotal = invoiceRevenue + consultationRevenue;
		int despesasTotal = (int) Math.round(receitaTotal * HOSPITAL_EXPENSE_RATE);
		int lucroLiquido = receitaTotal - despesasTotal;
		double margemLucro = receitaTotal > 0 ? Math.round((lucroLiquido * 1000.0) / receitaTotal) / 10.0 : 0.0;
		int ticketMedio = (int) (receitaTotal / Math.max(1L, patientRepository.count()));
		int faturasPendentes = invoices.stream()
				.filter(i -> !"PAGO".equalsIgnoreCase(i.getPaymentStatus()))
				.mapToInt(Invoice::getTotalAmount)
				.sum();

		return new DashboardSummaryDTO(
				receitaTotal,
				despesasTotal,
				lucroLiquido,
				margemLucro,
				ticketMedio,
				faturasPendentes,
				420,
				margemLucro >= 50.0 ? "Saudável" : "Atenção",
				buildMonthlyEvolution(receitaTotal, despesasTotal, lucroLiquido),
				buildRevenueSources(invoices, invoiceRevenue, receitaTotal));
	}

	private RelatorioMedicoDTO buildDoctorReport(Doctor doctor) {
		List<Appointment> active = appointmentRepository.findByDoctorId(doctor.getId()).stream()
				.filter(a -> a.getStatus() != AppointmentStatus.CANCELADO)
				.toList();
		Long invoiceSum = invoiceRepository.sumTotalAmountByDoctorId(doctor.getId());
		int invoiceTotal = invoiceSum != null ? invoiceSum.intValue() : 0;

		List<ConsultaFinanceiraDTO> details = new ArrayList<>();
		int appointmentsRevenue = 0;
		for (Appointment app : active) {
			int fee = app.getConsultationFee();
			int cost = (int) Math.round(fee * OPERATIONAL_COST_RATE);
			appointmentsRevenue += fee;
			details.add(new ConsultaFinanceiraDTO(
					app.getId(),
					app.getAppointmentDate(),
					app.getPatient() != null ? app.getPatient().getName() : "N/A",
					fee,
					cost,
					fee - cost));
		}

		int receitaBruta = appointmentsRevenue + invoiceTotal;
		int custoOperacional = (int) Math.round(receitaBruta * OPERATIONAL_COST_RATE);
		return new RelatorioMedicoDTO(
				doctor.getId(),
				doctor.getName(),
				doctor.getDesignation(),
				active.size(),
				receitaBruta,
				custoOperacional,
				receitaBruta,
				receitaBruta - custoOperacional,
				details);
	}

	private List<DashboardSummaryDTO.MonthlyMetricDTO> buildMonthlyEvolution(int receita, int despesas, int lucro) {
		return List.of(
				new DashboardSummaryDTO.MonthlyMetricDTO("Abr", (int) (receita * 0.72), (int) (despesas * 0.80), (int) (lucro * 0.68)),
				new DashboardSummaryDTO.MonthlyMetricDTO("Mai", (int) (receita * 0.82), (int) (despesas * 0.90), (int) (lucro * 0.79)),
				new DashboardSummaryDTO.MonthlyMetricDTO("Jun", (int) (receita * 0.76), (int) (despesas * 0.74), (int) (lucro * 0.77)),
				new DashboardSummaryDTO.MonthlyMetricDTO("Jul", (int) (receita * 0.90), (int) (despesas * 0.85), (int) (lucro * 0.92)),
				new DashboardSummaryDTO.MonthlyMetricDTO("Ago", (int) (receita * 0.95), (int) (despesas * 0.82), (int) (lucro * 0.97)),
				new DashboardSummaryDTO.MonthlyMetricDTO("Set", receita, despesas, lucro));
	}

	private List<DashboardSummaryDTO.RevenueSourceDTO> buildRevenueSources(
			List<Invoice> invoices, int invoiceRevenue, int totalRevenue) {
		int utiTotal = invoices.stream()
				.filter(i -> i.getBill() != null && i.getBill().getParticular().toLowerCase().contains("uti"))
				.mapToInt(Invoice::getTotalAmount)
				.sum();
		int examsTotal = Math.max(0, invoiceRevenue - utiTotal);
		int denominator = Math.max(1, totalRevenue);
		int pctUti = (int) Math.round((utiTotal * 100.0) / denominator);
		int pctExams = (int) Math.round((examsTotal * 100.0) / denominator);
		int pctConsultas = Math.max(0, 100 - pctUti - pctExams);
		return List.of(
				new DashboardSummaryDTO.RevenueSourceDTO("Diárias & UTI (Bill)", pctUti),
				new DashboardSummaryDTO.RevenueSourceDTO("Exames & Procedimentos", pctExams),
				new DashboardSummaryDTO.RevenueSourceDTO("Consultas Médicas", pctConsultas));
	}
}
