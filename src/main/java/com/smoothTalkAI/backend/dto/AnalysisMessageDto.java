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
public class AnalysisMessageDto {

    @NotNull
    private SenderType sender;

    @NotBlank
    private String text;

    private String timeLabel;
}

