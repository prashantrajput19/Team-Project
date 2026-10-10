package com.examly.springapp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.examly.springapp.exceptions.ArchitectInquiryException;
import com.examly.springapp.exceptions.ResourceNotFoundException;
import com.examly.springapp.model.ArchitectInquiry;
import com.examly.springapp.model.SubInquiry;
import com.examly.springapp.repository.ArchitectInquiryRepo;
import com.examly.springapp.repository.ArchitectRepo;
import com.examly.springapp.repository.UserRepo;

@Service
public class ArchitectInquiryServiceImpl implements ArchitectInquiryService {
    @Autowired
    private ArchitectInquiryRepo inquiryRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ArchitectRepo architectRepo;

    @Override
    public ArchitectInquiry createInquiry(ArchitectInquiry inquiry) {
        if (inquiry.getUser() == null || inquiry.getUser().getUserId() == null
                || inquiry.getArchitect() == null || inquiry.getArchitect().getArchitectId() == null) {
            throw new ArchitectInquiryException("An inquiry must name both a user and an architect.");
        }

        Long userId = inquiry.getUser().getUserId();
        Long architectId = inquiry.getArchitect().getArchitectId();

        inquiry.setUser(userRepo.findById(userId)
                .orElseThrow(() -> new ArchitectInquiryException(
                        "User with id " + userId + " does not exist.")));
        inquiry.setArchitect(architectRepo.findById(architectId)
                .orElseThrow(() -> new ArchitectInquiryException(
                        "Architect with id " + architectId + " does not exist.")));

        if ("Inactive".equals(inquiry.getArchitect().getStatus())) {
            throw new ArchitectInquiryException("This architect is not taking new inquiries right now.");
        }

        inquiry.setSubInquiries(new ArrayList<>());

        return inquiryRepo.save(inquiry);
    }

    @Override
    public ArchitectInquiry updateInquiry(Long inquiryId, ArchitectInquiry updated) {
        ArchitectInquiry existing = getInquiryById(inquiryId);

        if (updated.getStatus() == null || updated.getStatus().isBlank()) {
            throw new ArchitectInquiryException("Status cannot be empty.");
        }
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

    @Override
    public ArchitectInquiry addSubInquiry(Long inquiryId, String message, String senderEmail, boolean fromAdmin) {
        ArchitectInquiry inquiry = getInquiryById(inquiryId);

        if (!fromAdmin && !inquiry.getUser().getEmail().equals(senderEmail)) {
            throw new ArchitectInquiryException("You can only follow up on your own inquiries.");
        }
        if (message == null || message.isBlank()) {
            throw new ArchitectInquiryException("Follow-up message cannot be empty.");
        }
        if ("Closed".equals(inquiry.getStatus())) {
            throw new ArchitectInquiryException("This inquiry is closed. Please send a new inquiry instead.");
        }

        SubInquiry subInquiry = new SubInquiry();
        subInquiry.setMessage(message.trim());
        subInquiry.setSender(fromAdmin ? "Admin" : "User");
        subInquiry.setCreatedAt(LocalDateTime.now().withNano(0).toString());
        inquiry.getSubInquiries().add(subInquiry);

        return inquiryRepo.save(inquiry);
    }

    private ArchitectInquiry getInquiryById(Long inquiryId) {
        return inquiryRepo.findById(inquiryId)
                .filter(inquiry -> !inquiry.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inquiry with id " + inquiryId + " does not exist."));
    }
}
