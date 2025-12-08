package com.smoothTalkAI.backend.ratelimit;

import com.smoothTalkAI.backend.security.JwtProvider;
import com.smoothTalkAI.backend.user.User;
import com.smoothTalkAI.backend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageController {

    private final RateLimitService rateLimitService;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @GetMapping("/quota")
    public ResponseEntity<Map<String, Object>> getQuota(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        Map<String, Object> result = new HashMap<>();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Long userId = jwtProvider.extractUserId(token);
                User user = userRepository.findById(userId).orElse(null);

                if (user != null) {
                    Long tokenBalance = rateLimitService.getRemainingQuotaForUser(user);
                    result.put("tokenBalance", tokenBalance);
                    result.put("isLoggedIn", true);
                    return ResponseEntity.ok(result);
                }
            } catch (Exception e) {
                // Invalid token, fall through to anonymous
            }
        }

        // Anonymous user
        String ipAddress = getClientIp(request);
        int remaining = rateLimitService.getRemainingQuotaForIp(ipAddress);
        result.put("remaining", remaining);
        result.put("isLoggedIn", false);

        return ResponseEntity.ok(result);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        } else if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }
}
