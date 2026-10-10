package com.examly.springapp.service;

import java.util.List;

import com.examly.springapp.model.Feedback;

public interface FeedbackService {
    Feedback createFeedback(Feedback feedback);

    Feedback getFeedbackById(Long feedbackId);

    List<Feedback> getAllFeedback();

    List<Feedback> getFeedbackByUserId(Long userId);

    void deleteFeedback(Long feedbackId);
}