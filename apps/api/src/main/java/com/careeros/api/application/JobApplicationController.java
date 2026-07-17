package com.careeros.api.application;

import java.net.URI;
import java.util.List;

import com.careeros.api.auth.CurrentUserService;
import com.careeros.api.auth.persistence.UserEntity;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/applications")
public class JobApplicationController {

	private final JobApplicationService service;
	private final CurrentUserService currentUserService;

	public JobApplicationController(JobApplicationService service, CurrentUserService currentUserService) {
		this.service = service;
		this.currentUserService = currentUserService;
	}

	@GetMapping
	public List<JobApplicationResponse> listApplications(@AuthenticationPrincipal OidcUser oidcUser) {
		return service.listApplications(currentUserService.resolve(oidcUser));
	}

	@GetMapping("/{id}")
	public JobApplicationResponse getApplication(
			@AuthenticationPrincipal OidcUser oidcUser,
			@PathVariable long id) {
		return service.getApplication(currentUserService.resolve(oidcUser), id);
	}

	@PostMapping
	public ResponseEntity<JobApplicationResponse> createApplication(
			@AuthenticationPrincipal OidcUser oidcUser,
			@Valid @RequestBody CreateJobApplicationRequest request) {
		UserEntity user = currentUserService.resolve(oidcUser);
		JobApplicationResponse createdApplication = service.createApplication(user, request);
		URI location = URI.create("/api/v1/applications/" + createdApplication.id());

		return ResponseEntity.created(location).body(createdApplication);
	}

	@PatchMapping("/{id}")
	public JobApplicationResponse updateApplication(
			@AuthenticationPrincipal OidcUser oidcUser,
			@PathVariable long id,
			@Valid @RequestBody UpdateJobApplicationRequest request) {
		return service.updateApplication(currentUserService.resolve(oidcUser), id, request);
	}
}
