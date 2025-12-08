package com.smoothTalkAI.backend.controller;

import com.smoothTalkAI.backend.analysis.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalysisEventController {

    private final AnalysisService analysisService;

    @GetMapping("/subscribe/{analysisId}")
    public SseEmitter subscribe(@PathVariable String analysisId) {
        return analysisService.subscribe(analysisId);
    }
}
