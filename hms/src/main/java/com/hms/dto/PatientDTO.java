package com.hms.dto;

import com.hms.model.Patient;

public class PatientDTO {

	private long id;
	private String name;
	private String address;
	private String gender;
	private String disease;
	private String emailId;
	private long contactNumber;
	private Long doctorId;
	private DoctorDTO doctor;

	public static PatientDTO fromEntity(Patient patient) {
		if (patient == null) {
			return null;
		}
		PatientDTO dto = new PatientDTO();
		dto.setId(patient.getId());
		dto.setName(patient.getName());
		dto.setAddress(patient.getAddress());
		dto.setGender(patient.getGender());
		dto.setDisease(patient.getDisease());
		dto.setEmailId(patient.getEmailId());
		dto.setContactNumber(patient.getContactNumber());
		if (patient.getDoctor() != null) {
			dto.setDoctorId(patient.getDoctor().getId());
			dto.setDoctor(DoctorDTO.fromEntity(patient.getDoctor()));
		}
		return dto;
	}

	public Patient toEntity() {
		Patient patient = new Patient();
		patient.setId(this.id);
		patient.setName(this.name);
		patient.setAddress(this.address);
		patient.setGender(this.gender);
		patient.setDisease(this.disease);
		patient.setEmailId(this.emailId);
		patient.setContactNumber(this.contactNumber);
		if (this.doctor != null) {
			patient.setDoctor(this.doctor.toEntity());
		}
		return patient;
	}

	public Long resolveDoctorId() {
		if (this.doctorId != null && this.doctorId > 0) {
			return this.doctorId;
		}
		if (this.doctor != null && this.doctor.getId() > 0) {
			return this.doctor.getId();
		}
		return null;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getDisease() {
		return disease;
	}

	public void setDisease(String disease) {
		this.disease = disease;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public long getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(long contactNumber) {
		this.contactNumber = contactNumber;
	}

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public DoctorDTO getDoctor() {
		return doctor;
	}

	public void setDoctor(DoctorDTO doctor) {
		this.doctor = doctor;
	}
}
