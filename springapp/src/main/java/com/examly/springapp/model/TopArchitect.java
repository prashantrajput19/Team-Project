package com.examly.springapp.model;

public class TopArchitect {
    private final Architect architect;

    private final double score;

    private final Double userAverage;

    private final int reviews;

    public TopArchitect(Architect architect, double score, Double userAverage, int reviews) {
        this.architect = architect;
        this.score = score;
        this.userAverage = userAverage;
        this.reviews = reviews;
    }

    public Architect getArchitect() {
        return architect;
    }

    public double getScore() {
        return score;
    }

    public Double getUserAverage() {
        return userAverage;
    }

    public int getReviews() {
        return reviews;
    }
}