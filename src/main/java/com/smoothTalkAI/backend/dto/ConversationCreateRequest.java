package com.smoothTalkAI.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationCreateRequest {

    @Size(max = 100, message = "제목은 100자를 넘을 수 없습니다.")
    private String title;
}

