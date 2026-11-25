package com.hms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hms.dto.DoctorDTO;
import com.hms.service.DoctorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/hms/doctors")
@Tag(name = "Médicos", description = "CRUD e busca de profissionais médicos")
public class DoctorController {

	private final DoctorService doctorService;

	public DoctorController(DoctorService doctorService) {
		this.doctorService = doctorService;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Listar todos os médicos")
	public ResponseEntity<List<DoctorDTO>> findAll() {
		return ResponseEntity.ok(doctorService.findAll());
	}

	@GetMapping("/viewByName/{name}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Buscar médicos por nome")
	public ResponseEntity<List<DoctorDTO>> findByName(@PathVariable("name") String name) {
		return ResponseEntity.ok(doctorService.findByName(name));
	}

	@GetMapping({"/{id}", "/viewById/{id}"})
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Buscar médico por ID")
	public ResponseEntity<DoctorDTO> findById(@PathVariable("id") long id) {
		return ResponseEntity.ok(doctorService.findById(id));
	}

	@PostMapping(value = {"", "/addDoctor"}, consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Cadastrar novo médico")
	public ResponseEntity<DoctorDTO> create(@RequestBody DoctorDTO doctorDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.create(doctorDto));
	}

	@PutMapping(value = {"/{id}", "/updateDoctor/{id}"}, consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Atualizar médico existente")
	public ResponseEntity<DoctorDTO> update(@PathVariable("id") long id, @RequestBody DoctorDTO doctorDto) {
		return ResponseEntity.ok(doctorService.update(id, doctorDto));
	}

	@DeleteMapping({"/{id}", "/deleteDoctor/{id}"})
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Remover médico")
	public ResponseEntity<Void> delete(@PathVariable("id") long id) {
		doctorService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
