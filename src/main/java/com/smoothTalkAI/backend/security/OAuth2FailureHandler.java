package com.smoothTalkAI.backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles OAuth2 login failures (e.g., user cancels Google login).
 * Redirects back to the frontend instead of showing an error.
 */
@Slf4j
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.failure-redirect}")
    private String failureRedirect;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        log.info("OAuth2 login failed or cancelled: {}", exception.getMessage());

        // Redirect back to frontend (configured via application.yml)
        getRedirectStrategy().sendRedirect(request, response, failureRedirect);
    }
}
