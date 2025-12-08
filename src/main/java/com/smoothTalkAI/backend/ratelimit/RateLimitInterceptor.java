package com.smoothTalkAI.backend.ratelimit;

import com.smoothTalkAI.backend.security.JwtProvider;
import com.smoothTalkAI.backend.user.User;
import com.smoothTalkAI.backend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Rate Limiting을 적용하는 Interceptor
 * /api/conversations/{id}/analyze 엔드포인트에만 적용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 분석 API에만 적용
        // String requestURI = request.getRequestURI();
        // if (!requestURI.matches(".*/conversations/\\d+/analyze")) {
        // return true;
        // }

        // Authorization 헤더에서 토큰 추출
        String authHeader = request.getHeader("Authorization");
        User user = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Long userId = jwtProvider.extractUserId(token);
                user = userRepository.findById(userId).orElse(null);
            } catch (Exception e) {
                log.debug("Failed to extract user from token", e);
            }
        }

        boolean allowed;
        if (user == null) {
            // 비로그인 사용자 - IP 기반
            String ipAddress = getClientIp(request);
            log.debug("Checking rate limit for IP: {}", ipAddress);
            allowed = rateLimitService.checkAnonymousLimit(ipAddress);
            if (!allowed) {
                throw RateLimitExceededException.forAnonymousUser();
            }
        } else {
            // 로그인 사용자
            log.debug("Checking rate limit for user: {}", user.getId());
            allowed = rateLimitService.checkUserLimit(user);
            if (!allowed) {
                throw RateLimitExceededException.forFreeUser();
            }
        }

        return true;
    }

    /**
     * 클라이언트 IP 추출 (프록시 고려)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 여러 IP가 있는 경우 첫 번째만 사용
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // 로컬호스트 IPv6를 IPv4로 변환
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }
}
