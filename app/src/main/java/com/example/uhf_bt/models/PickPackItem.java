package com.example.uhf_bt.models;

import com.google.gson.annotations.SerializedName;

public class PickPackItem {
    @SerializedName("id")
    private Integer id;

    @SerializedName("pickingOrderItemId")
    private Integer pickingOrderItemId;

    @SerializedName("pickingOrderId")
    private Integer pickingOrderId;

    @SerializedName("pickingOrderNumber")
    private String pickingOrderNumber;

    @SerializedName("orderDate")
    private String orderDate;

    @SerializedName("productId")
    private Integer productId;

    @SerializedName("barcode")
    private String barcode;

    @SerializedName("itemNumber")
    private String itemNumber;

    @SerializedName("productName")
    private String productName;

    @SerializedName("quantity")
    private Double quantity = 0.0;

    @SerializedName("qtyBox")
    private Integer qtyBox = 0;

    @SerializedName("boxType")
    private String boxType;

    @SerializedName("lotNumber")
    private String lotNumber;

    @SerializedName("lot")
    private String lot;

    @SerializedName("palletTagNumber")
    private String palletTagNumber;

    @SerializedName("palletName")
    private String palletName;

    @SerializedName("palletNumber")
    private String palletNumber;

    @SerializedName("pickingOrderStatus")
    private Integer pickingOrderStatus;

    @SerializedName("pickingType")
    private String pickingType;

    @SerializedName("setNumber")
    private Integer setNumber = 0;

    @SerializedName("customer")
    private String customer;

    @SerializedName("remark")
    private String remark;

    @SerializedName("genRfidPartial")
    private String genRfidPartial;

    // UI state fields (Local only)
    private int scannedCount = 0;
    private boolean isHighlighted = false;
    private boolean isEvenRow = false;

    public PickPackItem() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPickingOrderItemId() {
        return pickingOrderItemId != null ? pickingOrderItemId : id;
    }

    public void setPickingOrderItemId(Integer pickingOrderItemId) {
        this.pickingOrderItemId = pickingOrderItemId;
    }

    public Integer getPickingOrderId() {
        return pickingOrderId;
    }

    public void setPickingOrderId(Integer pickingOrderId) {
        this.pickingOrderId = pickingOrderId;
    }

    public String getPickingOrderNumber() {
        return pickingOrderNumber;
    }

    public void setPickingOrderNumber(String pickingOrderNumber) {
        this.pickingOrderNumber = pickingOrderNumber;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getQuantity() {
        return quantity != null ? quantity : 0.0;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Integer getQtyBox() {
        return qtyBox != null ? qtyBox : 0;
    }

    public void setQtyBox(Integer qtyBox) {
        this.qtyBox = qtyBox;
    }

    public String getBoxType() {
        return boxType;
    }

    public void setBoxType(String boxType) {
        this.boxType = boxType;
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

    public String getPalletTagNumber() {
        return palletTagNumber;
    }

    public void setPalletTagNumber(String palletTagNumber) {
        this.palletTagNumber = palletTagNumber;
    }

    public String getPalletName() {
        return palletName;
    }

    public void setPalletName(String palletName) {
        this.palletName = palletName;
    }

    public String getPalletNumber() {
        return palletNumber;
    }

    public void setPalletNumber(String palletNumber) {
        this.palletNumber = palletNumber;
    }

    public Integer getPickingOrderStatus() {
        return pickingOrderStatus;
    }

    public void setPickingOrderStatus(Integer pickingOrderStatus) {
        this.pickingOrderStatus = pickingOrderStatus;
    }

    public String getPickingType() {
        return pickingType;
    }

    public void setPickingType(String pickingType) {
        this.pickingType = pickingType;
    }

    public Integer getSetNumber() {
        return setNumber != null ? setNumber : 0;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getGenRfidPartial() {
        return genRfidPartial;
    }

    public void setGenRfidPartial(String genRfidPartial) {
        this.genRfidPartial = genRfidPartial;
    }

    public int getScannedCount() {
        return scannedCount;
    }

    public void setScannedCount(int scannedCount) {
        this.scannedCount = scannedCount;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public boolean isEvenRow() {
        return isEvenRow;
    }

    public void setEvenRow(boolean evenRow) {
        isEvenRow = evenRow;
    }

    // Helper: Total expected tags to scan (default to quantity or qtyBox)
    public double getTotalTags() {
        if (qtyBox != null && qtyBox > 0) {
            return qtyBox;
        }
        return quantity != null ? quantity : 0.0;
    }
}
