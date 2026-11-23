package com.hms.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.hms.dto.DoctorDTO;
import com.hms.dto.PatientDTO;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.service.HMSService;

public class HMSControllerTest {

	private DoctorDTO buildDoctorDTO() {
		DoctorDTO dto = new DoctorDTO();
		dto.setId(1L);
		dto.setName("Dr. John");
		dto.setDesignation("Cardiologist");
		dto.setDegree("MD");
		dto.setExperience(10);
		dto.setAddress("Main St");
		dto.setEmailId("john@hospital.com");
		dto.setContactNumber(9856231475L);
		return dto;
	}

	@Test
	public void testDoctorDTOGettersAndToEntity() {
		DoctorDTO dto = buildDoctorDTO();
		assertEquals(1L, dto.getId());
		assertEquals("Dr. John", dto.getName());
		assertEquals("Cardiologist", dto.getDesignation());
		assertEquals("MD", dto.getDegree());
		assertEquals(10, dto.getExperience());
		assertEquals("Main St", dto.getAddress());
		assertEquals("john@hospital.com", dto.getEmailId());
		assertEquals(9856231475L, dto.getContactNumber());

		Doctor entity = dto.toEntity();
		assertNotNull(entity);
		assertEquals(1L, entity.getId());
		assertEquals("Dr. John", entity.getName());
		assertEquals("Cardiologist", entity.getDesignation());
		assertEquals("MD", entity.getDegree());
		assertEquals(10, entity.getExperience());
		assertEquals("Main St", entity.getAddress());
		assertEquals("john@hospital.com", entity.getEmailId());
		assertEquals(9856231475L, entity.getContactNumber());
	}

	@Test
	public void testAddDoctor() throws Exception {
		final Doctor[] captured = new Doctor[1];
		HMSService stubService = new HMSService(null, null, null, null) {
			@Override
			public Doctor createDoctor(Doctor doctor) {
				captured[0] = doctor;
				return doctor;
			}
		};
		HMSController controller = new HMSController(stubService);

		controller.addDoctor(buildDoctorDTO());

		assertNotNull(captured[0]);
		assertEquals("Dr. John", captured[0].getName());
	}

	@Test
	public void testUpdateDoctor() throws Exception {
		final Doctor[] captured = new Doctor[1];
		HMSService stubService = new HMSService(null, null, null, null) {
			@Override
			public Doctor updateDoctor(Doctor doctor) {
				captured[0] = doctor;
				return doctor;
			}
		};
		HMSController controller = new HMSController(stubService);

		controller.updateDoctor(42L, buildDoctorDTO());

		assertNotNull(captured[0]);
		assertEquals(42L, captured[0].getId());
		assertEquals("Dr. John", captured[0].getName());
	}

	private PatientDTO buildPatientDTO() {
		PatientDTO dto = new PatientDTO();
		dto.setId(2L);
		dto.setName("Rahul");
		dto.setAddress("Vasai");
		dto.setGender("Male");
		dto.setDisease("Fever");
		dto.setEmailId("rahul@mail.com");
		dto.setContactNumber(8655312612L);
		dto.setDoctor(buildDoctorDTO());
		return dto;
	}

	@Test
	public void testPatientDTOGettersAndToEntity() {
		PatientDTO dto = buildPatientDTO();
		assertEquals(2L, dto.getId());
		assertEquals("Rahul", dto.getName());
		assertEquals("Vasai", dto.getAddress());
		assertEquals("Male", dto.getGender());
		assertEquals("Fever", dto.getDisease());
		assertEquals("rahul@mail.com", dto.getEmailId());
		assertEquals(8655312612L, dto.getContactNumber());
		assertNotNull(dto.getDoctor());

		Patient entity = dto.toEntity();
		assertNotNull(entity);
		assertEquals(2L, entity.getId());
		assertEquals("Rahul", entity.getName());
		assertEquals("Vasai", entity.getAddress());
		assertEquals("Male", entity.getGender());
		assertEquals("Fever", entity.getDisease());
		assertEquals("rahul@mail.com", entity.getEmailId());
		assertEquals(8655312612L, entity.getContactNumber());
		assertNotNull(entity.getDoctor());
		assertEquals(1L, entity.getDoctor().getId());

		dto.setDoctor(null);
		assertNull(dto.toEntity().getDoctor());
	}

	@Test
	public void testAddPatient() throws Exception {
		final Patient[] captured = new Patient[1];
		HMSService stubService = new HMSService(null, null, null, null) {
			@Override
			public Patient createPatient(Patient patient) {
				captured[0] = patient;
				return patient;
			}
		};
		HMSController controller = new HMSController(stubService);

		controller.addPatient(buildPatientDTO());

		assertNotNull(captured[0]);
		assertEquals("Rahul", captured[0].getName());
	}
}
