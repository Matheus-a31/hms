package com.hms.dto;

import com.hms.model.Bill;

public record BillDTO(
		long id,
		String particular,
		int quantity,
		int unitValue,
		int totalValue) {

	public static BillDTO fromEntity(Bill bill) {
		return new BillDTO(
				bill.getId(),
				bill.getParticular(),
				bill.getQuantity(),
				bill.getUnitValue(),
				bill.getTotalValue());
	}
}
