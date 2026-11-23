package com.hms.dto;

import com.hms.model.Doctor;

public class DoctorDTO {

	private long id;
	private String name;
	private String designation;
	private String degree;
	private int experience;
	private String address;
	private String emailId;
	private long contactNumber;

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

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getDegree() {
		return degree;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public int getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public Doctor toEntity() {
		Doctor doctor = new Doctor();
		doctor.setId(this.id);
		doctor.setName(this.name);
		doctor.setDesignation(this.designation);
		doctor.setDegree(this.degree);
		doctor.setExperience(this.experience);
		doctor.setAddress(this.address);
		doctor.setEmailId(this.emailId);
		doctor.setContactNumber(this.contactNumber);
		return doctor;
	}
}
