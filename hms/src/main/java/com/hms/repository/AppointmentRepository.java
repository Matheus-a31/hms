package com.hms.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hms.model.Appointment;
import com.hms.model.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
	boolean existsByDoctorIdAndAppointmentDateAndStatusNot(long doctorId, LocalDateTime appointmentDate, AppointmentStatus status);
	List<Appointment> findByDoctorIdAndAppointmentDateBetween(long doctorId, LocalDateTime start, LocalDateTime end);
	List<Appointment> findByDoctorId(long doctorId);
	List<Appointment> findByPatientId(long patientId);
	List<Appointment> findByAppointmentDateBetween(LocalDateTime start, LocalDateTime end);
}
