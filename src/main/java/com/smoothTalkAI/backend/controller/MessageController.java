package com.smoothTalkAI.backend.controller;

import com.smoothTalkAI.backend.dto.ApiResponse;
import com.smoothTalkAI.backend.dto.MessageRequest;
import com.smoothTalkAI.backend.dto.MessageResponse;
import com.smoothTalkAI.backend.message.MessageService;
import com.smoothTalkAI.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conversations/{conversationId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ApiResponse<MessageResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long conversationId,
            @Valid @RequestBody MessageRequest request) {
        return ApiResponse.ok(messageService.addMessage(principal.getId(), conversationId, request));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long conversationId,
            @PathVariable Long messageId) {
        messageService.deleteMessage(principal.getId(), conversationId, messageId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long conversationId) {
        messageService.clearMessages(principal.getId(), conversationId);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{messageId}")
    public ResponseEntity<Void> updateTime(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @RequestBody java.util.Map<String, String> request) {
        String newTime = request.get("time");
        messageService.updateMessageTime(principal.getId(), conversationId, messageId, newTime);
        return ResponseEntity.ok().build();
    }
}
