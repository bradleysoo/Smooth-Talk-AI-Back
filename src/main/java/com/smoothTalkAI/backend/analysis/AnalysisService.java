package com.smoothTalkAI.backend.analysis;

import com.smoothTalkAI.backend.dto.AnalysisMessageDto;
import com.smoothTalkAI.backend.dto.AnalysisRequest;
import com.smoothTalkAI.backend.dto.AnalysisResponse;
import com.smoothTalkAI.backend.message.MessageRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    private final MessageRepository messageRepository;
    private final RestTemplate restTemplate;
    private final AnalysisPersistenceService analysisPersistenceService;
    private final com.smoothTalkAI.backend.user.UserService userService;

    private final Map<String, SseEmitter> emitters = new java.util.concurrent.ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Value("${app.python.analyze-url}")
    private String pythonUrl;

    public SseEmitter subscribe(String analysisId) {
        SseEmitter emitter = new SseEmitter(60 * 1000L); // 1 minute timeout
        emitters.put(analysisId, emitter);

        emitter.onCompletion(() -> emitters.remove(analysisId));
        emitter.onTimeout(() -> emitters.remove(analysisId));
        emitter.onError((e) -> emitters.remove(analysisId));

        return emitter;
    }

    @org.springframework.scheduling.annotation.Async
    public void startAnalysis(String analysisId, Long userId, Long conversationId, AnalysisRequest request) {
        try {
            // 1. Resolve messages (from request or DB)
            List<AnalysisMessageDto> messages = resolveMessages(conversationId, request);
            AnalysisRequest payload = new AnalysisRequest(messages);

            // 2. Call Python API
            log.info("Sending analysis request to Python: {}", pythonUrl);
            AnalysisResponse response = restTemplate.postForObject(pythonUrl, payload, AnalysisResponse.class);

            if (response == null) {
                throw new RuntimeException("Received null response from Python server");
            }

            // 3. Save to DB if persisted conversation (using separate service to ensure
            // transaction proxy works)
            if (userId != null && conversationId != null) {
                analysisPersistenceService.saveAnalysis(userId, conversationId, response);

                // Token deduction after successful analysis (using transactional method)
                boolean deducted = userService.deductTokenForUser(userId);
                if (deducted) {
                    com.smoothTalkAI.backend.user.User user = userService.getUserOrThrow(userId);
                    log.info("✅ Deducted 1 token from user {}. Remaining: {}", userId, user.getTokenBalance());
                } else {
                    log.warn("⚠️ Failed to deduct token from user {} (insufficient balance)", userId);
                }

                // Ensure response has conversationId
                response = AnalysisResponse.builder()
                        .conversationId(conversationId)
                        .summary(response.getSummary())
                        .advice(response.getAdvice())
                        .sampleReplies(response.getSampleReplies())
                        .messageFrequency(response.getMessageFrequency())
                        .timeFrequency(response.getTimeFrequency())
                        .build();
            }

            // 4. Send SSE
            SseEmitter emitter = emitters.get(analysisId);
            if (emitter != null) {
                emitter.send(SseEmitter.event().name("complete").data(response));
                emitter.complete();
            }
        } catch (Exception e) {
            log.error("Analysis failed", e);
            SseEmitter emitter = emitters.get(analysisId);
            if (emitter != null) {
                emitter.completeWithError(e);
            }
        }
    }

    private List<AnalysisMessageDto> resolveMessages(Long conversationId, AnalysisRequest request) {
        if (request != null && !CollectionUtils.isEmpty(request.getMessages())) {
            return request.getMessages();
        }
        if (conversationId == null) {
            return List.of();
        }
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(message -> AnalysisMessageDto.builder()
                        .sender(message.getSender())
                        .text(message.getText())
                        .timeLabel(message.getTimeLabel())
                        .build())
                .toList();
    }
}
