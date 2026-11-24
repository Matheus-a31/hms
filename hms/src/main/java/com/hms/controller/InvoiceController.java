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

import com.hms.dto.InvoiceDTO;
import com.hms.service.InvoiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/hms")
@Tag(name = "Faturas (Invoice)", description = "Emissão e gestão de faturas hospitalares")
public class InvoiceController {

	private final InvoiceService invoiceService;

	public InvoiceController(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@GetMapping({"/invoice", "/invoices"})
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO', 'PACIENTE')")
	@Operation(summary = "Listar todas as faturas")
	public ResponseEntity<List<InvoiceDTO>> findAll() {
		return ResponseEntity.ok(invoiceService.findAll());
	}

	@GetMapping("/invoices/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO', 'PACIENTE')")
	@Operation(summary = "Consultar fatura por ID")
	public ResponseEntity<InvoiceDTO> findById(@PathVariable("id") long id) {
		return ResponseEntity.ok(invoiceService.findById(id));
	}

	@PostMapping(value = "/invoices", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
	@Operation(summary = "Emitir nova fatura")
	public ResponseEntity<InvoiceDTO> create(@RequestBody InvoiceDTO invoiceDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(invoiceDto));
	}

	@PutMapping(value = "/invoices/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
	@Operation(summary = "Atualizar fatura existente")
	public ResponseEntity<InvoiceDTO> update(@PathVariable("id") long id, @RequestBody InvoiceDTO invoiceDto) {
		return ResponseEntity.ok(invoiceService.update(id, invoiceDto));
	}

	@DeleteMapping("/invoices/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
	@Operation(summary = "Remover fatura")
	public ResponseEntity<Void> delete(@PathVariable("id") long id) {
		invoiceService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
