package com.examly.springapp.dto;

import java.util.List;
import java.util.Map;

// The chat so far. Each message looks like: { "role": "user" or "assistant", "content": "..." }
public record AiChatRequest(List<Map<String, String>> messages) {
}
