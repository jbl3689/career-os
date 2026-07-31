package com.careeros.api.gmail;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.careeros.api.application.CreateJobApplicationRequest;
import com.careeros.api.application.persistence.JobApplicationEntity;
import com.careeros.api.application.persistence.JobApplicationRepository;
import com.careeros.api.application.JobApplicationNotFoundException;
import com.careeros.api.application.JobApplicationResponse;
import com.careeros.api.application.JobApplicationService;
import com.careeros.api.auth.persistence.UserEntity;
import com.careeros.api.gmail.persistence.GmailScanResultEntity;
import com.careeros.api.gmail.persistence.GmailScanResultRepository;
import com.careeros.api.gmail.persistence.GmailScanResultStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GmailReviewService {

	private final GmailScanResultRepository scanResultRepository;
	private final JobApplicationRepository applicationRepository;
	private final JobApplicationService applicationService;
	private final Clock clock;

	public GmailReviewService(
			GmailScanResultRepository scanResultRepository,
			JobApplicationRepository applicationRepository,
			JobApplicationService applicationService,
			Clock clock) {
		this.scanResultRepository = scanResultRepository;
		this.applicationRepository = applicationRepository;
		this.applicationService = applicationService;
		this.clock = clock;
	}

	public List<GmailCandidateResponse> listPendingReviews(UserEntity user) {
		return scanResultRepository
				.findAllByEmailMessageUserIdAndStatusOrderByCreatedAtDesc(
						user.getId(),
						GmailScanResultStatus.PENDING_REVIEW)
				.stream()
				.map(GmailCandidateResponse::from)
				.toList();
	}

	@Transactional
	public GmailCandidateResponse confirmMatch(
			UserEntity user,
			long reviewId,
			long applicationId) {
		GmailScanResultEntity review = findReview(user, reviewId);
		JobApplicationEntity application = applicationRepository
				.findByIdAndUserId(applicationId, user.getId())
				.orElseThrow(() -> new JobApplicationNotFoundException(applicationId));
		review.confirmApplication(application, Instant.now(clock));
		return GmailCandidateResponse.from(review);
	}

	@Transactional
	public GmailCandidateResponse dismiss(UserEntity user, long reviewId) {
		GmailScanResultEntity review = findReview(user, reviewId);
		review.dismiss(Instant.now(clock));
		return GmailCandidateResponse.from(review);
	}

	@Transactional
	public CreateApplicationFromGmailReviewResponse createApplication(
			UserEntity user,
			long reviewId,
			CreateJobApplicationRequest request) {
		GmailScanResultEntity review = findReview(user, reviewId);
		JobApplicationResponse created = applicationService.createApplication(user, request);
		JobApplicationEntity application = applicationRepository
				.findByIdAndUserId(created.id(), user.getId())
				.orElseThrow(() -> new JobApplicationNotFoundException(created.id()));
		review.confirmApplication(application, Instant.now(clock));
		return new CreateApplicationFromGmailReviewResponse(
				GmailCandidateResponse.from(review),
				created);
	}

	private GmailScanResultEntity findReview(UserEntity user, long reviewId) {
		GmailScanResultEntity review = scanResultRepository
				.findByIdAndEmailMessageUserId(reviewId, user.getId())
				.orElseThrow(() -> new GmailReviewNotFoundException(reviewId));
		if (review.getReviewStatus() != GmailReviewStatus.PENDING) {
			throw new GmailReviewNotFoundException(reviewId);
		}
		return review;
	}
}
