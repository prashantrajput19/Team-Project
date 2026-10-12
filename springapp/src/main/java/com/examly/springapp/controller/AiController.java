package com.examly.springapp.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examly.springapp.dto.AiChatRequest;
import com.examly.springapp.dto.AiWriteRequest;
import com.examly.springapp.service.AiService;

// Endpoints for "Archie", the AI assistant. All of them need a logged-in user.
@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    // Is the AI ready? (false when no Groq key is set)
    @GetMapping("/status")
    public Map<String, Boolean> getStatus() {
        return Map.of("enabled", aiService.isEnabled());
    }

    // Chat with Archie
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody AiChatRequest request, Authentication auth) {
        return aiService.chat(request.messages(), auth.getName(), isAdmin(auth));
    }

    // "Write with AI" button on the forms
    @PostMapping("/write")
    public Map<String, String> write(@RequestBody AiWriteRequest request, Authentication auth) {
        return Map.of("text", aiService.write(request, auth.getName(), isAdmin(auth)));
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_Admin"));
    }
}
