package com.careeros.api.auth.google;

import com.careeros.api.auth.CurrentUserService;
import com.careeros.api.auth.persistence.UserEntity;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/google-connection")
public class GoogleConnectionController {

	private final CurrentUserService currentUserService;
	private final GoogleConnectionService connectionService;

	public GoogleConnectionController(
			CurrentUserService currentUserService,
			GoogleConnectionService connectionService) {
		this.currentUserService = currentUserService;
		this.connectionService = connectionService;
	}

	@GetMapping
	public GoogleConnectionResponse getConnection(@AuthenticationPrincipal OidcUser oidcUser) {
		UserEntity user = currentUserService.resolve(oidcUser);
		return connectionService.getConnection(user);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void disconnect(@AuthenticationPrincipal OidcUser oidcUser) {
		UserEntity user = currentUserService.resolve(oidcUser);
		connectionService.disconnect(user);
	}
}
