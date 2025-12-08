package com.smoothTalkAI.backend.security;

import com.smoothTalkAI.backend.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties properties;
    private SecretKey secretKey;

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = Date.from(Instant.ofEpochMilli(now.getTime())
            .plusSeconds(properties.getAccessTokenValidityInSec()));

        return Jwts.builder()
            .setSubject(String.valueOf(user.getId()))
            .claim("email", user.getEmail())
            .claim("name", user.getName())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build()
            .parseClaimsJws(token).getBody();
        return Long.parseLong(claims.getSubject());
    }
}

