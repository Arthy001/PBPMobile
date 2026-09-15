package com.example.uhf_bt.models;

public class CheckStockTagWithStatus {
    private CheckStockItem CheckStockItem;
    private String status; // "SUCCESS" หรือ "SCANNING"

    public CheckStockTagWithStatus(CheckStockItem CheckStockItem, String status) {
        this.CheckStockItem = CheckStockItem;
        this.status = status;
    }

    // Getters
    public CheckStockItem getCheckStockItem() {
        return CheckStockItem;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setCheckStockItem(CheckStockItem CheckStockItem) {
        this.CheckStockItem = CheckStockItem;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Convenience methods to access CheckStockItem properties
    public String getTagNumber() {
        return CheckStockItem != null ? CheckStockItem.getTagNumber() : null;
    }

    public String getLot() {
        return CheckStockItem != null ? CheckStockItem.getLot() : null;
    }

    public String getItemType() {
        return CheckStockItem != null ? CheckStockItem.getItemType() : null;
    }

    public int getProductId() {
        return CheckStockItem != null ? CheckStockItem.getProductId() : 0;
    }

    public double getAllocatedQty() {
        return CheckStockItem != null ? CheckStockItem.getAllocatedQty() : 0.0;
    }
}
