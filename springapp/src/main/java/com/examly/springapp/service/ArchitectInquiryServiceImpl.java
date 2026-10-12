package com.examly.springapp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.examly.springapp.exceptions.InvalidRequestException;
import com.examly.springapp.exceptions.ResourceNotFoundException;
import com.examly.springapp.model.ArchitectInquiry;
import com.examly.springapp.model.SubInquiry;
import com.examly.springapp.repository.ArchitectInquiryRepo;
import com.examly.springapp.repository.ArchitectRepo;
import com.examly.springapp.repository.UserRepo;

@Service
public class ArchitectInquiryServiceImpl implements ArchitectInquiryService {
    private final ArchitectInquiryRepo inquiryRepo;

    private final UserRepo userRepo;

    private final ArchitectRepo architectRepo;

    public ArchitectInquiryServiceImpl(
        ArchitectInquiryRepo inquiryRepo,
        UserRepo userRepo,
        ArchitectRepo architectRepo) {
        this.inquiryRepo = inquiryRepo;
        this.userRepo = userRepo;
        this.architectRepo = architectRepo;
    }

    @Override
    public ArchitectInquiry createInquiry(ArchitectInquiry inquiry) {
        Long userId = inquiry.getUser().getUserId();
        Long architectId = inquiry.getArchitect().getArchitectId();

        inquiry.setUser(userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " does not exist.")));
        inquiry.setArchitect(architectRepo.findById(architectId)
                .orElseThrow(() -> new ResourceNotFoundException("Architect with id " + architectId + " does not exist.")));

        if ("Inactive".equals(inquiry.getArchitect().getStatus())) {
            throw new InvalidRequestException("This architect is not taking new inquiries right now.");
        }

        return inquiryRepo.save(inquiry);
    }

    // Admin can only change the status of an inquiry
    @Override
    public ArchitectInquiry updateInquiry(Long inquiryId, ArchitectInquiry updated) {
        ArchitectInquiry existing = getInquiryById(inquiryId);
        existing.setStatus(updated.getStatus());
        return inquiryRepo.save(existing);
    }

    @Override
    public List<ArchitectInquiry> getInquiriesByUserId(Long userId) {
        return inquiryRepo.findByUserUserIdAndDeletedFalse(userId);
    }

    @Override
    public List<ArchitectInquiry> getAllInquiries() {
        return inquiryRepo.findByDeletedFalse();
    }

    @Override
    public void deleteInquiry(Long inquiryId) {
        ArchitectInquiry inquiry = getInquiryById(inquiryId);
        inquiry.setDeleted(true);
        inquiryRepo.save(inquiry);
    }

    // Adds a follow-up message (from the user or the admin) to an inquiry
    @Override
    public ArchitectInquiry addSubInquiry(Long inquiryId, String message, String senderEmail, boolean fromAdmin) {
        ArchitectInquiry inquiry = getInquiryById(inquiryId);

        if (!fromAdmin && !inquiry.getUser().getEmail().equals(senderEmail)) {
            throw new InvalidRequestException("You can only follow up on your own inquiries.");
        }
        if ("Closed".equals(inquiry.getStatus())) {
            throw new InvalidRequestException("This inquiry is closed. Please send a new inquiry instead.");
        }

        SubInquiry subInquiry = new SubInquiry();
        subInquiry.setMessage(message.trim());
        subInquiry.setSender(fromAdmin ? "Admin" : "User");
        subInquiry.setCreatedAt(LocalDateTime.now().withNano(0).toString());
        inquiry.getSubInquiries().add(subInquiry);

        return inquiryRepo.save(inquiry);
    }

    private ArchitectInquiry getInquiryById(Long inquiryId) {
        ArchitectInquiry inquiry = inquiryRepo.findById(inquiryId).orElse(null);
        if (inquiry == null || inquiry.isDeleted()) {
            throw new ResourceNotFoundException("Inquiry with id " + inquiryId + " does not exist.");
        }
        return inquiry;
    }
}
