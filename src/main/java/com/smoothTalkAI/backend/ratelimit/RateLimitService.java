package com.smoothTalkAI.backend.ratelimit;

import com.smoothTalkAI.backend.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Token-based Rate Limiting Service
 * - Anonymous users: IP-based, 3 daily requests
 * - Logged-in users: Token-based, checks token balance
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int ANONYMOUS_DAILY_LIMIT = 3;

    /**
     * Check anonymous user limit (IP-based)
     * 
     * @param ipAddress Client IP
     * @return true if allowed, false if exceeded
     */
    public boolean checkAnonymousLimit(String ipAddress) {
        String blockKey = "blocked:ip:" + ipAddress;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))) {
            log.warn("Blocked IP attempted access: {}", ipAddress);
            return false;
        }

        String key = "rate_limit:ip:" + ipAddress + ":daily";
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofDays(1));
        }

        if (count != null && count > ANONYMOUS_DAILY_LIMIT) {
            redisTemplate.opsForValue().set(blockKey, "blocked", Duration.ofDays(1));
            log.warn("IP {} exceeded daily limit. Blocked for 24 hours.", ipAddress);
            return false;
        }

        log.debug("IP {} used {}/{} requests today", ipAddress, count, ANONYMOUS_DAILY_LIMIT);
        return true;
    }

    /**
     * Check logged-in user's token balance
     * 
     * @param user The user
     * @return true if user has tokens, false otherwise
     */
    public boolean checkUserLimit(User user) {
        Long tokenBalance = user.getTokenBalance();
        if (tokenBalance != null && tokenBalance > 0) {
            log.debug("User {} has {} tokens", user.getId(), tokenBalance);
            return true;
        }

        log.warn("User {} has insufficient tokens ({})", user.getId(), tokenBalance);
        return false;
    }

    /**
     * Get remaining quota for anonymous users
     */
    public int getRemainingQuotaForIp(String ipAddress) {
        String key = "rate_limit:ip:" + ipAddress + ":daily";
        String value = redisTemplate.opsForValue().get(key);
        int used = value != null ? Integer.parseInt(value) : 0;
        return Math.max(0, ANONYMOUS_DAILY_LIMIT - used);
    }

    /**
     * Get token balance for logged-in users
     */
    public Long getRemainingQuotaForUser(User user) {
        return user.getTokenBalance() != null ? user.getTokenBalance() : 0L;
    }
}
