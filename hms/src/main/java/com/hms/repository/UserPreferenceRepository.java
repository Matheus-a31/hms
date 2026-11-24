package com.hms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hms.model.UserPreference;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
	Optional<UserPreference> findByUserId(long userId);
}
