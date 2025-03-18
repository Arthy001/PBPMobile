package com.example.uhf_bt.models;

import com.google.gson.annotations.SerializedName;

public class PalletTransaction {
    @SerializedName("id")
    private long id;

    @SerializedName("pickingOrderItemId")
    private long pickingOrderItemId;

    @SerializedName("rowGuid")
    private String rowGuid;

    @SerializedName("createdAtUtc")
    private String createdAtUtc;

    @SerializedName("pickingOrder")
    private String pickingOrder;

    @SerializedName("warehouseSourceName")
    private String warehouseSourceName;

    @SerializedName("warehouseDestinationName")
    private String warehouseDestinationName;

    @SerializedName("orderDate")
    private String orderDate;

    @SerializedName("itemNumber")
    private String itemNumber;

    @SerializedName("product")
    private String product;

    @SerializedName("pickingOrderId")
    private Long pickingOrderId;

    @SerializedName("productId")
    private long productId;

    @SerializedName("tagId")
    private String tagId;

    @SerializedName("summary")
    private String summary;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("lotNumber")
    private String lotNumber;

    @SerializedName("lot")
    private String lot;

    @SerializedName("pickingTransStatus")
    private int pickingTransStatus;

    // Getters
    public long getId() { return id; }
    public long getPickingOrderItemId() { return pickingOrderItemId; }
    public String getRowGuid() { return rowGuid; }
    public String getCreatedAtUtc() { return createdAtUtc; }
    public String getPickingOrder() { return pickingOrder; }
    public String getWarehouseSourceName() { return warehouseSourceName; }
    public String getWarehouseDestinationName() { return warehouseDestinationName; }
    public String getOrderDate() { return orderDate; }
    public String getItemNumber() { return itemNumber; }
    public String getProduct() { return product; }
    public Long getPickingOrderId() { return pickingOrderId; }
    public long getProductId() { return productId; }
    public String getTagId() { return tagId; }
    public String getSummary() { return summary; }
    public int getQuantity() { return quantity; }
    public String getLotNumber() { return lotNumber; }
    public String getLot() { return lot; }
    public int getPickingTransStatus() { return pickingTransStatus; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setPickingOrderItemId(long pickingOrderItemId) { this.pickingOrderItemId = pickingOrderItemId; }
    public void setRowGuid(String rowGuid) { this.rowGuid = rowGuid; }
    public void setCreatedAtUtc(String createdAtUtc) { this.createdAtUtc = createdAtUtc; }
    public void setPickingOrder(String pickingOrder) { this.pickingOrder = pickingOrder; }
    public void setWarehouseSourceName(String warehouseSourceName) { this.warehouseSourceName = warehouseSourceName; }
    public void setWarehouseDestinationName(String warehouseDestinationName) { this.warehouseDestinationName = warehouseDestinationName; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setItemNumber(String itemNumber) { this.itemNumber = itemNumber; }
    public void setProduct(String product) { this.product = product; }
    public void setPickingOrderId(Long pickingOrderId) { this.pickingOrderId = pickingOrderId; }
    public void setProductId(long productId) { this.productId = productId; }
    public void setTagId(String tagId) { this.tagId = tagId; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
    public void setLot(String lot) { this.lot = lot; }
    public void setPickingTransStatus(int pickingTransStatus) { this.pickingTransStatus = pickingTransStatus; }
}
