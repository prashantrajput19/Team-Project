package com.examly.springapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.examly.springapp.model.ArchitectInquiry;

@Repository
public interface ArchitectInquiryRepo extends JpaRepository<ArchitectInquiry, Long> {
    List<ArchitectInquiry> findByArchitectArchitectId(Long architectId);

    List<ArchitectInquiry> findByDeletedFalse();

    List<ArchitectInquiry> findByUserUserIdAndDeletedFalse(Long userId);
}
