package com.pcbuildstore.models;

public class GPUOption {

    private int gpuOptionId;
    private int gpuPartId;
    private int forBudget;
    private int priceIncrease;
    private int performanceIncrease;
    private String gpuName;
    private String gpuBrand;

    public GPUOption(int gpuOptionId, int gpuPartId, int forBudget, int priceIncrease,
                     int performanceIncrease) {
        this.gpuOptionId = gpuOptionId;
        this.gpuPartId = gpuPartId;
        this.forBudget = forBudget;
        this.priceIncrease = priceIncrease;
        this.performanceIncrease = performanceIncrease;
    }

    public GPUOption(int gpuOptionId, int gpuPartId, int forBudget, int priceIncrease,
                     int performanceIncrease, String gpuName, String gpuBrand) {
        this(gpuOptionId, gpuPartId, forBudget, priceIncrease, performanceIncrease);
        this.gpuName = gpuName;
        this.gpuBrand = gpuBrand;
    }

    public int getGpuOptionId() { return gpuOptionId; }
    public int getGpuPartId() { return gpuPartId; }
    public int getForBudget() { return forBudget; }
    public int getPriceIncrease() { return priceIncrease; }
    public int getPerformanceIncrease() { return performanceIncrease; }
    public String getGpuName() { return gpuName; }
    public String getGpuBrand() { return gpuBrand; }
}
