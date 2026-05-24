package com.pcbuildstore.models;

import java.time.LocalDateTime;

public class Bill {

    private int billId;
    private int buildId;
    private int finalPrice;
    private int finalScore;
    private LocalDateTime purchaseDate;
    private String buildName;

    public Bill(int billId, int buildId, int finalPrice, int finalScore, LocalDateTime purchaseDate) {
        this.billId = billId;
        this.buildId = buildId;
        this.finalPrice = finalPrice;
        this.finalScore = finalScore;
        this.purchaseDate = purchaseDate;
    }

    public Bill(int billId, int buildId, int finalPrice, int finalScore, LocalDateTime purchaseDate,
                String buildName) {
        this(billId, buildId, finalPrice, finalScore, purchaseDate);
        this.buildName = buildName;
    }

    public int getBillId() { return billId; }
    public int getBuildId() { return buildId; }
    public int getFinalPrice() { return finalPrice; }
    public int getFinalScore() { return finalScore; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public String getBuildName() { return buildName; }
}
