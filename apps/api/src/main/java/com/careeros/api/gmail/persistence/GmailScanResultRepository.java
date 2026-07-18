package com.careeros.api.gmail.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GmailScanResultRepository extends JpaRepository<GmailScanResultEntity, Long> {

	Optional<GmailScanResultEntity> findByEmailMessageId(long emailMessageId);

	@EntityGraph(attributePaths = {
			"emailMessage",
			"suggestedApplication",
			"suggestedApplication.company",
			"selectedApplication"
	})
	List<GmailScanResultEntity> findAllByEmailMessageUserIdAndStatusOrderByCreatedAtDesc(
			long userId,
			GmailScanResultStatus status);

	@EntityGraph(attributePaths = {
			"emailMessage",
			"suggestedApplication",
			"suggestedApplication.company",
			"selectedApplication"
	})
	Optional<GmailScanResultEntity> findByIdAndEmailMessageUserId(long id, long userId);
}
