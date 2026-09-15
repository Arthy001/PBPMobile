package com.example.uhf_bt.models;

public class TagWithStatus {
    private MoveOrderItem moveOrderItem;
    private String status; // "SUCCESS" หรือ "SCANNING"

    public TagWithStatus(MoveOrderItem moveOrderItem, String status) {
        this.moveOrderItem = moveOrderItem;
        this.status = status;
    }

    // Getters
    public MoveOrderItem getMoveOrderItem() {
        return moveOrderItem;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setMoveOrderItem(MoveOrderItem moveOrderItem) {
        this.moveOrderItem = moveOrderItem;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Convenience methods to access MoveOrderItem properties
    public String getTagNumber() {
        return moveOrderItem != null ? moveOrderItem.getTagNumber() : null;
    }

    public String getLot() {
        return moveOrderItem != null ? moveOrderItem.getLot() : null;
    }

    public String getItemType() {
        return moveOrderItem != null ? moveOrderItem.getItemType() : null;
    }

    public int getProductId() {
        return moveOrderItem != null ? moveOrderItem.getProductId() : 0;
    }

    public double getAllocatedQty() {
        return moveOrderItem != null ? moveOrderItem.getAllocatedQty() : 0.0;
    }
}
