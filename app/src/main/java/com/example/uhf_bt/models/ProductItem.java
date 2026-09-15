package com.example.uhf_bt.models;

public class ProductItem {
    private Long id;
    private String rowGuid;
    private String createdAtUtc;
    private Integer moduleId;
    private String moduleName;
    private String moduleCode;
    private String moduleNumber;
    private String number;
    private Integer productItemId;
    private Integer productId;
    private String productName;
    private String productNumber;
    private String productGroupName;
    private String unitMeasure;
    private Integer stock;
    private String tagNumber;
    private String lotNumber;
    private String lot;
    private Integer warehouseId;
    private String warehouseName;
    private Integer productItemStatus;
    private String createDate;
    private String lastUpdateDate;
    private Double unitPrice;
    private Boolean physical;
    private String gS1;
    private String gtiN13;
    private String barcode;
    private double quantityPerUOM;
    private String itemType;
    private double  quantityInBox;

    private boolean isScanned = false;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRowGuid() { return rowGuid; }
    public void setRowGuid(String rowGuid) { this.rowGuid = rowGuid; }

    public String getCreatedAtUtc() { return createdAtUtc; }
    public void setCreatedAtUtc(String createdAtUtc) { this.createdAtUtc = createdAtUtc; }

    public Integer getModuleId() { return moduleId; }
    public void setModuleId(Integer moduleId) { this.moduleId = moduleId; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }

    public String getModuleNumber() { return moduleNumber; }
    public void setModuleNumber(String moduleNumber) { this.moduleNumber = moduleNumber; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public Integer getProductItemId() { return productItemId; }
    public void setProductItemId(Integer productItemId) { this.productItemId = productItemId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductNumber() { return productNumber; }
    public void setProductNumber(String productNumber) { this.productNumber = productNumber; }

    public String getProductGroupName() { return productGroupName; }
    public void setProductGroupName(String productGroupName) { this.productGroupName = productGroupName; }

    public String getUnitMeasure() { return unitMeasure; }
    public void setUnitMeasure(String unitMeasure) { this.unitMeasure = unitMeasure; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getTagNumber() { return tagNumber; }
    public void setTagNumber(String tagNumber) { this.tagNumber = tagNumber; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }

    public Integer getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public Integer getProductItemStatus() { return productItemStatus; }
    public void setProductItemStatus(Integer productItemStatus) { this.productItemStatus = productItemStatus; }

    public String getCreateDate() { return createDate; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }

    public String getLastUpdateDate() { return lastUpdateDate; }
    public void setLastUpdateDate(String lastUpdateDate) { this.lastUpdateDate = lastUpdateDate; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Boolean getPhysical() { return physical; }
    public void setPhysical(Boolean physical) { this.physical = physical; }

    public String getGS1() { return gS1; }
    public void setGS1(String gS1) { this.gS1 = gS1; }

    public String getGTIN13() { return gtiN13; }
    public void setGTIN13(String gtiN13) { this.gtiN13 = gtiN13; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public double getQuantityPerUOM() { return quantityPerUOM; }
    public void setQuantityPerUOM(double   quantityPerUOM) { this.quantityPerUOM = quantityPerUOM; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public double getQuantityInBox() { return quantityInBox; }
    public void setQuantityInBox(double   quantityInBox) { this.quantityInBox = quantityInBox; }
    public boolean isScanned() {
        return isScanned;
    }

    public void setScanned(boolean scanned) {
        isScanned = scanned;
    }
}
