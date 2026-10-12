package com.examly.springapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.examly.springapp.dto.SubInquiryRequest;
import com.examly.springapp.model.ArchitectInquiry;
import com.examly.springapp.service.ArchitectInquiryService;

@RestController
@RequestMapping("/api/inquiries")
public class ArchitectInquiryController {
    private final ArchitectInquiryService inquiryService;

    public ArchitectInquiryController(ArchitectInquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArchitectInquiry addInquiry(@RequestBody ArchitectInquiry inquiry) {
        return inquiryService.createInquiry(inquiry);
    }

    @GetMapping
    public List<ArchitectInquiry> getAllInquiries() {
        return inquiryService.getAllInquiries();
    }

    @GetMapping("/user/{userId}")
    public List<ArchitectInquiry> getInquiriesByUserId(@PathVariable Long userId) {
        return inquiryService.getInquiriesByUserId(userId);
    }

    @PutMapping("/{inquiryId}")
    public ArchitectInquiry updateInquiry(@PathVariable Long inquiryId,
                                          @RequestBody ArchitectInquiry inquiry) {
        return inquiryService.updateInquiry(inquiryId, inquiry);
    }

    @DeleteMapping("/{inquiryId}")
    public void deleteInquiry(@PathVariable Long inquiryId) {
        inquiryService.deleteInquiry(inquiryId);
    }

    @PostMapping("/{inquiryId}/subinquiries")
    @ResponseStatus(HttpStatus.CREATED)
    public ArchitectInquiry addSubInquiry(@PathVariable Long inquiryId,
                                          @RequestBody SubInquiryRequest request,
                                          Authentication auth) {
        boolean fromAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_Admin"));
        return inquiryService.addSubInquiry(inquiryId, request.message(), auth.getName(), fromAdmin);
    }
}