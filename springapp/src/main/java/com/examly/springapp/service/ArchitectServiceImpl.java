package com.examly.springapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.examly.springapp.exceptions.ArchitectException;
import com.examly.springapp.exceptions.InvalidRequestException;
import com.examly.springapp.model.Architect;
import com.examly.springapp.repository.ArchitectInquiryRepo;
import com.examly.springapp.repository.ArchitectRepo;

@Service
public class ArchitectServiceImpl implements ArchitectService {
    @Autowired
    private ArchitectRepo architectRepo;

    @Autowired
    private ArchitectInquiryRepo architectInquiryRepo;

    @Override
    public Architect addArchitect(Architect architect) {
        return architectRepo.save(architect);
    }

    @Override
    public Architect updateArchitect(Long architectId, Architect updatedArchitect) {
        Architect existing = getArchitectById(architectId);

        existing.setName(updatedArchitect.getName());
        existing.setExpertise(updatedArchitect.getExpertise());
        existing.setLocation(updatedArchitect.getLocation());
        existing.setRating(updatedArchitect.getRating());
        existing.setPortfolioLink(updatedArchitect.getPortfolioLink());
        existing.setStatus(updatedArchitect.getStatus());

        return architectRepo.save(existing);
    }

    @Override
    public Architect getArchitectById(Long architectId) {
        return architectRepo.findById(architectId)
                .orElseThrow(() -> new ArchitectException(
                        "Architect with id " + architectId + " does not exist."));
    }

    @Override
    public List<Architect> getAllArchitects() {
        return architectRepo.findByDeletedFalse();
    }

    @Override
    public void deleteArchitect(Long architectId) {
        Architect architect = getArchitectById(architectId);

        if (!"Inactive".equalsIgnoreCase(architect.getStatus())) {
            throw new InvalidRequestException(
                    "Cannot delete an active architect. Mark the architect as Inactive first.");
        }

        long openInquiries = architectInquiryRepo.findByArchitectArchitectId(architectId).stream()
                .filter(inquiry -> !inquiry.isDeleted())
                .filter(inquiry -> !"Closed".equalsIgnoreCase(inquiry.getStatus()))
                .count();

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
