package com.smoothTalkAI.backend.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtLoginResponse {

    private String token;
    private long expiresIn;
    private Instant issuedAt;
    private AuthUserResponse user;
}

