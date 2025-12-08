package com.smoothTalkAI.backend.controller;

import com.smoothTalkAI.backend.analysis.AnalysisService;
import com.smoothTalkAI.backend.dto.AnalysisRequest;
import com.smoothTalkAI.backend.dto.AnalysisResponse;
import com.smoothTalkAI.backend.dto.ApiResponse;
import com.smoothTalkAI.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conversations/{conversationId}/analyze")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> analyze(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long conversationId,
            @Valid @RequestBody(required = false) AnalysisRequest request) {
        String analysisId = java.util.UUID.randomUUID().toString();
        Long userId = principal != null ? principal.getId() : null;

        analysisService.startAnalysis(analysisId, userId, conversationId, request);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("analysisId", analysisId);
        response.put("status", "PENDING");

        return org.springframework.http.ResponseEntity.accepted().body(ApiResponse.ok(response).getData());
    }
}
