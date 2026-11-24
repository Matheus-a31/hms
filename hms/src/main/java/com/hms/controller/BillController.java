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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hms.dto.BillDTO;
import com.hms.service.BillService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/hms/bills")
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
@Tag(name = "Contas (Bill)", description = "Gestão de itens faturáveis hospitalares")
public class BillController {

	private final BillService billService;

	public BillController(BillService billService) {
		this.billService = billService;
	}

	@GetMapping
	@Operation(summary = "Listar itens de conta (Bill)")
	public ResponseEntity<List<BillDTO>> findAll(@RequestParam(value = "q", required = false) String query) {
		return ResponseEntity.ok(billService.findAll(query));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Cadastrar novo item de conta (Bill)")
	public ResponseEntity<BillDTO> create(@RequestBody BillDTO billDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(billService.create(billDto));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Atualizar item de conta (Bill)")
	public ResponseEntity<BillDTO> update(@PathVariable("id") long id, @RequestBody BillDTO billDto) {
		return ResponseEntity.ok(billService.update(id, billDto));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Remover item de conta (Bill)")
	public ResponseEntity<Void> delete(@PathVariable("id") long id) {
		billService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
