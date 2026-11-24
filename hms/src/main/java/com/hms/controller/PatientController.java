package com.hms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hms.dto.PatientDTO;
import com.hms.service.PatientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/hms/patients")
@Tag(name = "Pacientes", description = "CRUD e busca de pacientes")
public class PatientController {

	private final PatientService patientService;

	public PatientController(PatientService patientService) {
		this.patientService = patientService;
	}

	@GetMapping
	@Operation(summary = "Listar todos os pacientes")
	public ResponseEntity<List<PatientDTO>> findAll() {
		return ResponseEntity.ok(patientService.findAll());
	}

	@GetMapping("/viewByName/{name}")
	@Operation(summary = "Buscar pacientes por nome")
	public ResponseEntity<List<PatientDTO>> findByName(@PathVariable("name") String name) {
		return ResponseEntity.ok(patientService.findByName(name));
	}

	@GetMapping({"/{id}", "/viewById/{id}"})
	@Operation(summary = "Buscar paciente por ID")
	public ResponseEntity<PatientDTO> findById(@PathVariable("id") long id) {
		return ResponseEntity.ok(patientService.findById(id));
	}

	@PostMapping(value = {"", "/addPatient"}, consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Cadastrar novo paciente")
	public ResponseEntity<PatientDTO> create(@RequestBody PatientDTO patientDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(patientDto));
	}

	@PutMapping(value = {"/{id}", "/updatePatient/{id}"}, consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Atualizar paciente existente")
	public ResponseEntity<PatientDTO> update(@PathVariable("id") long id, @RequestBody PatientDTO patientDto) {
		return ResponseEntity.ok(patientService.update(id, patientDto));
	}

	@DeleteMapping({"/{id}", "/deletePatient/{id}"})
	@Operation(summary = "Remover paciente")
	public ResponseEntity<Void> delete(@PathVariable("id") long id) {
		patientService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
