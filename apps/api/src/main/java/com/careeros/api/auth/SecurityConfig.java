package com.careeros.api.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			@Value("${career-os.frontend-url}") String frontendUrl) throws Exception {
		CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();

		http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(
								"/api/v1/health",
								"/api/v1/auth/csrf",
								"/oauth2/**",
								"/login/**",
								"/error")
						.permitAll()
						.anyRequest()
						.authenticated())
				.csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
				.oauth2Login(oauth -> oauth.defaultSuccessUrl(frontendUrl + "/applications", true))
				.logout(logout -> logout
						.logoutUrl("/api/v1/auth/logout")
						.deleteCookies("JSESSIONID")
						.invalidateHttpSession(true)
						.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
				.exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
						new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
						PathPatternRequestMatcher.pathPattern("/api/**")));

		return http.build();
	}
}
