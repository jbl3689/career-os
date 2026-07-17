package com.careeros.api.gmail.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GmailScanResultRepository extends JpaRepository<GmailScanResultEntity, Long> {

	Optional<GmailScanResultEntity> findByEmailMessageId(long emailMessageId);
}
