package com.examly.springapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.examly.springapp.exceptions.ArchitectInquiryException;
import com.examly.springapp.exceptions.ResourceNotFoundException;
import com.examly.springapp.model.Feedback;
import com.examly.springapp.repository.ArchitectRepo;
import com.examly.springapp.repository.FeedbackRepo;
import com.examly.springapp.repository.UserRepo;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    @Autowired
    private FeedbackRepo feedbackRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ArchitectRepo architectRepo;

    @Override
    public Feedback createFeedback(Feedback feedback) {
        if (feedback.getUser() == null || feedback.getUser().getUserId() == null) {
            throw new ArchitectInquiryException("Feedback must name the user leaving it.");
        }

        Long userId = feedback.getUser().getUserId();
        feedback.setUser(userRepo.findById(userId)
                .orElseThrow(() -> new ArchitectInquiryException(
                        "User with id " + userId + " does not exist.")));

        if (feedback.getArchitect() != null && feedback.getArchitect().getArchitectId() != null) {
            Long architectId = feedback.getArchitect().getArchitectId();
            feedback.setArchitect(architectRepo.findById(architectId)
                    .orElseThrow(() -> new ArchitectInquiryException(
                            "Architect with id " + architectId + " does not exist.")));
        } 

        Integer rating = feedback.getRating();
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new ArchitectInquiryException("Rating must be between 1 and 5.");
        }

        return feedbackRepo.save(feedback);
    }

    @Override
    public Feedback getFeedbackById(Long feedbackId) {
        return feedbackRepo.findById(feedbackId)
                .filter(feedback -> !feedback.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feedback with id " + feedbackId + " does not exist."));
    }

    @Override
    public List<Feedback> getAllFeedback() {
        return feedbackRepo.findByDeletedFalse();
    }

    @Override
    public List<Feedback> getFeedbackByUserId(Long userId) {
        return feedbackRepo.findByUserUserIdAndDeletedFalse(userId);
    }

    @Override
    public void deleteFeedback(Long feedbackId) {
        Feedback feedback = getFeedbackById(feedbackId);
        feedback.setDeleted(true);
        feedbackRepo.save(feedback);
    }
}
