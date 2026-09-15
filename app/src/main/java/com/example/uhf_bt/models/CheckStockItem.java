package com.example.uhf_bt.models;

public class CheckStockItem {
    private int id;
    private String rowGuid;
    private String createdAtUtc;
    private String checkStock;
    //private int CheckStockItemId;
    private String warehouseSourceName;
    private String warehouseDestinationName;
    private String warehouseName;
    private String orderDate;
    private int productId;
    private String product;
    private String barcode;
    private String productNumber;
    private String summary;
    private double unitPrice;
    private double quantity;
    private double quantitySuccess;
    private double quantityScan;
    private double quantityRenant;
    private double quantityRenantSuccess;
    private double quantityRenantScan;
    private double quantityPerBox;
    private double allocatedQty;
    private double total;
    private int quantityItem;
    private int tagId;
    private String tagNumber;
    private String lotNumber;
    private String lot;
    private int moveTransStatus;
    private boolean isScanned;
    private String createdBy;
    private String boxType;
    private String department;
    private String itemType;
    private double qtyTagRfid;
    private String qtyBox;
    private String fullBox;
    private String renant;
    private String itemTypeCheck;
    private double quantityInBoxCheck;
    private double std;

    private double quantityPcs;
    private double quantitySuccessPcs;
    private double quantityScanPcs;
    private double quantityRenantPcs;
    private double quantityRenantSuccessPcs;
    private double quantityRenantScanPcs;

    // Existing getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRowGuid() { return rowGuid; }
    public void setRowGuid(String rowGuid) { this.rowGuid = rowGuid; }

    public String getCreatedAtUtc() { return createdAtUtc; }
    public void setCreatedAtUtc(String createdAtUtc) { this.createdAtUtc = createdAtUtc; }

    public String getCheckStock() { return checkStock; }
    public void setCheckStock(String checkStock) { this.checkStock = checkStock; }
    //public int getCheckStockItemId() { return CheckStockItemId; }
    //public void setCheckStockItemId(int CheckStock) { this.CheckStockItemId = CheckStockItemId; }

    public String getWarehouseSourceName() { return warehouseSourceName; }
    public void setWarehouseSourceName(String warehouseSourceName) {
        this.warehouseSourceName = warehouseSourceName;
    }

    public String getWarehouseDestinationName() { return warehouseDestinationName; }
    public void setWarehouseDestinationName(String warehouseDestinationName) {
        this.warehouseDestinationName = warehouseDestinationName;
    }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
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

    public int getTagId() { return tagId; }
    public void setTagId(int tagId) { this.tagId = tagId; }
    public String getTagNumber() { return tagNumber; }
    public void setTagNumber(String tagNumber) { this.tagNumber = tagNumber; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }

    public int getMoveTransStatus() { return moveTransStatus; }
    public void setMoveTransStatus(int moveTransStatus) { this.moveTransStatus = moveTransStatus; }

    public boolean isScanned() { return isScanned; }
    public void setScanned(boolean scanned) { isScanned = scanned; }

    // New getters and setters for additional fields from JSON
    public double getQuantityRenant() { return quantityRenant; }
    public void setQuantityRenant(double quantityRenant) { this.quantityRenant = quantityRenant; }

    public double getQuantityRenantSuccess() { return quantityRenantSuccess; }
    public void setQuantityRenantSuccess(double quantityRenantSuccess) { this.quantityRenantSuccess = quantityRenantSuccess; }

    public double getQuantityRenantScan() { return quantityRenantScan; }
    public void setQuantityRenantScan(double quantityRenantScan) { this.quantityRenantScan = quantityRenantScan; }

    public double getQuantityPerBox() { return quantityPerBox; }
    public void setQuantityPerBox(double quantityPerBox) { this.quantityPerBox = quantityPerBox; }

    public double getAllocatedQty() { return allocatedQty; }
    public void setAllocatedQty(double allocatedQty) { this.allocatedQty = allocatedQty; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getBoxType() { return boxType; }
    public void setBoxType(String boxType) { this.boxType = boxType; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public double getQtyTagRfid() { return qtyTagRfid; }
    public void setQtyTagRfid(double qtyTagRfid) { this.qtyTagRfid = qtyTagRfid; }

    public String getQtyBox() { return qtyBox; }
    public void setQtyBox(String qtyBox) { this.qtyBox = qtyBox; }

    public String getFullBox() { return fullBox; }
    public void setFullBox(String fullBox) { this.fullBox = fullBox; }

    public String getRenant() { return renant; }
    public void setRenant(String renant) { this.renant = renant; }

    public String getItemTypeCheck() { return itemTypeCheck; }
    public void setItemTypeCheck(String itemTypeCheck) { this.itemTypeCheck = itemTypeCheck; }

    public double getQuantityInBoxCheck() { return quantityInBoxCheck; }
    public void setQuantityInBoxCheck(double quantityInBoxCheck) { this.quantityInBoxCheck = quantityInBoxCheck; }

    public double getStd() { return std; }
    public void setStd(double std) { this.std = std; }

    public double getQuantityPcs() { return quantityPcs; }
    public void setQuantityPcs(double quantityPcs) { this.quantityPcs = quantityPcs; }
    public double getQuantitySuccessPcs() { return quantitySuccessPcs; }
    public void setQuantitySuccessPcs(double quantitySuccessPcs) { this.quantitySuccessPcs = quantitySuccessPcs; }
    public double getQuantityScanPcs() { return quantityScanPcs; }
    public void setQuantityScanPcs(double quantityScanPcs) { this.quantityScanPcs = quantityScanPcs; }
    public double getQuantityRenantPcs() { return quantityRenantPcs; }
    public void setQuantityRenantPcs(double quantityRenantPcs) { this.quantityRenantPcs = quantityRenantPcs; }
    public double getQuantityRenantSuccessPcs() { return quantityRenantSuccessPcs; }
    public void setQuantityRenantSuccessPcs(double quantityRenantSuccessPcs) { this.quantityRenantSuccessPcs = quantityRenantSuccessPcs; }
    public double getQuantityRenantScanPcs() { return quantityRenantScanPcs; }
    public void setQuantityRenantScanPcs(double quantityRenantScanPcs) { this.quantityRenantScanPcs = quantityRenantScanPcs; }
}

//package com.example.uhf_bt.models;
//
//public class CheckStockItem {
//    private int id;
//    private String rowGuid;
//    private String createdAtUtc;
//    private String CheckStock;
//    private String warehouseSourceName;
//    private String warehouseDestinationName;
//    private String orderDate;
//    private int productId;
//    private String product;
//    private String barcode;
//    private String productNumber;
//    private String summary;
//    private double unitPrice;
//    private double quantity;
//    private double quantitySuccess ;
//    private double quantityScan;
//    private double total;
//    private int quantityItem;
//    private String tagId;
//    private String lotNumber;
//    private String lot;
//    private int moveTransStatus;
//    private boolean isScanned;
//
//    // Getters and Setters
//    public int getId() { return id; }
//    public void setId(int id) { this.id = id; }
//
//    public String getRowGuid() { return rowGuid; }
//    public void setRowGuid(String rowGuid) { this.rowGuid = rowGuid; }
//
//    public String getCreatedAtUtc() { return createdAtUtc; }
//    public void setCreatedAtUtc(String createdAtUtc) { this.createdAtUtc = createdAtUtc; }
//
//    public String getCheckStock() { return CheckStock; }
//    public void setCheckStock(String CheckStock) { this.CheckStock = CheckStock; }
//
//    public String getWarehouseSourceName() { return warehouseSourceName; }
//    public void setWarehouseSourceName(String warehouseSourceName) {
//        this.warehouseSourceName = warehouseSourceName;
//    }
//
//    public String getWarehouseDestinationName() { return warehouseDestinationName; }
//    public void setWarehouseDestinationName(String warehouseDestinationName) {
//        this.warehouseDestinationName = warehouseDestinationName;
//    }
//
//    public String getOrderDate() { return orderDate; }
//    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
//
//    public String getProductNumber() { return productNumber; }
//    public void setProductNumber(String productNumber) { this.productNumber = productNumber; }
//
//    public String getProduct() { return product; }
//    public void setProduct(String product) { this.product = product; }
//
//    public int getProductId() { return productId; }
//    public void setProductId(int productId) { this.productId = productId; }
//    public String getBarcode() { return barcode; }
//    public void setBarcode(String barcode) { this.barcode = barcode; }
//    public String getSummary() { return summary; }
//    public void setSummary(String summary) { this.summary = summary; }
//
//    public double getUnitPrice() { return unitPrice; }
//    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
//
//    public double getQuantity() { return quantity; }
//    public void setQuantity(double quantity) { this.quantity = quantity; }
//
//    public double getQuantitySuccess() { return quantitySuccess; }
//    public void setQuantitySuccess(double quantitySuccess) { this.quantitySuccess = quantitySuccess; }
//
//    public double getQuantityScan() { return quantityScan; }
//    public void setQuantityScan(double quantityScan) { this.quantityScan = quantityScan; }
//    public double getTotal() { return total; }
//    public void setTotal(double total) { this.total = total; }
//
//    public int getQuantityItem() { return quantityItem; }
//    public void setQuantityItem(int quantityItem) { this.quantityItem = quantityItem; }
//
//    public String getTagId() { return tagId; }
//    public void setTagId(String tagId) { this.tagId = tagId; }
//    public String getLotNumber() { return lotNumber; }
//    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
//
//    public String getLot() { return lot; }
//    public void setLot(String lot) { this.lot = lot; }
//
//    public int getMoveTransStatus() { return moveTransStatus; }
//    public void setMoveTransStatus(int moveTransStatus) { this.moveTransStatus = moveTransStatus; }
//
//    public boolean isScanned() { return isScanned; }
//    public void setScanned(boolean scanned) { isScanned = scanned; }
//}
