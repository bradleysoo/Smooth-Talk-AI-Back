package com.smoothTalkAI.backend.conversation;

import com.smoothTalkAI.backend.dto.ConversationCreateRequest;
import com.smoothTalkAI.backend.dto.ConversationDetailResponse;
import com.smoothTalkAI.backend.dto.ConversationSummaryResponse;
import com.smoothTalkAI.backend.dto.MessageResponse;
import com.smoothTalkAI.backend.exception.ApiException;
import com.smoothTalkAI.backend.message.MessageRepository;
import com.smoothTalkAI.backend.user.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;

    @Transactional
    public List<ConversationSummaryResponse> getConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        
        if (conversations.isEmpty()) {
            Conversation defaultConversation = createDefaultConversation(userId);
            conversations = List.of(defaultConversation);
        }

        return conversations.stream()
            .map(ConversationSummaryResponse::from)
            .toList();
    }

    public ConversationDetailResponse getConversation(Long userId, Long conversationId) {
        Conversation conversation = findOwnedConversation(userId, conversationId);
        List<MessageResponse> messages = messageRepository
            .findByConversationIdOrderByCreatedAtAsc(conversation.getId())
            .stream()
            .map(MessageResponse::from)
            .toList();

        return ConversationDetailResponse.of(conversation, messages);
    }

    @Transactional
    public ConversationSummaryResponse createConversation(Long userId, ConversationCreateRequest request) {
        var user = userService.getUserOrThrow(userId);
        String requestedTitle = request != null ? request.getTitle() : null;
        String title = StringUtils.hasText(requestedTitle) ? requestedTitle : "새 대화";
        Conversation conversation = Conversation.builder()
            .user(user)
            .title(title)
            .build();
        Conversation saved = conversationRepository.save(conversation);
        return ConversationSummaryResponse.from(saved);
    }

    @Transactional
    public void deleteConversation(Long userId, Long conversationId) {
        Conversation conversation = findOwnedConversation(userId, conversationId);
        conversationRepository.delete(conversation);
        
        // Ensure at least one conversation remains
        if (conversationRepository.countByUserId(userId) == 0) {
            createDefaultConversation(userId);
        }
    }

    @Transactional
    public ConversationSummaryResponse updateConversationTitle(Long userId, Long conversationId, String newTitle) {
        Conversation conversation = findOwnedConversation(userId, conversationId);
        conversation.changeTitle(newTitle);
        return ConversationSummaryResponse.from(conversation);
    }

    private Conversation createDefaultConversation(Long userId) {
        var user = userService.getUserOrThrow(userId);
        Conversation conversation = Conversation.builder()
            .user(user)
            .title("새 대화")
            .build();
        return conversationRepository.save(conversation);
    }

    Conversation findOwnedConversation(Long userId, Long conversationId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> ApiException.notFound("대화를 찾을 수 없습니다."));
    }
}

