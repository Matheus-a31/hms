package com.hms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.dto.PatientDTO;
import com.hms.exception.BadRequestException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.model.Doctor;
import com.hms.model.Invoice;
import com.hms.model.Patient;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.InvoiceRepository;
import com.hms.repository.PatientRepository;

@Service
public class PatientService {

	private final PatientRepository patientRepository;
	private final DoctorRepository doctorRepository;
	private final InvoiceRepository invoiceRepository;
	private final AppointmentRepository appointmentRepository;

	public PatientService(
			PatientRepository patientRepository,
			DoctorRepository doctorRepository,
			InvoiceRepository invoiceRepository,
			AppointmentRepository appointmentRepository) {
		this.patientRepository = patientRepository;
		this.doctorRepository = doctorRepository;
		this.invoiceRepository = invoiceRepository;
		this.appointmentRepository = appointmentRepository;
	}

	public List<PatientDTO> findAll() {
		return patientRepository.findAll().stream()
				.map(PatientDTO::fromEntity)
				.toList();
	}

	public List<PatientDTO> findByName(String name) {
		return patientRepository.findByNameContainingIgnoreCase(name).stream()
				.map(PatientDTO::fromEntity)
				.toList();
	}

	public PatientDTO findById(long id) {
		Patient patient = patientRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado: " + id));
		return PatientDTO.fromEntity(patient);
	}

	@Transactional
	public PatientDTO create(PatientDTO dto) {
		if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
			throw new BadRequestException("O nome do paciente é obrigatório.");
		}
		Patient patient = dto.toEntity();
		patient.setId(0);
		patient.setDoctor(resolveDoctor(dto.resolveDoctorId()));
		return PatientDTO.fromEntity(patientRepository.save(patient));
	}

	@Transactional
	public PatientDTO update(long id, PatientDTO dto) {
		Patient existing = patientRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado: " + id));
		existing.setName(dto.getName());
		existing.setAddress(dto.getAddress());
		existing.setGender(dto.getGender());
		existing.setDisease(dto.getDisease());
		existing.setEmailId(dto.getEmailId());
		existing.setContactNumber(dto.getContactNumber());
		Long doctorId = dto.resolveDoctorId();
		if (doctorId != null) {
			existing.setDoctor(resolveDoctor(doctorId));
		}
		return PatientDTO.fromEntity(patientRepository.save(existing));
	}

	@Transactional
	public void delete(long id) {
		Patient patient = patientRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado: " + id));
		for (Invoice invoice : invoiceRepository.findByPatientId(id)) {
			invoice.setPatient(null);
			invoiceRepository.save(invoice);
		}
		appointmentRepository.deleteAll(appointmentRepository.findByPatientId(id));
		patientRepository.delete(patient);
	}

	private Doctor resolveDoctor(Long doctorId) {
		if (doctorId == null) {
			return null;
		}
		return doctorRepository.findById(doctorId)
				.orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado: " + doctorId));
	}
}
