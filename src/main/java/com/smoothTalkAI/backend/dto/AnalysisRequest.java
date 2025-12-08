package com.smoothTalkAI.backend.dto;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {

    @Valid
    private List<AnalysisMessageDto> messages;
}

