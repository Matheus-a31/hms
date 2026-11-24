package com.hms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hms.model.Hospital;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
}
