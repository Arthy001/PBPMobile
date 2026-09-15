package com.example.uhf_bt.models;

/**
 * Lightweight version of ProductItem for high-volume scanning performance.
 * Reduced from 30+ fields to only essential ones needed for inventory.
 */
public class ProductItemLite {
    private String productName;
    private String productNumber;
    private String productGroupName;
    private String tagNumber;
    private String lotNumber;
    private String warehouseName;
    private Integer productItemStatus;
    private boolean isScanned = false;
    private String lastUpdateDate;
    private boolean isNotDeleted = true;

    // Getters and Setters
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductNumber() { return productNumber; }
    public void setProductNumber(String productNumber) { this.productNumber = productNumber; }

    public String getProductGroupName() { return productGroupName; }
    public void setProductGroupName(String productGroupName) { this.productGroupName = productGroupName; }

    public String getTagNumber() { return tagNumber; }
    public void setTagNumber(String tagNumber) { this.tagNumber = tagNumber; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public Integer getProductItemStatus() { return productItemStatus; }
    public void setProductItemStatus(Integer productItemStatus) { this.productItemStatus = productItemStatus; }

    public boolean isScanned() { return isScanned; }
    public void setScanned(boolean scanned) { isScanned = scanned; }

    public String getLastUpdateDate() { return lastUpdateDate; }
    public void setLastUpdateDate(String lastUpdateDate) { this.lastUpdateDate = lastUpdateDate; }

    public boolean isNotDeleted() { return isNotDeleted; }
    public void setNotDeleted(boolean notDeleted) { isNotDeleted = notDeleted; }
}
