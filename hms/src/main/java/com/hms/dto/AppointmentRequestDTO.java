package com.hms.dto;

import java.time.LocalDateTime;

public record AppointmentRequestDTO(
		Long pacienteId,
		Long medicoId,
		Long hospitalId,
		String especialidade,
		LocalDateTime dataHora,
		Integer valorConsulta) {
}
