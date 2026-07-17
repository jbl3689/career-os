package com.careeros.api.auth.google;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GoogleConnectionRepository extends JpaRepository<GoogleConnectionEntity, Long> {

	Optional<GoogleConnectionEntity> findByUserId(Long userId);
}
