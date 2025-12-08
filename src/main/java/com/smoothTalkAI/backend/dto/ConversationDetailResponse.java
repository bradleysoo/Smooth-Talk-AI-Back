package com.smoothTalkAI.backend.dto;

import com.smoothTalkAI.backend.conversation.Conversation;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConversationDetailResponse {

    private Long id;
    private String title;
    private LocalDateTime updatedAt;
    private List<MessageResponse> messages;
    private ConversationAnalysisDto analysis;

    public static ConversationDetailResponse of(
        Conversation conversation,
        List<MessageResponse> messages
    ) {
        return ConversationDetailResponse.builder()
            .id(conversation.getId())
            .title(conversation.getTitle())
            .updatedAt(conversation.getUpdatedAt())
            .messages(messages)
            .analysis(ConversationAnalysisDto.from(conversation))
            .build();
    }
}

