package com.smoothTalkAI.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    private ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static ApiException of(HttpStatus status, String message) {
        return new ApiException(status, message);
    }

    public static ApiException notFound(String message) {
        return of(HttpStatus.NOT_FOUND, message);
    }

    public static ApiException unauthorized(String message) {
        return of(HttpStatus.UNAUTHORIZED, message);
    }
}

