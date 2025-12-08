package com.smoothTalkAI.backend.message;

import com.smoothTalkAI.backend.conversation.Conversation;
import com.smoothTalkAI.backend.conversation.ConversationRepository;
import com.smoothTalkAI.backend.dto.MessageRequest;
import com.smoothTalkAI.backend.dto.MessageResponse;
import com.smoothTalkAI.backend.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public MessageResponse addMessage(Long userId, Long conversationId, MessageRequest request) {
        Conversation conversation = getOwnedConversation(userId, conversationId);
        boolean firstMessage = messageRepository.countByConversationId(conversationId) == 0;

        Message message = Message.builder()
                .conversation(conversation)
                .sender(request.getSender())
                .text(request.getText())
                .timeLabel(request.getTimeLabel())
                .build();

        Message saved = messageRepository.save(message);

        // 첫 메시지여부와 상관없이 수정일만 업데이트 (제목 자동 변경 로직 완전 제거)
        conversation.markUpdated();

        return MessageResponse.from(saved);
    }

    @Transactional
    public void deleteMessage(Long userId, Long conversationId, Long messageId) {
        Conversation conversation = getOwnedConversation(userId, conversationId);
        Message message = messageRepository.findByIdAndConversationId(messageId, conversation.getId())
                .orElseThrow(() -> ApiException.notFound("메시지를 찾을 수 없습니다."));
        messageRepository.delete(message);
        conversation.markUpdated();
    }

    @Transactional
    public void clearMessages(Long userId, Long conversationId) {
        Conversation conversation = getOwnedConversation(userId, conversationId);
        messageRepository.deleteByConversationId(conversation.getId());
        conversation.markUpdated();
    }

    @Transactional
    public void updateMessageTime(Long userId, Long conversationId, Long messageId, String newTime) {
        Conversation conversation = getOwnedConversation(userId, conversationId);
        Message message = messageRepository.findByIdAndConversationId(messageId, conversation.getId())
                .orElseThrow(() -> ApiException.notFound("메시지를 찾을 수 없습니다."));

        message.updateTimeLabel(newTime);
        conversation.markUpdated();
    }

    private Conversation getOwnedConversation(Long userId, Long conversationId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> ApiException.notFound("대화를 찾을 수 없습니다."));
    }
}
