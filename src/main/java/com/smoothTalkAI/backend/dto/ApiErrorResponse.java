package com.smoothTalkAI.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ApiErrorResponse {

    private final boolean success = false;
    private final String code;
    private final String message;
    private final LocalDateTime timestamp;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, String> errors;

    public static ApiErrorResponse of(HttpStatus status, String message, Map<String, String> errors) {
        return ApiErrorResponse.builder()
            .code(status.name())
            .message(message)
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static ApiErrorResponse unauthorized() {
        return of(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.", null);
    }

    public static ApiErrorResponse forbidden() {
        return of(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", null);
    }

    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{\"success\":false}";
        }
    }
}

