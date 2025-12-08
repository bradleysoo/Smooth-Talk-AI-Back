package com.smoothTalkAI.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smoothTalkAI.backend.dto.AuthUserResponse;
import com.smoothTalkAI.backend.dto.JwtLoginResponse;
import com.smoothTalkAI.backend.user.User;
import com.smoothTalkAI.backend.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    @Value("${app.oauth2.success-redirect:}")
    private String successRedirect;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        var attributes = oauthToken.getPrincipal().getAttributes();

        String providerUserId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        User user = userService.upsertGoogleUser(providerUserId, email, name, picture);
        userService.updateLastLogin(user.getId());

        String jwt = jwtProvider.generateAccessToken(user);
        JwtLoginResponse payload = JwtLoginResponse.builder()
            .token(jwt)
            .expiresIn(jwtProperties.getAccessTokenValidityInSec())
            .issuedAt(Instant.now())
            .user(AuthUserResponse.from(user))
            .build();

        if (StringUtils.hasText(successRedirect)) {
            String target = UriComponentsBuilder.fromUriString(successRedirect)
                .queryParam("token", jwt)
                .build().toUriString();
            response.sendRedirect(target);
            return;
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), payload);
    }
}

