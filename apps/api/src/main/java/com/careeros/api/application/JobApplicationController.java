package com.careeros.api.application;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
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

	public JobApplicationController(JobApplicationService service) {
		this.service = service;
	}

	@GetMapping
	public List<JobApplicationResponse> listApplications() {
		return service.listApplications();
	}

	@GetMapping("/{id}")
	public JobApplicationResponse getApplication(@PathVariable long id) {
		return service.getApplication(id);
	}

	@PostMapping
	public ResponseEntity<JobApplicationResponse> createApplication(
			@Valid @RequestBody CreateJobApplicationRequest request) {
		JobApplicationResponse createdApplication = service.createApplication(request);
		URI location = URI.create("/api/v1/applications/" + createdApplication.id());

		return ResponseEntity.created(location).body(createdApplication);
	}

	@PatchMapping("/{id}")
	public JobApplicationResponse updateApplication(
			@PathVariable long id,
			@Valid @RequestBody UpdateJobApplicationRequest request) {
		return service.updateApplication(id, request);
	}
}
