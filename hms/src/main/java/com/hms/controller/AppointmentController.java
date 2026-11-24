package com.hms.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hms.dto.AppointmentDTO;
import com.hms.dto.AppointmentRequestDTO;
import com.hms.dto.OccupancyDTO;
import com.hms.model.AppointmentStatus;
import com.hms.service.AppointmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/hms/appointments")
@Tag(name = "Agendamentos e Vagas (Solicitação 08)", description = "Agendamento de consultas (RF29) e controle de ocupação de vagas (RF30)")
public class AppointmentController {

	private final AppointmentService appointmentService;

	public AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Listar agendamentos de consultas")
	public ResponseEntity<List<AppointmentDTO>> findAll() {
		return ResponseEntity.ok(appointmentService.findAll());
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'PACIENTE')")
	@Operation(summary = "Agendar consulta com validação de conflito de horário (RF29)")
	public ResponseEntity<AppointmentDTO> create(@RequestBody AppointmentRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'PACIENTE')")
	@Operation(summary = "Atualizar status do agendamento")
	public ResponseEntity<AppointmentDTO> updateStatus(
			@PathVariable("id") long id,
			@RequestParam("status") AppointmentStatus status) {
		return ResponseEntity.ok(appointmentService.updateStatus(id, status));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Remover agendamento")
	public ResponseEntity<Void> delete(@PathVariable("id") long id) {
		appointmentService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/available")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Listar horários livres de um médico em uma data (RF29)")
	public ResponseEntity<List<String>> getAvailableSlots(
			@RequestParam("doctorId") long doctorId,
			@RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, date));
	}

	@GetMapping("/occupancy")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Painel administrativo de ocupação de vagas por médico (RF30)")
	public ResponseEntity<List<OccupancyDTO>> getOccupancyReport(
			@RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return ResponseEntity.ok(appointmentService.getOccupancyReport(date));
	}
}
