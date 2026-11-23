package com.hms.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.hms.dto.DoctorDTO;
import com.hms.model.Doctor;
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
}
