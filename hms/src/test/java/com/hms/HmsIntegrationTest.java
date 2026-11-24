package com.hms;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class HmsIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private String accessToken;
	private String refreshToken;

	@BeforeEach
	void authenticate() throws Exception {
		MvcResult loginResult = mockMvc.perform(post("/hms/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"admin@hms.com\",\"password\":\"admin123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expiresInSeconds").value(900))
				.andReturn();

		JsonNode json = objectMapper.readTree(loginResult.getResponse().getContentAsString());
		this.accessToken = json.get("accessToken").asText();
		this.refreshToken = json.get("refreshToken").asText();
	}

	private String bearer() {
		return "Bearer " + accessToken;
	}

	@Test
	void testAuthLifecycleRegisterRefreshLogoutAndUnauthorized() throws Exception {
		mockMvc.perform(get("/hms/doctors"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/hms/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"admin@hms.com\",\"password\":\"wrongpass\"}"))
				.andExpect(status().isUnauthorized());

		String uniqueEmail = "novo" + System.nanoTime() + "@hms.com";
		MvcResult regResult = mockMvc.perform(post("/hms/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Novo Usuario\",\"email\":\"" + uniqueEmail + "\",\"password\":\"senha123\",\"role\":\"PACIENTE\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").isNotEmpty())
				.andReturn();

		mockMvc.perform(post("/hms/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Dup\",\"email\":\"" + uniqueEmail + "\",\"password\":\"senha123\",\"role\":\"PACIENTE\"}"))
				.andExpect(status().isConflict());

		JsonNode regJson = objectMapper.readTree(regResult.getResponse().getContentAsString());
		String regRefresh = regJson.get("refreshToken").asText();

		MvcResult refreshed = mockMvc.perform(post("/hms/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + regRefresh + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andReturn();

		// Old rotated refresh token must fail
		mockMvc.perform(post("/hms/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + regRefresh + "\"}"))
				.andExpect(status().isUnauthorized());

		String nextRefresh = objectMapper.readTree(refreshed.getResponse().getContentAsString()).get("refreshToken").asText();
		mockMvc.perform(post("/hms/auth/logout")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + nextRefresh + "\"}"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/hms/auth/me").header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("admin@hms.com"));
	}

	@Test
	void testAccessibilityPreferencesRf32() throws Exception {
		mockMvc.perform(get("/hms/preferences").header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tamanhoFonte").isNotEmpty());

		mockMvc.perform(put("/hms/preferences")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"usuarioId\":1,\"altoContraste\":true,\"tamanhoFonte\":\"large\",\"focoAcentuado\":true}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.altoContraste").value(true))
				.andExpect(jsonPath("$.tamanhoFonte").value("large"));
	}

	@Test
	void testHospitalDoctorPatientCrudFlow() throws Exception {
		mockMvc.perform(get("/hms/"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/hms/hospital").header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThan(0))));

		mockMvc.perform(put("/hms/hospital/1")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"id\":1,\"name\":\"Hospital Central de Fortaleza\",\"address\":\"Av. Universidade, 2853\",\"website\":\"www.hospitalsanatorio.com.br\",\"contactNumber\":8533669000}"))
				.andExpect(status().isOk());

		// Create Doctor -> 201
		MvcResult createdDoc = mockMvc.perform(post("/hms/doctors")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Dr. Teste CRUD\",\"designation\":\"Ortopedista\",\"degree\":\"MD\",\"experience\":5,\"address\":\"Fortaleza\",\"emailId\":\"crud@hms.com\",\"contactNumber\":85999990000}"))
				.andExpect(status().isCreated())
				.andReturn();
		long docId = objectMapper.readTree(createdDoc.getResponse().getContentAsString()).get("id").asLong();

		mockMvc.perform(get("/hms/doctors/" + docId).header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Dr. Teste CRUD"));

		mockMvc.perform(get("/hms/doctors/viewByName/Teste").header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThan(0))));

		mockMvc.perform(put("/hms/doctors/" + docId)
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Dr. Teste Atualizado\",\"designation\":\"Ortopedista\",\"degree\":\"MD\",\"experience\":6,\"address\":\"Fortaleza\",\"emailId\":\"crud@hms.com\",\"contactNumber\":85999990000}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Dr. Teste Atualizado"));

		// Create Patient linked to Doctor -> 201
		MvcResult createdPat = mockMvc.perform(post("/hms/patients")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Paciente Teste\",\"address\":\"Aldeota\",\"gender\":\"Feminino\",\"disease\":\"Gripe\",\"emailId\":\"pac@hms.com\",\"contactNumber\":85988887777,\"doctorId\":" + docId + "}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.doctor.id").value(docId))
				.andReturn();
		long patId = objectMapper.readTree(createdPat.getResponse().getContentAsString()).get("id").asLong();

		mockMvc.perform(get("/hms/patients/" + patId).header("Authorization", bearer()))
				.andExpect(status().isOk());
		mockMvc.perform(get("/hms/patients/viewByName/Paciente").header("Authorization", bearer()))
				.andExpect(status().isOk());
		mockMvc.perform(put("/hms/patients/" + patId)
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Paciente Atualizado\",\"address\":\"Aldeota\",\"gender\":\"Feminino\",\"disease\":\"Rinite\",\"emailId\":\"pac@hms.com\",\"contactNumber\":85988887777,\"doctorId\":" + docId + "}"))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/hms/patients/" + patId).header("Authorization", bearer()))
				.andExpect(status().isNoContent());
		mockMvc.perform(delete("/hms/doctors/" + docId).header("Authorization", bearer()))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/hms/doctors/99999").header("Authorization", bearer()))
				.andExpect(status().isNotFound());
	}

	@Test
	void testBillsInvoicesAppointmentsAndFinancialReports() throws Exception {
		// Bills CRUD
		MvcResult createdBill = mockMvc.perform(post("/hms/bills")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"particular\":\"Exame Sangue\",\"quantity\":2,\"unitValue\":120}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.totalValue").value(240))
				.andReturn();
		long billId = objectMapper.readTree(createdBill.getResponse().getContentAsString()).get("id").asLong();

		mockMvc.perform(get("/hms/bills").header("Authorization", bearer()))
				.andExpect(status().isOk());
		mockMvc.perform(put("/hms/bills/" + billId)
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"particular\":\"Exame Sangue Completo\",\"quantity\":2,\"unitValue\":150}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalValue").value(300));

		// Invoice CRUD with @ManyToOne links
		MvcResult createdInv = mockMvc.perform(post("/hms/invoices")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"patientId\":1,\"doctorId\":1,\"billId\":" + billId + ",\"invoiceType\":\"PACIENTE\",\"paymentStatus\":\"PENDENTE\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.totalAmount").value(300))
				.andReturn();
		long invId = objectMapper.readTree(createdInv.getResponse().getContentAsString()).get("id").asLong();

		mockMvc.perform(get("/hms/invoices/" + invId).header("Authorization", bearer()))
				.andExpect(status().isOk());
		mockMvc.perform(put("/hms/invoices/" + invId)
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"patientId\":1,\"doctorId\":1,\"billId\":" + billId + ",\"totalAmount\":300,\"invoiceType\":\"PACIENTE\",\"paymentStatus\":\"PAGO\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paymentStatus").value("PAGO"));

		// Appointments (Solicitação 08 - RF29 / RF30) + double booking conflict check
		String targetDate = LocalDate.now().plusDays(5).toString();
		String appointmentPayload = "{\"pacienteId\":1,\"medicoId\":1,\"especialidade\":\"Cardiologia\",\"dataHora\":\"" + targetDate + "T14:00:00\",\"valorConsulta\":400}";

		MvcResult createdApp = mockMvc.perform(post("/hms/appointments")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(appointmentPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("AGENDADO"))
				.andReturn();
		long appId = objectMapper.readTree(createdApp.getResponse().getContentAsString()).get("id").asLong();

		// Double booking same doctor + time -> 409 Conflict
		mockMvc.perform(post("/hms/appointments")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(appointmentPayload))
				.andExpect(status().isConflict());

		mockMvc.perform(get("/hms/appointments/available?doctorId=1&date=" + targetDate)
						.header("Authorization", bearer()))
				.andExpect(status().isOk());

		mockMvc.perform(get("/hms/appointments/occupancy?date=" + targetDate)
						.header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].vagasOcupadas").value(1));

		mockMvc.perform(patch("/hms/appointments/" + appId + "/status?status=CONFIRMADO")
						.header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMADO"));

		mockMvc.perform(patch("/hms/appointments/" + appId + "/status?status=CONCLUIDO")
						.header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONCLUIDO"));

		// Financial Reports & Dashboard (Solicitação 11 - RF31)
		mockMvc.perform(get("/hms/doctors/financial-report").header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThan(0))));

		mockMvc.perform(get("/hms/doctors/1/financial-report").header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lucroLiquido", greaterThan(0)));

		mockMvc.perform(get("/hms/dashboard/summary").header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.receitaTotal", greaterThan(0)));

		// Cleanup
		mockMvc.perform(delete("/hms/appointments/" + appId).header("Authorization", bearer()))
				.andExpect(status().isNoContent());
		mockMvc.perform(delete("/hms/invoices/" + invId).header("Authorization", bearer()))
				.andExpect(status().isNoContent());
		mockMvc.perform(delete("/hms/bills/" + billId).header("Authorization", bearer()))
				.andExpect(status().isNoContent());

		// OpenAPI & static index
		mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
		mockMvc.perform(get("/index.html")).andExpect(status().isOk());
	}

	@Test
	void testValidationErrorAndEdgeCasePathsAcrossServices() throws Exception {
		// Auth validation & invalid tokens
		mockMvc.perform(post("/hms/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"\",\"email\":\"a@hms.com\",\"password\":\"123456\"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/hms/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Curta\",\"email\":\"curta@hms.com\",\"password\":\"123\"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/hms/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"\",\"password\":\"\"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/hms/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"naoexiste@hms.com\",\"password\":\"123456\"}"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(post("/hms/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"\"}"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(post("/hms/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"token-inexistente\"}"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(post("/hms/auth/logout")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"\"}"))
				.andExpect(status().isNoContent());

		// Invalid Bearer token
		mockMvc.perform(get("/hms/doctors").header("Authorization", "Bearer invalid.jwt.token"))
				.andExpect(status().isUnauthorized());

		// List endpoints directly
		mockMvc.perform(get("/hms/doctors").header("Authorization", bearer())).andExpect(status().isOk());
		mockMvc.perform(get("/hms/patients").header("Authorization", bearer())).andExpect(status().isOk());
		mockMvc.perform(get("/hms/invoices").header("Authorization", bearer())).andExpect(status().isOk());
		mockMvc.perform(get("/hms/appointments").header("Authorization", bearer())).andExpect(status().isOk());
		mockMvc.perform(get("/hms/bills?q=Consulta").header("Authorization", bearer())).andExpect(status().isOk());

		// Doctor validation, explicit hospitalId, conflict on active appointment, and unlinkRelations on delete
		mockMvc.perform(post("/hms/doctors")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"\"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/hms/doctors")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Dr. Hosp Invalido\",\"hospitalId\":99999}"))
				.andExpect(status().isNotFound());

		MvcResult docRes = mockMvc.perform(post("/hms/doctors")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Dr. Vinculos\",\"designation\":\"Clinico\",\"degree\":\"CRM\",\"experience\":3,\"address\":\"Centro\",\"emailId\":\"vinculos@hms.com\",\"contactNumber\":85911112222,\"hospitalId\":1}"))
				.andExpect(status().isCreated())
				.andReturn();
		long docId = objectMapper.readTree(docRes.getResponse().getContentAsString()).get("id").asLong();

		// Patient validation + null doctor + update without doctor + 404
		mockMvc.perform(post("/hms/patients")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"\"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/hms/patients")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Pac Sem Medico\",\"doctorId\":99999}"))
				.andExpect(status().isNotFound());
		MvcResult patRes = mockMvc.perform(post("/hms/patients")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Pac Vinculado\",\"address\":\"Centro\",\"gender\":\"Masculino\",\"disease\":\"Checkup\",\"emailId\":\"pv@hms.com\",\"contactNumber\":85922223333,\"doctorId\":" + docId + "}"))
				.andExpect(status().isCreated())
				.andReturn();
		long patId = objectMapper.readTree(patRes.getResponse().getContentAsString()).get("id").asLong();

		MvcResult patNoDoc = mockMvc.perform(post("/hms/patients")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Pac Avulso\",\"address\":\"Centro\",\"gender\":\"Outro\",\"disease\":\"Rotina\",\"emailId\":\"pa@hms.com\",\"contactNumber\":85933334444}"))
				.andExpect(status().isCreated())
				.andReturn();
		long patNoDocId = objectMapper.readTree(patNoDoc.getResponse().getContentAsString()).get("id").asLong();
		mockMvc.perform(put("/hms/patients/" + patNoDocId)
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Pac Avulso Upd\",\"address\":\"Sul\",\"gender\":\"Outro\",\"disease\":\"Rotina\",\"emailId\":\"pa@hms.com\",\"contactNumber\":85933334444}"))
				.andExpect(status().isOk());

		// Bill validation + 404 + linked invoice unlink on bill delete
		mockMvc.perform(post("/hms/bills")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"particular\":\"\",\"quantity\":1,\"unitValue\":50}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(put("/hms/bills/99999")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"particular\":\"X\",\"quantity\":1,\"unitValue\":50}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/hms/bills/99999").header("Authorization", bearer()))
				.andExpect(status().isNotFound());

		MvcResult billRes = mockMvc.perform(post("/hms/bills")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"particular\":\"Raio-X\",\"quantity\":1,\"unitValue\":180}"))
				.andExpect(status().isCreated())
				.andReturn();
		long billId = objectMapper.readTree(billRes.getResponse().getContentAsString()).get("id").asLong();

		// Invoices: institutional (null refs) + linked + 404 checks
		MvcResult instInvRes = mockMvc.perform(post("/hms/invoices")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"totalAmount\":500,\"dueDate\":\"2026-12-31\"}"))
				.andExpect(status().isCreated())
				.andReturn();
		long instInvId = objectMapper.readTree(instInvRes.getResponse().getContentAsString()).get("id").asLong();

		MvcResult linkedInvRes = mockMvc.perform(post("/hms/invoices")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"patientId\":" + patId + ",\"doctorId\":" + docId + ",\"billId\":" + billId + "}"))
				.andExpect(status().isCreated())
				.andReturn();
		long linkedInvId = objectMapper.readTree(linkedInvRes.getResponse().getContentAsString()).get("id").asLong();

		mockMvc.perform(post("/hms/invoices")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"patientId\":99999}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(post("/hms/invoices")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"doctorId\":99999}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(post("/hms/invoices")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"billId\":99999}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/hms/invoices/99999").header("Authorization", bearer()))
				.andExpect(status().isNotFound());

		// Appointments: validation, default specialty/fee, hospitalId, CANCELADO, AGENDADO, default date
		mockMvc.perform(post("/hms/appointments")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"pacienteId\":null}"))
				.andExpect(status().isBadRequest());
		String futureDate = LocalDate.now().plusDays(12).toString();
		MvcResult appRes = mockMvc.perform(post("/hms/appointments")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"pacienteId\":" + patId + ",\"medicoId\":" + docId + ",\"hospitalId\":1,\"dataHora\":\"" + futureDate + "T09:00:00\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.valorConsulta").value(350))
				.andReturn();
		long appId = objectMapper.readTree(appRes.getResponse().getContentAsString()).get("id").asLong();

		mockMvc.perform(get("/hms/appointments/available?doctorId=" + docId).header("Authorization", bearer()))
				.andExpect(status().isOk());
		mockMvc.perform(get("/hms/appointments/occupancy").header("Authorization", bearer()))
				.andExpect(status().isOk());

		// Cannot delete doctor with active appointment -> 409
		mockMvc.perform(delete("/hms/doctors/" + docId).header("Authorization", bearer()))
				.andExpect(status().isConflict());

		// Cancel appointment, then toggle back to AGENDADO and CANCELADO so doctor can be deleted
		mockMvc.perform(patch("/hms/appointments/" + appId + "/status?status=AGENDADO").header("Authorization", bearer()))
				.andExpect(status().isOk());
		mockMvc.perform(patch("/hms/appointments/" + appId + "/status?status=CANCELADO").header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELADO"));
		mockMvc.perform(delete("/hms/appointments/99999").header("Authorization", bearer()))
				.andExpect(status().isNotFound());

		// Delete bill (unlinks invoice), delete doctor (unlinks patient, invoice & deletes canceled appointment), delete patients & invoices
		mockMvc.perform(delete("/hms/bills/" + billId).header("Authorization", bearer())).andExpect(status().isNoContent());
		mockMvc.perform(delete("/hms/doctors/" + docId).header("Authorization", bearer())).andExpect(status().isNoContent());
		mockMvc.perform(delete("/hms/patients/" + patId).header("Authorization", bearer())).andExpect(status().isNoContent());
		mockMvc.perform(delete("/hms/patients/" + patNoDocId).header("Authorization", bearer())).andExpect(status().isNoContent());
		mockMvc.perform(delete("/hms/invoices/" + linkedInvId).header("Authorization", bearer())).andExpect(status().isNoContent());
		mockMvc.perform(delete("/hms/invoices/" + instInvId).header("Authorization", bearer())).andExpect(status().isNoContent());
	}
}
