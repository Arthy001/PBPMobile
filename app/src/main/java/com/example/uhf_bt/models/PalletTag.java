package com.example.uhf_bt.models;


import com.google.gson.annotations.SerializedName;

public class PalletTag {
    @SerializedName("id")
    private long id;

    @SerializedName("rowGuid")
    private String rowGuid;

    @SerializedName("createdAtUtc")
    private String createdAtUtc;

    @SerializedName("pickingNumber")
    private String pickingNumber;

    @SerializedName("orderTagId")
    private String orderTagId;

    @SerializedName("warehouseSourceName")
    private String warehouseSourceName;

    @SerializedName("warehouseDestinationName")
    private String warehouseDestinationName;

    @SerializedName("customer")
    private String customer;

    @SerializedName("orderDate")
    private String orderDate;

    @SerializedName("productId")
    private long productId;

    @SerializedName("productNumber")
    private String productNumber;

    @SerializedName("product")
    private String product;

    @SerializedName("lotNumber")
    private String lotNumber;

    @SerializedName("summary")
    private String summary;

    @SerializedName("unitPrice")
    private double unitPrice;

    @SerializedName("quantity")
    private double quantity;

    @SerializedName("total")
    private double total;

    @SerializedName("palletName")
    private String palletName;

    @SerializedName("palletNumber")
    private String palletNumber;

    @SerializedName("palletTagNumber")
    private String palletTagNumber;

    @SerializedName("boxType")
    private String boxType;

    @SerializedName("sequenceNumber")
    private String sequenceNumber;

    @SerializedName("pickingOrderId")
    private int pickingOrderId; // เพิ่ม property สำหรับ pickingOrderId

    // เพิ่ม getter และ setter สำหรับ pickingOrderId
    public int getPickingOrderId() {
        return pickingOrderId;
    }

    @SerializedName("palletStatus")
    private int palletStatus;

    public void setPalletStatus(int palletStatus) { this.palletStatus = palletStatus; }
    public  int getPalletStatus() { return palletStatus; }

    @SerializedName("pickingType")
    private String pickingType;

    public String getPickingType() {
        return pickingType;
    }

    public void setPickingType(String pickingType) {
        this.pickingType = pickingType;
    }

    // Getters
    public long getId() { return id; }
    public String getRowGuid() { return rowGuid; }
    public String getCreatedAtUtc() { return createdAtUtc; }
    public String getPickingNumber() { return pickingNumber; }
    public String getOrderTagId() { return orderTagId; }
    public String getWarehouseSourceName() { return warehouseSourceName; }
    public String getWarehouseDestinationName() { return warehouseDestinationName; }
    public String getCustomer() { return customer; }
    public String getOrderDate() { return orderDate; }
    public long getProductId() { return productId; }
    public String getProductNumber() { return productNumber; }
    public String getProduct() { return product; }
    public String getLotNumber() { return lotNumber; }
    public String getSummary() { return summary; }
    public double getUnitPrice() { return unitPrice; }
    public double getQuantity() { return quantity; }
    public double getTotal() { return total; }
    public String getPalletName() { return palletName; }
    public String getPalletNumber() { return palletNumber; }
    public String getPalletTagNumber() { return palletTagNumber; }
    public String getBoxType() { return boxType; }
    public String getSequenceNumber() { return sequenceNumber; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setRowGuid(String rowGuid) { this.rowGuid = rowGuid; }
    public void setCreatedAtUtc(String createdAtUtc) { this.createdAtUtc = createdAtUtc; }
    public void setPickingNumber(String pickingNumber) { this.pickingNumber = pickingNumber; }
    public void setOrderTagId(String orderTagId) { this.orderTagId = orderTagId; }
    public void setWarehouseSourceName(String warehouseSourceName) { this.warehouseSourceName = warehouseSourceName; }
    public void setWarehouseDestinationName(String warehouseDestinationName) { this.warehouseDestinationName = warehouseDestinationName; }
    public void setCustomer(String customer) { this.customer = customer; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setProductId(long productId) { this.productId = productId; }
    public void setProductNumber(String productNumber) { this.productNumber = productNumber; }
    public void setProduct(String product) { this.product = product; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setTotal(double total) { this.total = total; }
    public void setPalletName(String palletName) { this.palletName = palletName; }
    public void setPalletNumber(String palletNumber) { this.palletNumber = palletNumber; }
    public void setPalletTagNumber(String palletTagNumber) { this.palletTagNumber = palletTagNumber; }
    public void setBoxType(String boxType) { this.boxType = boxType; }
    public void setSequenceNumber(String sequenceNumber) { this.sequenceNumber = sequenceNumber; }
}
