package com.smoothTalkAI.backend.dto;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalysisResponse {

    private Long conversationId;
    private String summary;
    private List<String> advice;
    private List<String> sampleReplies;
    private Map<String, Integer> messageFrequency;
    private Map<String, Integer> timeFrequency;
}
