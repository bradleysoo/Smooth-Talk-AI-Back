package com.smoothTalkAI.backend.dto;

import com.smoothTalkAI.backend.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthUserResponse {

    private Long id;
    private String email;
    private String name;
    private String profileImage;
    private Long tokenBalance;

    public static AuthUserResponse from(User user) {
        return AuthUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .tokenBalance(user.getTokenBalance())
                .build();
    }
}
