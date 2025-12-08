package com.smoothTalkAI.backend.dto;

import com.smoothTalkAI.backend.message.Message;
import com.smoothTalkAI.backend.message.SenderType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageResponse {

    private Long id;
    private SenderType sender;
    private String text;
    private String timeLabel;
    private LocalDateTime createdAt;

    public static MessageResponse from(Message message) {
        return MessageResponse.builder()
            .id(message.getId())
            .sender(message.getSender())
            .text(message.getText())
            .timeLabel(message.getTimeLabel())
            .createdAt(message.getCreatedAt())
            .build();
    }
}

