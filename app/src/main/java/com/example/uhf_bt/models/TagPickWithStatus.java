package com.example.uhf_bt.models;

public class TagPickWithStatus {

    private PickingOrderItem pickingOrderItem;
    private String status; // "SUCCESS" หรือ "SCANNING"

    public TagPickWithStatus(PickingOrderItem pickingOrderItem, String status) {
        this.pickingOrderItem = pickingOrderItem;
        this.status = status;
    }

    // Getters
    public PickingOrderItem getPickingOrderItem() {
        return pickingOrderItem;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setPickingOrderItem(PickingOrderItem pickingOrderItem) {
        this.pickingOrderItem = pickingOrderItem;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Convenience methods to access MoveOrderItem properties
    public String getTagNumber() {
        return pickingOrderItem != null ? pickingOrderItem.getTagNumber() : null;
    }

    public String getLot() {
        return pickingOrderItem != null ? pickingOrderItem.getLot() : null;
    }

    public String getItemType() {
        return pickingOrderItem != null ? pickingOrderItem.getItemType() : null;
    }

    public int getProductId() {
        return pickingOrderItem != null ? pickingOrderItem.getProductId() : 0;
    }

    public double getAllocatedQty() {
        return pickingOrderItem != null ? pickingOrderItem.getAllocatedQty() : 0.0;
    }
}
