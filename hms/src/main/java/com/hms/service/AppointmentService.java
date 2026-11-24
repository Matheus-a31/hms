package com.hms.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.dto.AppointmentDTO;
import com.hms.dto.AppointmentRequestDTO;
import com.hms.dto.OccupancyDTO;
import com.hms.exception.BadRequestException;
import com.hms.exception.ConflictException;
import com.hms.exception.ForbiddenException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.model.Appointment;
import com.hms.model.AppointmentStatus;
import com.hms.model.Doctor;
import com.hms.model.Hospital;
import com.hms.model.Patient;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.HospitalRepository;
import com.hms.repository.PatientRepository;
import com.hms.security.AuthenticatedUserService;

@Service
public class AppointmentService {

	private static final List<String> DAILY_SLOTS = List.of(
			"08:00", "09:00", "10:30", "11:30", "14:00", "15:30", "16:30", "17:30");
	private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

	private final AppointmentRepository appointmentRepository;
	private final PatientRepository patientRepository;
	private final DoctorRepository doctorRepository;
	private final HospitalRepository hospitalRepository;
	private final AuthenticatedUserService authenticatedUserService;

	public AppointmentService(
			AppointmentRepository appointmentRepository,
			PatientRepository patientRepository,
			DoctorRepository doctorRepository,
			HospitalRepository hospitalRepository,
			AuthenticatedUserService authenticatedUserService) {
		this.appointmentRepository = appointmentRepository;
		this.patientRepository = patientRepository;
		this.doctorRepository = doctorRepository;
		this.hospitalRepository = hospitalRepository;
		this.authenticatedUserService = authenticatedUserService;
	}

	public List<AppointmentDTO> findAll() {
		if (authenticatedUserService.isPatientRole()) {
			Long ownPatientId = authenticatedUserService.getCurrentPatientId();
			if (ownPatientId == null) {
				return List.of();
			}
			return appointmentRepository.findByPatientId(ownPatientId).stream()
					.map(AppointmentDTO::fromEntity)
					.toList();
		}
		return appointmentRepository.findAll().stream()
				.map(AppointmentDTO::fromEntity)
				.toList();
	}

	@Transactional
	public AppointmentDTO create(AppointmentRequestDTO request) {
		if (request == null || request.pacienteId() == null || request.medicoId() == null || request.dataHora() == null) {
			throw new BadRequestException("Paciente, médico e data/hora são obrigatórios para agendamento.");
		}
		authenticatedUserService.assertCanAccessPatient(request.pacienteId());
		Patient patient = patientRepository.findById(request.pacienteId())
				.orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado: " + request.pacienteId()));
		Doctor doctor = doctorRepository.findById(request.medicoId())
				.orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado: " + request.medicoId()));

		boolean occupied = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStatusNot(
				doctor.getId(), request.dataHora(), AppointmentStatus.CANCELADO);
		if (occupied) {
			throw new ConflictException("Horário indisponível: este médico já possui agendamento neste horário.");
		}

		Appointment appointment = new Appointment();
		appointment.setPatient(patient);
		appointment.setDoctor(doctor);
		appointment.setHospital(resolveHospital(request.hospitalId(), doctor));
		appointment.setSpecialty(request.especialidade() != null && !request.especialidade().isBlank()
				? request.especialidade()
				: doctor.getDesignation());
		appointment.setAppointmentDate(request.dataHora());
		appointment.setStatus(AppointmentStatus.AGENDADO);
		appointment.setConsultationFee(request.valorConsulta() != null && request.valorConsulta() > 0
				? request.valorConsulta()
				: 350);
		return AppointmentDTO.fromEntity(appointmentRepository.save(appointment));
	}

	@Transactional
	public AppointmentDTO updateStatus(long id, AppointmentStatus status) {
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado: " + id));
		if (authenticatedUserService.isPatientRole()) {
			Long ownerId = appointment.getPatient() != null ? appointment.getPatient().getId() : null;
			authenticatedUserService.assertCanAccessPatient(ownerId);
			if (status != AppointmentStatus.CANCELADO) {
				throw new ForbiddenException("Pacientes só podem cancelar seus próprios agendamentos.");
			}
		}
		switch (status) {
			case CONFIRMADO -> appointment.confirm();
			case CANCELADO -> appointment.cancel();
			case CONCLUIDO -> appointment.conclude();
			case AGENDADO -> appointment.setStatus(AppointmentStatus.AGENDADO);
		}
		return AppointmentDTO.fromEntity(appointmentRepository.save(appointment));
	}

	@Transactional
	public void delete(long id) {
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado: " + id));
		appointmentRepository.delete(appointment);
	}

	public List<String> getAvailableSlots(long doctorId, LocalDate date) {
		LocalDate targetDate = date != null ? date : LocalDate.now();
		Set<String> occupied = findOccupiedSlots(doctorId, targetDate).stream().collect(Collectors.toSet());
		return DAILY_SLOTS.stream()
				.filter(slot -> !occupied.contains(slot))
				.toList();
	}

	public List<OccupancyDTO> getOccupancyReport(LocalDate date) {
		LocalDate targetDate = date != null ? date : LocalDate.now();
		List<OccupancyDTO> result = new ArrayList<>();
		for (Doctor doctor : doctorRepository.findAll()) {
			List<String> occupiedSlots = findOccupiedSlots(doctor.getId(), targetDate);
			int total = DAILY_SLOTS.size();
			int ocupadas = occupiedSlots.size();
			result.add(new OccupancyDTO(
					doctor.getId(),
					doctor.getName(),
					doctor.getDesignation(),
					total,
					ocupadas,
					Math.max(0, total - ocupadas),
					occupiedSlots));
		}
		return result;
	}

	private List<String> findOccupiedSlots(long doctorId, LocalDate date) {
		LocalDateTime start = date.atStartOfDay();
		LocalDateTime end = date.atTime(LocalTime.MAX);
		return appointmentRepository.findByDoctorIdAndAppointmentDateBetween(doctorId, start, end).stream()
				.filter(a -> a.getStatus() != AppointmentStatus.CANCELADO)
				.map(a -> a.getAppointmentDate().format(TIME_FMT))
				.sorted()
				.toList();
	}

	private Hospital resolveHospital(Long hospitalId, Doctor doctor) {
		if (hospitalId != null) {
			return hospitalRepository.findById(hospitalId).orElse(doctor.getHospital());
		}
		if (doctor.getHospital() != null) {
			return doctor.getHospital();
		}
		return hospitalRepository.findAll().stream().findFirst().orElse(null);
	}
}
