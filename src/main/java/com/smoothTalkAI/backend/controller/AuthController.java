package com.smoothTalkAI.backend.controller;

import com.smoothTalkAI.backend.dto.ApiResponse;
import com.smoothTalkAI.backend.dto.AuthUserResponse;
import com.smoothTalkAI.backend.exception.ApiException;
import com.smoothTalkAI.backend.security.UserPrincipal;
import com.smoothTalkAI.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<AuthUserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw ApiException.unauthorized("로그인이 필요합니다.");
        }
        var user = userService.getUserOrThrow(principal.getId());
        return ApiResponse.ok(AuthUserResponse.from(user));
    }

    @DeleteMapping("/account")
    public ApiResponse<Void> deleteAccount(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw ApiException.unauthorized("로그인이 필요합니다.");
        }
        userService.deleteAccount(principal.getId());
        return ApiResponse.ok(null);
    }
}

