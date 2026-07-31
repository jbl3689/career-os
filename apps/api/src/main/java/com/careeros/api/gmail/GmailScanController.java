package com.careeros.api.gmail;

import com.careeros.api.application.CreateJobApplicationRequest;
import com.careeros.api.auth.CurrentUserService;
import com.careeros.api.auth.persistence.UserEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gmail")
public class GmailScanController {

	private final CurrentUserService currentUserService;
	private final GmailScanService gmailScanService;
	private final GmailReviewService gmailReviewService;

	public GmailScanController(
			CurrentUserService currentUserService,
			GmailScanService gmailScanService,
			GmailReviewService gmailReviewService) {
		this.currentUserService = currentUserService;
		this.gmailScanService = gmailScanService;
		this.gmailReviewService = gmailReviewService;
	}

	@PostMapping("/scan")
	public GmailScanResponse scan(@AuthenticationPrincipal OidcUser oidcUser) {
		UserEntity user = currentUserService.resolve(oidcUser);
		return gmailScanService.scan(user);
	}

	@GetMapping("/reviews")
	public List<GmailCandidateResponse> listReviews(
			@AuthenticationPrincipal OidcUser oidcUser) {
		return gmailReviewService.listPendingReviews(currentUserService.resolve(oidcUser));
	}

	@PostMapping("/reviews/{reviewId}/match")
	public GmailCandidateResponse confirmMatch(
			@AuthenticationPrincipal OidcUser oidcUser,
			@PathVariable long reviewId,
			@Valid @RequestBody MatchGmailReviewRequest request) {
		return gmailReviewService.confirmMatch(
				currentUserService.resolve(oidcUser),
				reviewId,
				request.applicationId());
	}

	@PostMapping("/reviews/{reviewId}/dismiss")
	public GmailCandidateResponse dismiss(
			@AuthenticationPrincipal OidcUser oidcUser,
			@PathVariable long reviewId) {
		return gmailReviewService.dismiss(
				currentUserService.resolve(oidcUser),
				reviewId);
	}

	@PostMapping("/reviews/{reviewId}/application")
	public CreateApplicationFromGmailReviewResponse createApplication(
			@AuthenticationPrincipal OidcUser oidcUser,
			@PathVariable long reviewId,
			@Valid @RequestBody CreateJobApplicationRequest request) {
		return gmailReviewService.createApplication(
				currentUserService.resolve(oidcUser),
				reviewId,
				request);
	}
}
