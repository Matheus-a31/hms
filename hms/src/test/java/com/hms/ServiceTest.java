package com.hms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.hms.dao.impl.HospitalDAOImpl;
import com.hms.model.Hospital;
import com.hms.service.HMSService;

public class ServiceTest {

	@Test
	public void testGetHospital() {
		Hospital expected = new Hospital("City Care Hospital", "Dadar", "info@citycarehospital.com", 9568745896L);
		HospitalDAOImpl hospitalDao = new HospitalDAOImpl(null, null) {
			@Override
			public List<Hospital> getHospital() {
				return Collections.singletonList(expected);
			}
		};
		HMSService service = new HMSService(null, null, null, hospitalDao);

		List<Hospital> result = service.getHospital();

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("City Care Hospital", result.get(0).getName());
	}
}
