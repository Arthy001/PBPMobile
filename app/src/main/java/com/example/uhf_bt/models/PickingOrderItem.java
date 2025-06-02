package com.example.uhf_bt.models;

public class PickingOrderItem {
    private int id;

    private int pickingOrderItemId;
    private String rowGuid;
    private String createdAtUtc;
    private String pickingOrder;
    private String warehouseSourceName;
    private String warehouseDestinationName;
    private String customer;
    private String orderDate;
    private int productId;
    private String product;
    private String remark;
    private String productNumber;
    private String summary;
    private double unitPrice;
    private double quantity;
    private double quantitySuccess ;
    private double quantityScan;
    private double total;
    private int quantityItem;
    private String tagId;
    private String barcode;
    private String lotNumber;
    private String lot;
    private int pickingTransStatus;
    private boolean isScanned;

    private String pallet;
    private String palletNo;
    private String palletTagId;

    private int palletStatus;

    private String boxType;
    private String sequenceNumber;
    private String lotSuggestion;
    private String locator;
    private double allocatedQty;
    private String qtyBox;
    private String itemType;
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPickingOrderItemId() { return pickingOrderItemId; }
    public void setPickingOrderItemId(int id) { this.pickingOrderItemId = pickingOrderItemId; }

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

    public String getProductNumber() { return productNumber; }
    public void setProductNumber(String productNumber) { this.productNumber = productNumber; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getQuantitySuccess() { return quantitySuccess; }
    public void setQuantitySuccess(double quantitySuccess) { this.quantitySuccess = quantitySuccess; }

    public double getQuantityScan() { return quantityScan; }
    public void setQuantityScan(double quantityScan) { this.quantityScan = quantityScan; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public int getQuantityItem() { return quantityItem; }
    public void setQuantityItem(int quantityItem) { this.quantityItem = quantityItem; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }

    public String getPallet() { return pallet; }
    public void setPallet(String pallet) { this.pallet = pallet; }

    public String getPalletNo() { return palletNo; }
    public void setPalletNo(String palletNo) { this.palletNo = palletNo; }
    public String getPalletTagId() { return palletTagId; }
    public void setPalletTagId(String palletTagId) { this.palletTagId = palletTagId; }


    public int getPalletStatus() { return palletStatus; }
    public void setPalletStatus(int palletStatus) { this.palletStatus = palletStatus; }

    public int getPickingTransStatus() { return pickingTransStatus; }
    public void setPickingTransStatus(int pickingTransStatus) { this.pickingTransStatus = pickingTransStatus; }

    public boolean isScanned() { return isScanned; }
    public void setScanned(boolean scanned) { isScanned = scanned; }

    public String getBoxType() {
        return boxType;
    }

    public void setBoxType(String boxType) {
        this.boxType = boxType;
    }
    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getLotSuggestion() {
        return lotSuggestion;
    }

    public void setLotSuggestion(String lotSuggestion) {
        this.lotSuggestion = lotSuggestion;
    }

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }

    public double getAllocatedQty() { return allocatedQty; }
    public void setAllocatedQty(double allocatedQty) { this.allocatedQty = allocatedQty; }
    public String getQtyBox() {
        return qtyBox;
    }
    public void setQtyBox(String qtyBox) {
        this.qtyBox = qtyBox;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
}
