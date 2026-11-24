package com.hms.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.dto.InvoiceDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.model.Bill;
import com.hms.model.Doctor;
import com.hms.model.Invoice;
import com.hms.model.Patient;
import com.hms.repository.BillRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.InvoiceRepository;
import com.hms.repository.PatientRepository;

@Service
public class InvoiceService {

	private final InvoiceRepository invoiceRepository;
	private final PatientRepository patientRepository;
	private final DoctorRepository doctorRepository;
	private final BillRepository billRepository;

	public InvoiceService(
			InvoiceRepository invoiceRepository,
			PatientRepository patientRepository,
			DoctorRepository doctorRepository,
			BillRepository billRepository) {
		this.invoiceRepository = invoiceRepository;
		this.patientRepository = patientRepository;
		this.doctorRepository = doctorRepository;
		this.billRepository = billRepository;
	}

	public List<InvoiceDTO> findAll() {
		return invoiceRepository.findAll().stream()
				.map(InvoiceDTO::fromEntity)
				.toList();
	}

	public InvoiceDTO findById(long id) {
		Invoice invoice = invoiceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada: " + id));
		return InvoiceDTO.fromEntity(invoice);
	}

	@Transactional
	public InvoiceDTO create(InvoiceDTO dto) {
		Invoice invoice = new Invoice();
		applyFields(invoice, dto);
		return InvoiceDTO.fromEntity(invoiceRepository.save(invoice));
	}

	@Transactional
	public InvoiceDTO update(long id, InvoiceDTO dto) {
		Invoice existing = invoiceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada: " + id));
		applyFields(existing, dto);
		return InvoiceDTO.fromEntity(invoiceRepository.save(existing));
	}

	@Transactional
	public void delete(long id) {
		Invoice existing = invoiceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada: " + id));
		invoiceRepository.delete(existing);
	}

	private void applyFields(Invoice invoice, InvoiceDTO dto) {
		invoice.setInvoiceType(defaultIfBlank(dto.invoiceType(), "PACIENTE"));
		invoice.setDueDate(dto.dueDate() != null ? dto.dueDate() : LocalDate.now().plusDays(15));
		invoice.setPaymentStatus(defaultIfBlank(dto.paymentStatus(), "PENDENTE"));
		invoice.setPatient(resolvePatient(dto.patientId()));
		invoice.setDoctor(resolveDoctor(dto.doctorId()));
		Bill bill = resolveBill(dto.billId());
		invoice.setBill(bill);
		int amount = dto.totalAmount() > 0 ? dto.totalAmount() : (bill != null ? bill.getTotalValue() : 0);
		invoice.setTotalAmount(amount);
	}

	private Patient resolvePatient(Long id) {
		if (id == null || id <= 0) {
			return null;
		}
		return patientRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado: " + id));
	}

	private Doctor resolveDoctor(Long id) {
		if (id == null || id <= 0) {
			return null;
		}
		return doctorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado: " + id));
	}

	private Bill resolveBill(Long id) {
		if (id == null || id <= 0) {
			return null;
		}
		return billRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Item de conta não encontrado: " + id));
	}

	private String defaultIfBlank(String value, String fallback) {
		return value != null && !value.isBlank() ? value : fallback;
	}
}
