package com.smoothTalkAI.backend.exception;

import com.smoothTalkAI.backend.dto.ApiErrorResponse;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
        log.warn("API 예외: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
            .body(ApiErrorResponse.of(ex.getStatus(), ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                fieldError -> fieldError.getField(),
                fieldError -> fieldError.getDefaultMessage(),
                (a, b) -> b
            ));
        return ResponseEntity.badRequest()
            .body(ApiErrorResponse.of(org.springframework.http.HttpStatus.BAD_REQUEST, "유효성 검사 실패", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        log.error("서버 오류", ex);
        return ResponseEntity.internalServerError()
            .body(ApiErrorResponse.of(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류", null));
    }
}

