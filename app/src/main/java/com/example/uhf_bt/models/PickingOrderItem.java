package com.example.uhf_bt.models;

public class PickingOrderItem {
    private int id;
    private String rowGuid;
    private String createdAtUtc;
    private String pickingOrder;
    private String warehouseSourceName;
    private String warehouseDestinationName;
    private String customer;
    private String orderDate;
    private String product;
    private String summary;
    private double unitPrice;
    private int quantity;
    private double total;
    private int quantityItem;
    private String tagId;
    private String lotNumber;
    private String lot;
    private int pickingTransStatus;
    private boolean isScanned;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRowGuid() { return rowGuid; }
    public void setRowGuid(String rowGuid) { this.rowGuid = rowGuid; }

    public String getCreatedAtUtc() { return createdAtUtc; }
    public void setCreatedAtUtc(String createdAtUtc) { this.createdAtUtc = createdAtUtc; }

    public String getPickingOrder() { return pickingOrder; }
    public void setPickingOrder(String pickingOrder) { this.pickingOrder = pickingOrder; }

    public String getWarehouseSourceName() { return warehouseSourceName; }
    public void setWarehouseSourceName(String warehouseSourceName) {
        this.warehouseSourceName = warehouseSourceName;
    }

    public String getWarehouseDestinationName() { return warehouseDestinationName; }
    public void setWarehouseDestinationName(String warehouseDestinationName) {
        this.warehouseDestinationName = warehouseDestinationName;
    }

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public int getQuantityItem() { return quantityItem; }
    public void setQuantityItem(int quantityItem) { this.quantityItem = quantityItem; }

    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }

    public int getPickingTransStatus() { return pickingTransStatus; }
    public void setPickingTransStatus(int pickingTransStatus) { this.pickingTransStatus = pickingTransStatus; }

    public boolean isScanned() { return isScanned; }
    public void setScanned(boolean scanned) { isScanned = scanned; }
}