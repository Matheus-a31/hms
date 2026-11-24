package com.hms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.dto.BillDTO;
import com.hms.exception.BadRequestException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.model.Bill;
import com.hms.model.Invoice;
import com.hms.repository.BillRepository;
import com.hms.repository.InvoiceRepository;

@Service
public class BillService {

	private final BillRepository billRepository;
	private final InvoiceRepository invoiceRepository;

	public BillService(BillRepository billRepository, InvoiceRepository invoiceRepository) {
		this.billRepository = billRepository;
		this.invoiceRepository = invoiceRepository;
	}

	public List<BillDTO> findAll(String query) {
		List<Bill> bills = (query == null || query.isBlank())
				? billRepository.findAll()
				: billRepository.findByParticularContainingIgnoreCase(query.trim());
		return bills.stream().map(BillDTO::fromEntity).toList();
	}

	@Transactional
	public BillDTO create(BillDTO dto) {
		if (dto == null || dto.particular() == null || dto.particular().isBlank()) {
			throw new BadRequestException("A descrição do item de conta é obrigatória.");
		}
		Bill bill = new Bill(dto.particular().trim(), Math.max(1, dto.quantity()), Math.max(0, dto.unitValue()));
		return BillDTO.fromEntity(billRepository.save(bill));
	}

	@Transactional
	public BillDTO update(long id, BillDTO dto) {
		Bill existing = billRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + id));
		existing.setParticular(dto.particular());
		existing.setQuantity(Math.max(1, dto.quantity()));
		existing.setUnitValue(Math.max(0, dto.unitValue()));
		return BillDTO.fromEntity(billRepository.save(existing));
	}

	@Transactional
	public void delete(long id) {
		Bill existing = billRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + id));
		for (Invoice invoice : invoiceRepository.findByBillId(id)) {
			invoice.setBill(null);
			invoiceRepository.save(invoice);
		}
		billRepository.delete(existing);
	}
}
