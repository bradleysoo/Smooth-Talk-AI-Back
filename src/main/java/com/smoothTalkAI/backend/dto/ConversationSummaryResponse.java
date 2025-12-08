package com.smoothTalkAI.backend.dto;

import com.smoothTalkAI.backend.conversation.Conversation;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConversationSummaryResponse {

    private Long id;
    private String title;
    private LocalDateTime updatedAt;

    public static ConversationSummaryResponse from(Conversation conversation) {
        return ConversationSummaryResponse.builder()
            .id(conversation.getId())
            .title(conversation.getTitle())
            .updatedAt(conversation.getUpdatedAt())
            .build();
    }
}

