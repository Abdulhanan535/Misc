package com.pcbuildstore.models;

import java.time.LocalDateTime;

public class Build {

    private int buildId;
    private String name;
    private int totalPrice;
    private int totalScore;
    private LocalDateTime createdAt;

    public Build(int buildId, String name, int totalPrice, int totalScore, LocalDateTime createdAt) {
        this.buildId = buildId;
        this.name = name;
        this.totalPrice = totalPrice;
        this.totalScore = totalScore;
        this.createdAt = createdAt;
    }

    public int getBuildId() { return buildId; }
    public String getName() { return name; }
    public int getTotalPrice() { return totalPrice; }
    public int getTotalScore() { return totalScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    @Override
    public String toString() { return name + " (PKR " + totalPrice + ")"; }
}
