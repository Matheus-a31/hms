package com.hms.dto;

import java.time.LocalDate;

import com.hms.model.Invoice;

public record InvoiceDTO(
		long id,
		Long patientId,
		String patientName,
		Long doctorId,
		String doctorName,
		Long billId,
		String billDescription,
		int totalAmount,
		String invoiceType,
		LocalDate dueDate,
		String paymentStatus) {

	public static InvoiceDTO fromEntity(Invoice invoice) {
		return new InvoiceDTO(
				invoice.getId(),
				invoice.getPatient() != null ? invoice.getPatient().getId() : null,
				invoice.getPatient() != null ? invoice.getPatient().getName() : "Institucional",
				invoice.getDoctor() != null ? invoice.getDoctor().getId() : null,
				invoice.getDoctor() != null ? invoice.getDoctor().getName() : "N/A",
				invoice.getBill() != null ? invoice.getBill().getId() : null,
				invoice.getBill() != null ? invoice.getBill().getParticular() : "Serviço Geral",
				invoice.getTotalAmount(),
				invoice.getInvoiceType(),
				invoice.getDueDate(),
				invoice.getPaymentStatus());
	}
}
