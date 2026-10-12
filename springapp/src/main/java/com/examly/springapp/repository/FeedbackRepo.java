package com.examly.springapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.examly.springapp.model.Feedback;

@Repository
public interface FeedbackRepo extends JpaRepository<Feedback, Long> {
    List<Feedback> findByDeletedFalse();

    List<Feedback> findByUserUserIdAndDeletedFalse(Long userId);

    List<Feedback> findByArchitectArchitectIdAndDeletedFalse(Long architectId);
}
