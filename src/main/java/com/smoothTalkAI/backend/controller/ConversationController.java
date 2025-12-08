package com.smoothTalkAI.backend.controller;

import com.smoothTalkAI.backend.conversation.ConversationService;
import com.smoothTalkAI.backend.dto.ApiResponse;
import com.smoothTalkAI.backend.dto.ConversationCreateRequest;
import com.smoothTalkAI.backend.dto.ConversationDetailResponse;
import com.smoothTalkAI.backend.dto.ConversationSummaryResponse;
import com.smoothTalkAI.backend.dto.ConversationUpdateRequest;
import com.smoothTalkAI.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@Validated
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public ApiResponse<List<ConversationSummaryResponse>> list(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(conversationService.getConversations(principal.getId()));
    }

    @PostMapping
    public ApiResponse<ConversationSummaryResponse> create(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ConversationCreateRequest request
    ) {
        return ApiResponse.ok(conversationService.createConversation(principal.getId(), request));
    }

    @GetMapping("/{conversationId}")
    public ApiResponse<ConversationDetailResponse> detail(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long conversationId
    ) {
        return ApiResponse.ok(conversationService.getConversation(principal.getId(), conversationId));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long conversationId
    ) {
        conversationService.deleteConversation(principal.getId(), conversationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{conversationId}")
    public ApiResponse<ConversationSummaryResponse> updateTitle(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long conversationId,
        @Valid @RequestBody ConversationUpdateRequest request
    ) {
        return ApiResponse.ok(conversationService.updateConversationTitle(principal.getId(), conversationId, request.getTitle()));
    }
}

