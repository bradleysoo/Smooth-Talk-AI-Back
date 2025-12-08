package com.smoothTalkAI.backend.ratelimit;

import com.smoothTalkAI.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Rate limit 초과 시에는 RuntimeException 대신 ApiException을 직접 던지도록 변경
 */
public class RateLimitExceededException {

    public static ApiException forAnonymousUser() {
        return ApiException.of(HttpStatus.TOO_MANY_REQUESTS, "Anonymous usage exceeded");
    }

    public static ApiException forFreeUser() {
        return ApiException.of(HttpStatus.TOO_MANY_REQUESTS, "Free usage exceeded");
    }
}
