package com.examly.springapp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.examly.springapp.exceptions.ResourceNotFoundException;
import com.examly.springapp.model.Architect;
import com.examly.springapp.model.Feedback;
import com.examly.springapp.repository.ArchitectRepo;
import com.examly.springapp.repository.FeedbackRepo;
import com.examly.springapp.repository.UserRepo;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepo feedbackRepo;

    private final UserRepo userRepo;

    private final ArchitectRepo architectRepo;

    public FeedbackServiceImpl(
        FeedbackRepo feedbackRepo,
        UserRepo userRepo,
        ArchitectRepo architectRepo) {
        this.feedbackRepo = feedbackRepo;
        this.userRepo = userRepo;
        this.architectRepo = architectRepo;
    }

    @Override
    public Feedback createFeedback(Feedback feedback) {
        Long userId = feedback.getUser().getUserId();
        Long architectId = feedback.getArchitect().getArchitectId();

        feedback.setUser(userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " does not exist.")));
        feedback.setArchitect(architectRepo.findById(architectId)
                .orElseThrow(() -> new ResourceNotFoundException("Architect with id " + architectId + " does not exist.")));

        Feedback saved = feedbackRepo.save(feedback);
        updateArchitectRating(saved.getArchitect());
        return saved;
    }

    // Architect rating = average of all ratings in that architect's feedback
    private void updateArchitectRating(Architect architect) {
        List<Feedback> feedbacks = feedbackRepo.findByArchitectArchitectIdAndDeletedFalse(architect.getArchitectId());
        int total = 0;
        int count = 0;
        for (Feedback f : feedbacks) {
            if (f.getRating() != null) {
                total = total + f.getRating();
                count = count + 1;
            }
        }

        // Show a rating only after at least 3 feedbacks; until then rating stays 0
        // and the app shows "Rising Star" instead.
        double average = 0;
        if (count >= 3) {
            average = Math.round(total * 10.0 / count) / 10.0; // keep 1 decimal, e.g. 4.3
        }

        architect.setRating(average);
        architectRepo.save(architect);
    }

    @Override
    public Feedback getFeedbackById(Long feedbackId) {
        Feedback feedback = feedbackRepo.findById(feedbackId).orElse(null);
        if (feedback == null || feedback.isDeleted()) {
            throw new ResourceNotFoundException("Feedback with id " + feedbackId + " does not exist.");
        }
        return feedback;
    }

    @Override
    public List<Feedback> getAllFeedback() {
        return feedbackRepo.findByDeletedFalse();
    }

    // For admin: every feedback, also the deleted ones
    @Override
    public List<Feedback> getAllFeedbackIncludingDeleted() {
        return feedbackRepo.findAll();
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
        updateArchitectRating(feedback.getArchitect());
    }
}
