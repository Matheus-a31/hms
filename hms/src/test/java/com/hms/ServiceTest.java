package com.hms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.hms.dto.HospitalDTO;
import com.hms.service.HospitalService;

@SpringBootTest
class ServiceTest {

	@Autowired
	private HospitalService hospitalService;

	@Test
	void testGetHospital() {
		List<HospitalDTO> hospitals = hospitalService.findAll();
		assertNotNull(hospitals);
		assertFalse(hospitals.isEmpty());
		assertEquals("Hospital Central de Fortaleza", hospitals.get(0).name());
	}
}
