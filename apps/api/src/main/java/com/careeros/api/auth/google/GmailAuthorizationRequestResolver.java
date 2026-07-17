package com.careeros.api.auth.google;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class GmailAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

	public static final String GMAIL_REGISTRATION_ID = "google-gmail";
	public static final String EXPECTED_GOOGLE_SUBJECT_SESSION_ATTRIBUTE =
			GmailAuthorizationRequestResolver.class.getName() + ".EXPECTED_GOOGLE_SUBJECT";

	private final DefaultOAuth2AuthorizationRequestResolver delegate;

	public GmailAuthorizationRequestResolver(ClientRegistrationRepository registrations) {
		this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
				registrations,
				"/oauth2/authorization");
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
		return customize(request, delegate.resolve(request));
	}

	@Override
	public OAuth2AuthorizationRequest resolve(
			HttpServletRequest request,
			String clientRegistrationId) {
		return customize(request, delegate.resolve(request, clientRegistrationId));
	}

	private OAuth2AuthorizationRequest customize(
			HttpServletRequest request,
			OAuth2AuthorizationRequest authorizationRequest) {
		if (authorizationRequest == null
				|| !GMAIL_REGISTRATION_ID.equals(
						authorizationRequest.getAttribute("registration_id"))) {
			return authorizationRequest;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
			return authorizationRequest;
		}

		request.getSession().setAttribute(
				EXPECTED_GOOGLE_SUBJECT_SESSION_ATTRIBUTE,
				oidcUser.getSubject());

		Map<String, Object> parameters = new LinkedHashMap<>(
				authorizationRequest.getAdditionalParameters());
		parameters.put("access_type", "offline");
		parameters.put("include_granted_scopes", "true");
		parameters.put("prompt", "consent");
		parameters.put("login_hint", oidcUser.getEmail());

		return OAuth2AuthorizationRequest.from(authorizationRequest)
				.additionalParameters(parameters)
				.build();
	}
}
