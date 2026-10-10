package com.examly.springapp.service;

import java.util.List;

import com.examly.springapp.model.ArchitectInquiry;

public interface ArchitectInquiryService {
    ArchitectInquiry createInquiry(ArchitectInquiry architectInquiry);

    ArchitectInquiry updateInquiry(Long inquiryId, ArchitectInquiry updatedInquiry);

    List<ArchitectInquiry> getInquiriesByUserId(Long userId);

    List<ArchitectInquiry> getAllInquiries();

    void deleteInquiry(Long inquiryId);

    ArchitectInquiry addSubInquiry(Long inquiryId, String message, String senderEmail, boolean fromAdmin);
}