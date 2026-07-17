package com.careeros.api.gmail;

import com.careeros.api.auth.CurrentUserService;
import com.careeros.api.auth.persistence.UserEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gmail")
public class GmailScanController {

	private final CurrentUserService currentUserService;
	private final GmailScanService gmailScanService;

	public GmailScanController(
			CurrentUserService currentUserService,
			GmailScanService gmailScanService) {
		this.currentUserService = currentUserService;
		this.gmailScanService = gmailScanService;
	}

	@PostMapping("/scan")
	public GmailScanResponse scan(@AuthenticationPrincipal OidcUser oidcUser) {
		UserEntity user = currentUserService.resolve(oidcUser);
		return gmailScanService.scan(user);
	}
}
