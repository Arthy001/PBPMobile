package com.example.uhf_bt.models;

public class MoveOrderItem {
    private int id;
    private String rowGuid;
    private String createdAtUtc;
    private String moveOrder;
    private String warehouseSourceName;
    private String warehouseDestinationName;
    private String orderDate;
    private int productId;
    private String product;
    private String barcode;
    private String productNumber;
    private String summary;
    private double unitPrice;
    private double quantity;
    private double quantitySuccess ;
    private double quantityScan;
    private double total;
    private int quantityItem;
    private String tagId;
    private String lotNumber;
    private String lot;
    private int moveTransStatus;
    private boolean isScanned;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRowGuid() { return rowGuid; }
    public void setRowGuid(String rowGuid) { this.rowGuid = rowGuid; }

    public String getCreatedAtUtc() { return createdAtUtc; }
    public void setCreatedAtUtc(String createdAtUtc) { this.createdAtUtc = createdAtUtc; }

    public String getMoveOrder() { return moveOrder; }
    public void setMoveOrder(String moveOrder) { this.moveOrder = moveOrder; }

    public String getWarehouseSourceName() { return warehouseSourceName; }
    public void setWarehouseSourceName(String warehouseSourceName) {
        this.warehouseSourceName = warehouseSourceName;
    }

    public String getWarehouseDestinationName() { return warehouseDestinationName; }
    public void setWarehouseDestinationName(String warehouseDestinationName) {
        this.warehouseDestinationName = warehouseDestinationName;
    }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getProductNumber() { return productNumber; }
    public void setProductNumber(String productNumber) { this.productNumber = productNumber; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
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

    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }
    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }

    public int getMoveTransStatus() { return moveTransStatus; }
    public void setMoveTransStatus(int moveTransStatus) { this.moveTransStatus = moveTransStatus; }

    public boolean isScanned() { return isScanned; }
    public void setScanned(boolean scanned) { isScanned = scanned; }
}