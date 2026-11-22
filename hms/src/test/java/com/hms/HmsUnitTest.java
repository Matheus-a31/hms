package com.hms;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.hms.controller.HMSController;
import com.hms.dao.impl.DoctorDAOImpl;
import com.hms.dao.impl.HospitalDAOImpl;
import com.hms.dao.impl.PatientDAOImpl;
import com.hms.rowmapper.DoctorRowMapper;
import com.hms.rowmapper.HospitalRowMapper;
import com.hms.rowmapper.PatientRowMapper;
import com.hms.service.HMSService;

public class HmsUnitTest {

	@Test
	public void testHMSControllerConstructor() {
		HMSController controller = new HMSController(new HMSService());
		assertNotNull(controller);
	}

	@Test
	public void testDoctorDAOImplConstructor() {
		DoctorDAOImpl dao = new DoctorDAOImpl(null, new DoctorRowMapper());
		assertNotNull(dao);
	}

	@Test
	public void testHospitalDAOImplConstructor() {
		HospitalDAOImpl dao = new HospitalDAOImpl(null, new HospitalRowMapper());
		assertNotNull(dao);
	}

	@Test
	public void testPatientDAOImplConstructor() {
		PatientDAOImpl dao = new PatientDAOImpl(null, new PatientRowMapper());
		assertNotNull(dao);
	}
}
