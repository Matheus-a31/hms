package com.hms.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hms.dto.HospitalDTO;
import com.hms.service.HospitalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/hms")
@Tag(name = "Hospital", description = "Status da API e dados institucionais da unidade hospitalar")
public class HospitalController {

	private final HospitalService hospitalService;

	public HospitalController(HospitalService hospitalService) {
		this.hospitalService = hospitalService;
	}

	@GetMapping("/")
	@Operation(summary = "Status da API HMS")
	public ResponseEntity<Map<String, String>> home() {
		return ResponseEntity.ok(Map.of("status", "UP", "service", "Hospital Management System API v2.0"));
	}

	@GetMapping("/hospital")
	@Operation(summary = "Listar instituições hospitalares")
	public ResponseEntity<List<HospitalDTO>> getHospital() {
		return ResponseEntity.ok(hospitalService.findAll());
	}

	@PutMapping(value = "/hospital/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Atualizar dados institucionais do hospital")
	public ResponseEntity<HospitalDTO> updateHospital(@PathVariable("id") long id, @RequestBody HospitalDTO dto) {
		return ResponseEntity.ok(hospitalService.update(id, dto));
	}
}
