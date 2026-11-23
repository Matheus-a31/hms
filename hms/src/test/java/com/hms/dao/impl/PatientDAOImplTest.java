package com.hms.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

import com.hms.model.Doctor;
import com.hms.model.Patient;

public class PatientDAOImplTest {

	private Patient buildPatient(Doctor doctor) {
		Patient patient = new Patient();
		patient.setId(5L);
		patient.setName("Rahul");
		patient.setAddress("Vasai");
		patient.setGender("Male");
		patient.setDisease("Fever");
		patient.setEmailId("rahul@mail.com");
		patient.setContactNumber(8655312612L);
		patient.setDoctor(doctor);
		return patient;
	}

	@Test
	public void testCreatePatientWithDoctor() {
		final Object[] capturedDoctorId = new Object[1];
		NamedParameterJdbcTemplate stubJdbc = new NamedParameterJdbcTemplate(new JdbcTemplate()) {
			@Override
			public int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder) {
				capturedDoctorId[0] = paramSource.getValue("doctor");
				generatedKeyHolder.getKeyList().add(Collections.<String, Object>singletonMap("id", 10L));
				return 1;
			}
		};
		PatientDAOImpl dao = new PatientDAOImpl(stubJdbc, null);
		Doctor doctor = new Doctor();
		doctor.setId(7L);

		Patient created = dao.createPatient(buildPatient(doctor));

		assertEquals(10L, created.getId());
		assertEquals(7L, capturedDoctorId[0]);
	}

	@Test
	public void testCreatePatientWithoutDoctor() {
		final Object[] capturedDoctorId = new Object[1];
		NamedParameterJdbcTemplate stubJdbc = new NamedParameterJdbcTemplate(new JdbcTemplate()) {
			@Override
			public int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder) {
				capturedDoctorId[0] = paramSource.getValue("doctor");
				generatedKeyHolder.getKeyList().add(Collections.<String, Object>singletonMap("id", 11L));
				return 1;
			}
		};
		PatientDAOImpl dao = new PatientDAOImpl(stubJdbc, null);

		Patient created = dao.createPatient(buildPatient(null));

		assertEquals(11L, created.getId());
		assertNull(capturedDoctorId[0]);
	}

	@Test
	public void testUpdatePatientWithDoctor() {
		final Object[] capturedDoctorId = new Object[1];
		NamedParameterJdbcTemplate stubJdbc = new NamedParameterJdbcTemplate(new JdbcTemplate()) {
			@Override
			public int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder) {
				capturedDoctorId[0] = paramSource.getValue("doctor");
				return 1;
			}
		};
		PatientDAOImpl dao = new PatientDAOImpl(stubJdbc, null);
		Doctor doctor = new Doctor();
		doctor.setId(8L);

		dao.updatePatient(buildPatient(doctor));

		assertEquals(8L, capturedDoctorId[0]);
	}

	@Test
	public void testUpdatePatientWithoutDoctor() {
		final Object[] capturedDoctorId = new Object[1];
		NamedParameterJdbcTemplate stubJdbc = new NamedParameterJdbcTemplate(new JdbcTemplate()) {
			@Override
			public int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder) {
				capturedDoctorId[0] = paramSource.getValue("doctor");
				return 1;
			}
		};
		PatientDAOImpl dao = new PatientDAOImpl(stubJdbc, null);

		dao.updatePatient(buildPatient(null));

		assertNull(capturedDoctorId[0]);
	}
}
