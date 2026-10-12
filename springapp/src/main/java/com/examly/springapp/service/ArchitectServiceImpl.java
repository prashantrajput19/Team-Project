package com.examly.springapp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.examly.springapp.exceptions.InvalidRequestException;
import com.examly.springapp.exceptions.ResourceNotFoundException;
import com.examly.springapp.model.Architect;
import com.examly.springapp.model.ArchitectInquiry;
import com.examly.springapp.repository.ArchitectInquiryRepo;
import com.examly.springapp.repository.ArchitectRepo;

@Service
public class ArchitectServiceImpl implements ArchitectService {
    private final ArchitectRepo architectRepo;

    private final ArchitectInquiryRepo architectInquiryRepo;

    public ArchitectServiceImpl(
        ArchitectRepo architectRepo,
        ArchitectInquiryRepo architectInquiryRepo) {
        this.architectRepo = architectRepo;
        this.architectInquiryRepo = architectInquiryRepo;
    }

    @Override
    public Architect addArchitect(Architect architect) {
        return architectRepo.save(architect);
    }

    @Override
    public Architect updateArchitect(Long architectId, Architect updatedArchitect) {
        getArchitectById(architectId); // throws an error if the architect does not exist
        updatedArchitect.setArchitectId(architectId);
        return architectRepo.save(updatedArchitect);
    }

    @Override
    public Architect getArchitectById(Long architectId) {
        return architectRepo.findById(architectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Architect with id " + architectId + " does not exist."));
    }

    @Override
    public List<Architect> getAllArchitects() {
        return architectRepo.findByDeletedFalse();
    }

    // Only an Inactive architect with no open inquiries can be deleted
    @Override
    public void deleteArchitect(Long architectId) {
        Architect architect = getArchitectById(architectId);

        if (!"Inactive".equals(architect.getStatus())) {
            throw new InvalidRequestException(
                    "Cannot delete an active architect. Mark the architect as Inactive first.");
        }

        int openInquiries = 0;
        for (ArchitectInquiry inquiry : architectInquiryRepo.findByArchitectArchitectId(architectId)) {
            if (!inquiry.isDeleted() && !"Closed".equals(inquiry.getStatus())) {
                openInquiries++;
            }
        }

        if (openInquiries > 0) {
            throw new InvalidRequestException("Cannot delete this architect: " + openInquiries
                    + " inquiry(s) are not closed yet. Close all inquiries first.");
        }

        architect.setDeleted(true);
        architectRepo.save(architect);
    }

    @Override
    public List<Architect> getTopRated() {
        return architectRepo.findTopRated();
    }

    @Override
    public List<String> getAllLocations() {
        return architectRepo.findAllLocations();
    }
}
