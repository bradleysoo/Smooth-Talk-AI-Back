package com.smoothTalkAI.backend.dto;

import com.smoothTalkAI.backend.message.SenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {

    @NotNull(message = "발신자를 입력하세요.")
    private SenderType sender;

    @NotBlank(message = "메시지 내용을 입력하세요.")
    private String text;

    private String timeLabel;
}

