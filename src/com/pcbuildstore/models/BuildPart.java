package com.pcbuildstore.models;

public class BuildPart {

    private int id;
    private int buildId;
    private int categoryId;
    private int partId;
    private int priceAtAdd;
    private String categoryName;
    private String partName;
    private String partBrand;

    public BuildPart(int id, int buildId, int categoryId, int partId, int priceAtAdd) {
        this.id = id;
        this.buildId = buildId;
        this.categoryId = categoryId;
        this.partId = partId;
        this.priceAtAdd = priceAtAdd;
    }

    public BuildPart(int id, int buildId, int categoryId, int partId, int priceAtAdd,
                     String categoryName, String partName, String partBrand) {
        this(id, buildId, categoryId, partId, priceAtAdd);
        this.categoryName = categoryName;
        this.partName = partName;
        this.partBrand = partBrand;
    }

    public int getId() { return id; }
    public int getBuildId() { return buildId; }
    public int getCategoryId() { return categoryId; }
    public int getPartId() { return partId; }
    public int getPriceAtAdd() { return priceAtAdd; }
    public String getCategoryName() { return categoryName; }
    public String getPartName() { return partName; }
    public String getPartBrand() { return partBrand; }
}
