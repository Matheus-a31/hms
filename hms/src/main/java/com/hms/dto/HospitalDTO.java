package com.hms.dto;

import com.hms.model.Hospital;

public record HospitalDTO(
		long id,
		String name,
		String address,
		String website,
		long contactNumber) {

	public static HospitalDTO fromEntity(Hospital hospital) {
		return new HospitalDTO(
				hospital.getId(),
				hospital.getName(),
				hospital.getAddress(),
				hospital.getWebsite(),
				hospital.getContactNumber());
	}
}
