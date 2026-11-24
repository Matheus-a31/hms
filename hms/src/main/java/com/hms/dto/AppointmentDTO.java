package com.hms.dto;

import java.time.LocalDateTime;

import com.hms.model.Appointment;
import com.hms.model.AppointmentStatus;

public record AppointmentDTO(
		long id,
		Long pacienteId,
		String pacienteNome,
		Long medicoId,
		String medicoNome,
		Long hospitalId,
		String hospitalNome,
		String especialidade,
		LocalDateTime dataHora,
		AppointmentStatus status,
		int valorConsulta) {

	public static AppointmentDTO fromEntity(Appointment appointment) {
		return new AppointmentDTO(
				appointment.getId(),
				appointment.getPatient() != null ? appointment.getPatient().getId() : null,
				appointment.getPatient() != null ? appointment.getPatient().getName() : null,
				appointment.getDoctor() != null ? appointment.getDoctor().getId() : null,
				appointment.getDoctor() != null ? appointment.getDoctor().getName() : null,
				appointment.getHospital() != null ? appointment.getHospital().getId() : null,
				appointment.getHospital() != null ? appointment.getHospital().getName() : null,
				appointment.getSpecialty(),
				appointment.getAppointmentDate(),
				appointment.getStatus(),
				appointment.getConsultationFee());
	}
}
