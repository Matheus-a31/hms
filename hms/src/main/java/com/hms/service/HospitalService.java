package com.hms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.dto.HospitalDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.model.Hospital;
import com.hms.repository.HospitalRepository;

@Service
public class HospitalService {

	private final HospitalRepository hospitalRepository;

	public HospitalService(HospitalRepository hospitalRepository) {
		this.hospitalRepository = hospitalRepository;
	}

	public List<HospitalDTO> findAll() {
		return hospitalRepository.findAll().stream()
				.map(HospitalDTO::fromEntity)
				.toList();
	}

	@Transactional
	public HospitalDTO update(long id, HospitalDTO dto) {
		Hospital hospital = hospitalRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Hospital não encontrado: " + id));
		hospital.setName(dto.name());
		hospital.setAddress(dto.address());
		hospital.setWebsite(dto.website());
		hospital.setContactNumber(dto.contactNumber());
		return HospitalDTO.fromEntity(hospitalRepository.save(hospital));
	}
}
