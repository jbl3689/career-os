package com.careeros.api.gmail;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.careeros.api.auth.persistence.UserEntity;
import com.careeros.api.gmail.persistence.EmailMessageEntity;
import com.careeros.api.gmail.persistence.EmailMessageRepository;
import com.careeros.api.gmail.persistence.GmailScanResultEntity;
import com.careeros.api.gmail.persistence.GmailScanResultRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GmailScanPersistenceService {

	private final EmailMessageRepository emailMessageRepository;
	private final GmailScanResultRepository scanResultRepository;
	private final GmailMessageClassifier classifier;

	public GmailScanPersistenceService(
			EmailMessageRepository emailMessageRepository,
			GmailScanResultRepository scanResultRepository,
			GmailMessageClassifier classifier) {
		this.emailMessageRepository = emailMessageRepository;
		this.scanResultRepository = scanResultRepository;
		this.classifier = classifier;
	}

	@Transactional
	public List<GmailCandidateResponse> persistCandidates(
			UserEntity user,
			List<GmailMessageMetadata> candidates,
			Instant scannedAt) {
		List<GmailCandidateResponse> persistedCandidates = new ArrayList<>();

		for (GmailMessageMetadata candidate : candidates) {
			StoredCandidate storedCandidate = emailMessageRepository
					.findByUserIdAndGmailMessageId(user.getId(), candidate.gmailMessageId())
					.map(existingMessage -> existingCandidate(
							existingMessage,
							candidate,
							scannedAt))
					.orElseGet(() -> newCandidate(user, candidate, scannedAt));

			GmailScanResultEntity scanResult = storedCandidate.scanResult();
			if (scanResult.isPendingClassification()) {
				scanResult.applyClassification(
						classifier.classify(candidate),
						scannedAt);
			}
			if (storedCandidate.newlyDiscovered()) {
				scanResultRepository.save(scanResult);
			}

			persistedCandidates.add(
					GmailCandidateResponse.from(
							candidate,
							storedCandidate.newlyDiscovered(),
							scanResult.classificationResult()));
		}

		return List.copyOf(persistedCandidates);
	}

	private StoredCandidate existingCandidate(
			EmailMessageEntity message,
			GmailMessageMetadata candidate,
			Instant scannedAt) {
		message.markSeen(candidate, scannedAt);
		GmailScanResultEntity scanResult = scanResultRepository
				.findByEmailMessageId(message.getId())
				.orElseThrow(() -> new IllegalStateException(
						"Stored Gmail message is missing its scan result"));
		return new StoredCandidate(scanResult, false);
	}

	private StoredCandidate newCandidate(
			UserEntity user,
			GmailMessageMetadata candidate,
			Instant scannedAt) {
		EmailMessageEntity message = emailMessageRepository.save(
				new EmailMessageEntity(user, candidate, scannedAt));
		GmailScanResultEntity scanResult =
				new GmailScanResultEntity(message, scannedAt);
		return new StoredCandidate(scanResult, true);
	}

	private record StoredCandidate(
			GmailScanResultEntity scanResult,
			boolean newlyDiscovered) {
	}
}
