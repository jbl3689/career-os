package com.careeros.api.gmail.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailMessageRepository extends JpaRepository<EmailMessageEntity, Long> {

	Optional<EmailMessageEntity> findByUserIdAndGmailMessageId(
			long userId,
			String gmailMessageId);
}
