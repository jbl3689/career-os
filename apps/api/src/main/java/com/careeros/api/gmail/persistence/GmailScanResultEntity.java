package com.careeros.api.gmail.persistence;

import java.time.Instant;

import com.careeros.api.application.persistence.JobApplicationEntity;
import com.careeros.api.gmail.ApplicationMatchSuggestion;
import com.careeros.api.gmail.GmailApplicationMatch;
import com.careeros.api.gmail.GmailClassificationCategory;
import com.careeros.api.gmail.GmailEventType;
import com.careeros.api.gmail.GmailMessageClassification;
import com.careeros.api.gmail.GmailReviewStatus;

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
import jakarta.persistence.ManyToOne;
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

	@Column(name = "match_attempted", nullable = false)
	private boolean matchAttempted;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "suggested_application_id")
	private JobApplicationEntity suggestedApplication;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "selected_application_id")
	private JobApplicationEntity selectedApplication;

	@Column(name = "match_confidence_score")
	private Integer matchConfidenceScore;

	@Column(name = "match_reason", columnDefinition = "text")
	private String matchReason;

	@Column(name = "reviewed_at")
	private Instant reviewedAt;

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

	public EmailMessageEntity getEmailMessage() {
		return emailMessage;
	}

	public GmailReviewStatus getReviewStatus() {
		return switch (status) {
			case MATCHED -> GmailReviewStatus.MATCHED;
			case DISMISSED, IGNORED -> GmailReviewStatus.DISMISSED;
			default -> GmailReviewStatus.PENDING;
		};
	}

	public Long getSelectedApplicationId() {
		return selectedApplication == null ? null : selectedApplication.getId();
	}

	public ApplicationMatchSuggestion matchSuggestion() {
		if (suggestedApplication == null) {
			return null;
		}
		return ApplicationMatchSuggestion.from(
				suggestedApplication,
				matchConfidenceScore,
				matchReason);
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

	public boolean needsMatching() {
		return !matchAttempted
				&& (status == GmailScanResultStatus.READY_FOR_MATCHING
						|| status == GmailScanResultStatus.PENDING_REVIEW);
	}

	public void applyMatching(
			java.util.Optional<GmailApplicationMatch> match,
			Instant matchedAt) {
		this.matchAttempted = true;
		match.ifPresent(suggestion -> {
			this.suggestedApplication = suggestion.application();
			this.matchConfidenceScore = suggestion.confidenceScore();
			this.matchReason = suggestion.reason();
		});
		this.status = GmailScanResultStatus.PENDING_REVIEW;
		this.updatedAt = matchedAt;
	}

	public void confirmApplication(
			JobApplicationEntity application,
			Instant reviewedAt) {
		this.selectedApplication = application;
		this.status = GmailScanResultStatus.MATCHED;
		this.reviewedAt = reviewedAt;
		this.updatedAt = reviewedAt;
	}

	public void dismiss(Instant reviewedAt) {
		this.selectedApplication = null;
		this.status = GmailScanResultStatus.DISMISSED;
		this.reviewedAt = reviewedAt;
		this.updatedAt = reviewedAt;
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
