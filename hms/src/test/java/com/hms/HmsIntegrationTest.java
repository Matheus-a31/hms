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
}
