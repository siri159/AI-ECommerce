package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AIChatRequest;
import com.ecommerce.backend.dto.AIChatResponse;
import com.ecommerce.backend.dto.RecommendationResponse;
import com.ecommerce.backend.service.AIService;
import com.ecommerce.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/ai/chat")
    public ResponseEntity<ApiResponse<AIChatResponse>> chat(Principal principal, @Valid @RequestBody AIChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success("AI response generated successfully",
                aiService.chat(request.getMessage(), principal != null ? principal.getName() : null)));
    }

    @GetMapping("/ai/recommendations")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getRecommendations(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Recommendations generated successfully",
                aiService.getRecommendations(principal != null ? principal.getName() : null)));
    }
}
