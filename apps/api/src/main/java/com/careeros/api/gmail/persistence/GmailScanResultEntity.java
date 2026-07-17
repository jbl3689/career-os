package com.careeros.api.gmail.persistence;

import java.time.Instant;

import com.careeros.api.gmail.GmailClassificationCategory;
import com.careeros.api.gmail.GmailEventType;
import com.careeros.api.gmail.GmailMessageClassification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "gmail_scan_results")
public class GmailScanResultEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "email_message_id", nullable = false, unique = true)
	private EmailMessageEntity emailMessage;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private GmailScanResultStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private GmailClassificationCategory classification;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", length = 30)
	private GmailEventType eventType;

	@Column(name = "confidence_score")
	private Integer confidenceScore;

	@Column(name = "classification_reason", columnDefinition = "text")
	private String classificationReason;

	protected GmailScanResultEntity() {
	}

	public GmailScanResultEntity(EmailMessageEntity emailMessage, Instant createdAt) {
		this.emailMessage = emailMessage;
		this.status = GmailScanResultStatus.PENDING_CLASSIFICATION;
		this.createdAt = createdAt;
		this.updatedAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public GmailScanResultStatus getStatus() {
		return status;
	}

	public GmailClassificationCategory getClassification() {
		return classification;
	}

	public GmailEventType getEventType() {
		return eventType;
	}

	public Integer getConfidenceScore() {
		return confidenceScore;
	}

	public String getClassificationReason() {
		return classificationReason;
	}

	public boolean isPendingClassification() {
		return status == GmailScanResultStatus.PENDING_CLASSIFICATION;
	}

	public void applyClassification(
			GmailMessageClassification result,
			Instant classifiedAt) {
		this.classification = result.category();
		this.eventType = result.eventType();
		this.confidenceScore = result.confidenceScore();
		this.classificationReason = result.reason();
		this.status = switch (result.category()) {
			case JOB_RELATED -> GmailScanResultStatus.READY_FOR_MATCHING;
			case NOT_JOB_RELATED -> GmailScanResultStatus.IGNORED;
			case UNCERTAIN -> GmailScanResultStatus.PENDING_REVIEW;
		};
		this.updatedAt = classifiedAt;
	}

	public GmailMessageClassification classificationResult() {
		if (classification == null
				|| eventType == null
				|| confidenceScore == null
				|| classificationReason == null) {
			throw new IllegalStateException("Gmail scan result has not been classified");
		}
		return new GmailMessageClassification(
				classification,
				eventType,
				confidenceScore,
				classificationReason);
	}
}
