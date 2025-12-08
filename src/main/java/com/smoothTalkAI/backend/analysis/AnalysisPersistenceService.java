package com.smoothTalkAI.backend.analysis;

import com.smoothTalkAI.backend.conversation.Conversation;
import com.smoothTalkAI.backend.conversation.ConversationRepository;
import com.smoothTalkAI.backend.dto.AnalysisResponse;
import com.smoothTalkAI.backend.exception.ApiException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Separate service to handle analysis persistence.
 * This ensures @Transactional works correctly when called from @Async methods.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisPersistenceService {

    private final ConversationRepository conversationRepository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAnalysis(Long userId, Long conversationId, AnalysisResponse response) {

        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> ApiException.notFound("대화를 찾을 수 없습니다."));

        conversation.applyAnalysis(
                response.getSummary(),
                response.getAdvice(),
                response.getSampleReplies(),
                response.getMessageFrequency(),
                response.getTimeFrequency());

        // Explicitly save and flush to ensure persistence
        Conversation saved = conversationRepository.save(conversation);
        entityManager.flush();

    }
}
