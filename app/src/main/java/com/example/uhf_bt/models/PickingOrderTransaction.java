package com.example.uhf_bt.models;

public class PickingOrderTransaction {
    private int id;
    private String rowGuid;
    private String createdAtUtc;
    private String pickingOrder;
    private String warehouseSourceName;
    private String warehouseDestinationName;
    private String orderDate;
    private String product;
    private String pickingOrderId;
    private int pickingOrderItemId;
    private int productId;

    private String tagId;
    private String summary;
    private double quantity;
    private String lotNumber;
    private String lot;
    private int pickingTransStatus;

    private String tagRemark;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRowGuid() {
        return rowGuid;
    }

    public void setRowGuid(String rowGuid) {
        this.rowGuid = rowGuid;
    }

    public String getCreatedAtUtc() {
        return createdAtUtc;
    }

    public void setCreatedAtUtc(String createdAtUtc) {
        this.createdAtUtc = createdAtUtc;
    }

    public String getPickingOrder() {
        return pickingOrder;
    }

    public void setPickingOrder(String pickingOrder) {
        this.pickingOrder = pickingOrder;
    }

    public int getPickingOrderItemId() {
        return pickingOrderItemId;
    }

    public void setPickingOrderItemId(String PickingOrderItemId) {
        this.pickingOrderItemId = pickingOrderItemId;
    }

    public String getWarehouseSourceName() {
        return warehouseSourceName;
    }

    public void setWarehouseSourceName(String warehouseSourceName) {
        this.warehouseSourceName = warehouseSourceName;
    }

    public String getWarehouseDestinationName() {
        return warehouseDestinationName;
    }

    public void setWarehouseDestinationName(String warehouseDestinationName) {
        this.warehouseDestinationName = warehouseDestinationName;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getPickingOrderId() {
        return pickingOrderId;
    }

    public void setPickingOrderId(String pickingOrderId) {
        this.pickingOrderId = pickingOrderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public int getPickingTransStatus() {
        return pickingTransStatus;
    }

    public void setPickingTransStatus(int pickingTransStatus) {
        this.pickingTransStatus = pickingTransStatus;
    }

    public String getTagRemark() {
        return tagRemark;
    }

    public void setTagRemark(String tagRemark) {
        this.tagRemark = tagRemark;
    }
}
