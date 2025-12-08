package com.smoothTalkAI.backend.dto;

import com.smoothTalkAI.backend.conversation.Conversation;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConversationAnalysisDto {

    private String summary;
    private List<String> advice;
    private List<String> sampleReplies;
    private Map<String, Integer> messageFrequency;

    public static ConversationAnalysisDto from(Conversation conversation) {
        if (conversation.getAnalysisSummary() == null
                && conversation.getAnalysisAdvice() == null
                && conversation.getAnalysisSampleReplies() == null) {
            return null;
        }
        return ConversationAnalysisDto.builder()
                .summary(conversation.getAnalysisSummary())
                .advice(conversation.getAnalysisAdvice())
                .sampleReplies(conversation.getAnalysisSampleReplies())
                .messageFrequency(conversation.getAnalysisMessageFrequency())
                .build();
    }
}
