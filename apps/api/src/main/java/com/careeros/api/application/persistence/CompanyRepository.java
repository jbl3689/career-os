package com.careeros.api.application.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {

	Optional<CompanyEntity> findByName(String name);
}
