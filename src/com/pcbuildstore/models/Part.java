package com.pcbuildstore.models;

public class Part {

    private int partId;
    private int categoryId;
    private String brand;
    private String name;
    private int price;
    private int performanceScore;
    private String socketType;
    private String ddrGeneration;
    private Integer coreCount;
    private String clockSpeed;
    private String vram;
    private String memorySpeed;
    private String capacity;
    private String readSpeed;
    private Integer wattage;
    private String efficiency;

    public Part(int partId, int categoryId, String brand, String name, int price,
                int performanceScore, String socketType, String ddrGeneration,
                Integer coreCount, String clockSpeed, String vram, String memorySpeed,
                String capacity, String readSpeed, Integer wattage, String efficiency) {
        this.partId = partId;
        this.categoryId = categoryId;
        this.brand = brand;
        this.name = name;
        this.price = price;
        this.performanceScore = performanceScore;
        this.socketType = socketType;
        this.ddrGeneration = ddrGeneration;
        this.coreCount = coreCount;
        this.clockSpeed = clockSpeed;
        this.vram = vram;
        this.memorySpeed = memorySpeed;
        this.capacity = capacity;
        this.readSpeed = readSpeed;
        this.wattage = wattage;
        this.efficiency = efficiency;
    }

    public int getPartId() { return partId; }
    public int getCategoryId() { return categoryId; }
    public String getBrand() { return brand; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getPerformanceScore() { return performanceScore; }
    public String getSocketType() { return socketType; }
    public String getDdrGeneration() { return ddrGeneration; }
    public Integer getCoreCount() { return coreCount; }
    public String getClockSpeed() { return clockSpeed; }
    public String getVram() { return vram; }
    public String getMemorySpeed() { return memorySpeed; }
    public String getCapacity() { return capacity; }
    public String getReadSpeed() { return readSpeed; }
    public Integer getWattage() { return wattage; }
    public String getEfficiency() { return efficiency; }

    @Override
    public String toString() { return brand + " " + name; }
}
