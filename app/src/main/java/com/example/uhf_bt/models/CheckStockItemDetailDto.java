package com.example.uhf_bt.models;

import com.google.gson.annotations.SerializedName;

public class CheckStockItemDetailDto {
    @SerializedName("id")
    private int id;
    @SerializedName("checkStockId")
    private int checkStockId;
    @SerializedName("checkStock")
    private String checkStock;
    @SerializedName("productId")
    private int productId;
    @SerializedName("productNumber")
    private String productNumber;
    @SerializedName("product")
    private String product;
    @SerializedName("barcode")
    private String barcode;
    @SerializedName("tagNumber")
    private String tagNumber;
    @SerializedName("std")
    private double std;
    @SerializedName("quantityInBox")
    private double quantityInBox;
    @SerializedName("itemType")
    private String itemType;
    @SerializedName("isChecked")
    private boolean isChecked;
    @SerializedName("boxType")
    private String boxType;
    @SerializedName("warehouseName")
    private String warehouseName;
    @SerializedName("description")
    private String description;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCheckStockId() { return checkStockId; }
    public void setCheckStockId(int checkStockId) { this.checkStockId = checkStockId; }

    public String getCheckStock() { return checkStock; }
    public void setCheckStock(String checkStock) { this.checkStock = checkStock; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductNumber() { return productNumber; }
    public void setProductNumber(String productNumber) { this.productNumber = productNumber; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getTagNumber() { return tagNumber; }
    public void setTagNumber(String tagNumber) { this.tagNumber = tagNumber; }

    public double getStd() { return std; }
    public void setStd(double std) { this.std = std; }

    public double getQuantityInBox() { return quantityInBox; }
    public void setQuantityInBox(double quantityInBox) { this.quantityInBox = quantityInBox; }

    @SerializedName("quantityInBoxCheck")
    private double quantityInBoxCheck;

    public double getQuantityInBoxCheck() { return quantityInBoxCheck; }
    public void setQuantityInBoxCheck(double quantityInBoxCheck) { this.quantityInBoxCheck = quantityInBoxCheck; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }

    public String getBoxType() { return boxType; }
    public void setBoxType(String boxType) { this.boxType = boxType; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
