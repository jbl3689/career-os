package com.careeros.api.auth;

import com.careeros.api.auth.persistence.UserEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final CurrentUserService currentUserService;

	public AuthController(CurrentUserService currentUserService) {
		this.currentUserService = currentUserService;
	}

	@GetMapping("/me")
	public CurrentUserResponse currentUser(@AuthenticationPrincipal OidcUser oidcUser) {
		UserEntity user = currentUserService.resolve(oidcUser);
		return CurrentUserResponse.from(user);
	}

	@GetMapping("/csrf")
	public CsrfResponse csrf(CsrfToken csrfToken) {
		return new CsrfResponse(csrfToken.getToken());
	}

	public record CsrfResponse(String token) {
	}
}
