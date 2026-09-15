package com.example.uhf_bt.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(tableName = "product_items", indices = {@Index("tagNumber")})
public class ProductItemEntity {
    @PrimaryKey
    @NonNull
    private String tagNumber;
    
    private String productName;
    private String productNumber;
    private String productGroupName;
    private String lotNumber;
    private String warehouseName;
    private Integer productItemStatus;
    private boolean isScanned = false; // For ProductItemFragment (Search/Global)
    private boolean isScannedStockCount = false; // For StockCountFragment (Local Count)
    private String lastUpdateDate;
    private boolean isNotDeleted = true;

    public ProductItemEntity() {}

    // Getters and Setters
    @NonNull
    public String getTagNumber() { return tagNumber; }
    public void setTagNumber(@NonNull String tagNumber) { this.tagNumber = tagNumber; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductNumber() { return productNumber; }
    public void setProductNumber(String productNumber) { this.productNumber = productNumber; }

    public String getProductGroupName() { return productGroupName; }
    public void setProductGroupName(String productGroupName) { this.productGroupName = productGroupName; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public Integer getProductItemStatus() { return productItemStatus; }
    public void setProductItemStatus(Integer productItemStatus) { this.productItemStatus = productItemStatus; }

    public boolean isScanned() { return isScanned; }
    public void setScanned(boolean scanned) { isScanned = scanned; }

    public boolean isScannedStockCount() { return isScannedStockCount; }
    public void setScannedStockCount(boolean scanned) { isScannedStockCount = scanned; }

    public String getLastUpdateDate() { return lastUpdateDate; }
    public void setLastUpdateDate(String lastUpdateDate) { this.lastUpdateDate = lastUpdateDate; }

    public boolean isNotDeleted() { return isNotDeleted; }
    public void setNotDeleted(boolean notDeleted) { isNotDeleted = notDeleted; }
    
    // Helper to convert from DTO
    public static ProductItemEntity fromLite(ProductItemLite dto) {
        ProductItemEntity entity = new ProductItemEntity();
        entity.setTagNumber(dto.getTagNumber());
        entity.setProductName(dto.getProductName());
        entity.setProductNumber(dto.getProductNumber());
        entity.setProductGroupName(dto.getProductGroupName());
        entity.setLotNumber(dto.getLotNumber());
        entity.setWarehouseName(dto.getWarehouseName());
        entity.setProductItemStatus(dto.getProductItemStatus());
        entity.setScanned(dto.isScanned());
        entity.setLastUpdateDate(dto.getLastUpdateDate());
        entity.setNotDeleted(dto.isNotDeleted());
        return entity;
    }
}
