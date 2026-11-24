package com.hms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.dto.DoctorDTO;
import com.hms.exception.BadRequestException;
import com.hms.exception.ConflictException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.model.Appointment;
import com.hms.model.AppointmentStatus;
import com.hms.model.Doctor;
import com.hms.model.Hospital;
import com.hms.model.Invoice;
import com.hms.model.Patient;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.HospitalRepository;
import com.hms.repository.InvoiceRepository;
import com.hms.repository.PatientRepository;

@Service
public class DoctorService {

	private final DoctorRepository doctorRepository;
	private final HospitalRepository hospitalRepository;
	private final PatientRepository patientRepository;
	private final InvoiceRepository invoiceRepository;
	private final AppointmentRepository appointmentRepository;

	public DoctorService(
			DoctorRepository doctorRepository,
			HospitalRepository hospitalRepository,
			PatientRepository patientRepository,
			InvoiceRepository invoiceRepository,
			AppointmentRepository appointmentRepository) {
		this.doctorRepository = doctorRepository;
		this.hospitalRepository = hospitalRepository;
		this.patientRepository = patientRepository;
		this.invoiceRepository = invoiceRepository;
		this.appointmentRepository = appointmentRepository;
	}

	public List<DoctorDTO> findAll() {
		return doctorRepository.findAll().stream()
				.map(DoctorDTO::fromEntity)
				.toList();
	}

	public List<DoctorDTO> findByName(String name) {
		return doctorRepository.findByNameContainingIgnoreCase(name).stream()
				.map(DoctorDTO::fromEntity)
				.toList();
	}

	public DoctorDTO findById(long id) {
		Doctor doctor = doctorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado: " + id));
		return DoctorDTO.fromEntity(doctor);
	}

	@Transactional
	public DoctorDTO create(DoctorDTO dto) {
		if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
			throw new BadRequestException("O nome do médico é obrigatório.");
		}
		Doctor doctor = dto.toEntity();
		doctor.setId(0);
		attachHospital(doctor, dto.getHospitalId());
		return DoctorDTO.fromEntity(doctorRepository.save(doctor));
	}

	@Transactional
	public DoctorDTO update(long id, DoctorDTO dto) {
		Doctor existing = doctorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado: " + id));
		existing.setName(dto.getName());
		existing.setDesignation(dto.getDesignation());
		existing.setDegree(dto.getDegree());
		existing.setExperience(dto.getExperience());
		existing.setAddress(dto.getAddress());
		existing.setEmailId(dto.getEmailId());
		existing.setContactNumber(dto.getContactNumber());
		attachHospital(existing, dto.getHospitalId());
		return DoctorDTO.fromEntity(doctorRepository.save(existing));
	}

	@Transactional
	public void delete(long id) {
		Doctor doctor = doctorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado: " + id));
		List<Appointment> appointments = appointmentRepository.findByDoctorId(id);
		boolean hasActive = appointments.stream()
				.anyMatch(a -> a.getStatus() == AppointmentStatus.AGENDADO || a.getStatus() == AppointmentStatus.CONFIRMADO);
		if (hasActive) {
			throw new ConflictException("Não é possível remover médico com consultas agendadas ativas.");
		}
		unlinkRelations(id, appointments);
		doctorRepository.delete(doctor);
	}

	private void unlinkRelations(long doctorId, List<Appointment> appointments) {
		for (Patient patient : patientRepository.findByDoctorId(doctorId)) {
			patient.setDoctor(null);
			patientRepository.save(patient);
		}
		for (Invoice invoice : invoiceRepository.findByDoctorId(doctorId)) {
			invoice.setDoctor(null);
			invoiceRepository.save(invoice);
		}
		appointmentRepository.deleteAll(appointments);
	}

	private void attachHospital(Doctor doctor, Long hospitalId) {
		if (hospitalId != null && hospitalId > 0) {
			Hospital hospital = hospitalRepository.findById(hospitalId)
					.orElseThrow(() -> new ResourceNotFoundException("Hospital não encontrado: " + hospitalId));
			doctor.setHospital(hospital);
		} else if (doctor.getHospital() == null) {
			hospitalRepository.findAll().stream().findFirst().ifPresent(doctor::setHospital);
		}
	}
}
