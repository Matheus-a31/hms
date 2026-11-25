package com.hms;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hms.dto.HospitalDTO;
import com.hms.exception.ForbiddenException;
import com.hms.security.AuthenticatedUserService;
import com.hms.service.HospitalService;

@SpringBootTest
class ServiceTest {

	@Autowired
	private HospitalService hospitalService;

	@Autowired
	private AuthenticatedUserService authenticatedUserService;

	@Test
	void testGetHospital() {
		List<HospitalDTO> hospitals = hospitalService.findAll();
		assertNotNull(hospitals);
		assertFalse(hospitals.isEmpty());
		assertEquals("Hospital Central de Fortaleza", hospitals.get(0).name());
	}

	@Test
	void testAuthenticatedUserServiceSecurityContextEdgeCases() {
		try {
			// Sem autenticação no contexto
			SecurityContextHolder.clearContext();
			assertTrue(authenticatedUserService.getCurrentUserOptional().isEmpty());
			assertFalse(authenticatedUserService.isPatientRole());
			assertNull(authenticatedUserService.getCurrentPatientId());
			assertDoesNotThrow(() -> authenticatedUserService.assertCanAccessPatient(1L));

			// Token não autenticado (authenticated == false)
			SecurityContextHolder.getContext().setAuthentication(
					new UsernamePasswordAuthenticationToken("paciente@hms.com", "pass"));
			assertTrue(authenticatedUserService.getCurrentUserOptional().isEmpty());

			// Token anônimo
			SecurityContextHolder.getContext().setAuthentication(
					new AnonymousAuthenticationToken("key", "anonymous", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
			assertTrue(authenticatedUserService.getCurrentUserOptional().isEmpty());

			// Paciente autenticado tentando acessar targetPatientId nulo
			SecurityContextHolder.getContext().setAuthentication(
					new UsernamePasswordAuthenticationToken(
							"paciente@hms.com", null, List.of(new SimpleGrantedAuthority("ROLE_PACIENTE"))));
			assertTrue(authenticatedUserService.isPatientRole());
			assertEquals(1L, authenticatedUserService.getCurrentPatientId());
			assertThrows(ForbiddenException.class, () -> authenticatedUserService.assertCanAccessPatient(null));
		} finally {
			SecurityContextHolder.clearContext();
		}
	}
}
