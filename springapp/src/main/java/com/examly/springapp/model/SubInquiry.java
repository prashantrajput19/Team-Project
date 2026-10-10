package com.examly.springapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class SubInquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subInquiryId;

    private String message;

    private String createdAt;

    private String sender;

    public SubInquiry() {
    }

    public Long getSubInquiryId() {
        return subInquiryId;
    }

    public void setSubInquiryId(Long subInquiryId) {
        this.subInquiryId = subInquiryId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}