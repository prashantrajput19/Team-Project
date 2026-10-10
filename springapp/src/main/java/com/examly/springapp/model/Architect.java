package com.examly.springapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Architect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long architectId;

    private String name;

    private String expertise;

    private String location;

    private Double rating;

    private String portfolioLink;

    private String status;

    private boolean deleted = false;

    public Architect() {
    }

    public Architect(Long architectId, String name, String expertise, String location, Double rating,
            String portfolioLink, String status) {
        this.architectId = architectId;
        this.name = name;
        this.expertise = expertise;
        this.location = location;
        this.rating = rating;
        this.portfolioLink = portfolioLink;
        this.status = status;
    }

    public Long getArchitectId() {
        return architectId;
    }

    public void setArchitectId(Long architectId) {
        this.architectId = architectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getPortfolioLink() {
        return portfolioLink;
    }

    public void setPortfolioLink(String portfolioLink) {
        this.portfolioLink = portfolioLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
