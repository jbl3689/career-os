package com.careeros.api.auth.google;

import java.io.IOException;

import com.careeros.api.auth.persistence.UserEntity;
import com.careeros.api.auth.persistence.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class GmailConnectionAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final OAuth2AuthorizedClientService authorizedClientService;
	private final UserRepository userRepository;
	private final GoogleConnectionService connectionService;
	private final SimpleUrlAuthenticationSuccessHandler loginSuccessHandler;
	private final String frontendUrl;

	public GmailConnectionAuthenticationSuccessHandler(
			OAuth2AuthorizedClientService authorizedClientService,
			UserRepository userRepository,
			GoogleConnectionService connectionService,
			@Value("${career-os.frontend-url}") String frontendUrl) {
		this.authorizedClientService = authorizedClientService;
		this.userRepository = userRepository;
		this.connectionService = connectionService;
		this.frontendUrl = frontendUrl;
		this.loginSuccessHandler = new SimpleUrlAuthenticationSuccessHandler(
				frontendUrl + "/applications");
		this.loginSuccessHandler.setAlwaysUseDefaultTargetUrl(true);
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)
				|| !GmailAuthorizationRequestResolver.GMAIL_REGISTRATION_ID.equals(
						oauthToken.getAuthorizedClientRegistrationId())) {
			loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);
			return;
		}

		OidcUser gmailUser = (OidcUser) oauthToken.getPrincipal();
		String expectedSubject = (String) request.getSession().getAttribute(
				GmailAuthorizationRequestResolver.EXPECTED_GOOGLE_SUBJECT_SESSION_ATTRIBUTE);
		request.getSession().removeAttribute(
				GmailAuthorizationRequestResolver.EXPECTED_GOOGLE_SUBJECT_SESSION_ATTRIBUTE);

		OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
				oauthToken.getAuthorizedClientRegistrationId(),
				oauthToken.getName());

		if (expectedSubject == null || !expectedSubject.equals(gmailUser.getSubject())) {
			removeTemporaryAuthorizedClient(oauthToken);
			request.getSession().invalidate();
			response.sendRedirect(frontendUrl + "/applications?gmail=account-mismatch");
			return;
		}

		if (authorizedClient == null || authorizedClient.getRefreshToken() == null) {
			removeTemporaryAuthorizedClient(oauthToken);
			response.sendRedirect(frontendUrl + "/applications?gmail=missing-refresh-token");
			return;
		}

		UserEntity user = userRepository.findByGoogleSubject(expectedSubject)
				.orElseThrow(() -> new IllegalStateException("Signed-in Career OS user was not found"));

		connectionService.connect(
				user,
				gmailUser.getSubject(),
				gmailUser.getEmail(),
				authorizedClient.getRefreshToken().getTokenValue(),
				authorizedClient.getAccessToken().getScopes());
		removeTemporaryAuthorizedClient(oauthToken);

		response.sendRedirect(frontendUrl + "/applications?gmail=connected");
	}

	private void removeTemporaryAuthorizedClient(OAuth2AuthenticationToken oauthToken) {
		authorizedClientService.removeAuthorizedClient(
				oauthToken.getAuthorizedClientRegistrationId(),
				oauthToken.getName());
	}
}
