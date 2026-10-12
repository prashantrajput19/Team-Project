package com.examly.springapp.dto;

// Asks the AI to write a text for a form.
// type is one of: "feedback", "inquiry", "followup", "reply"
public record AiWriteRequest(String type, String notes, Long architectId, Long inquiryId,
        Integer rating, String category) {
}
