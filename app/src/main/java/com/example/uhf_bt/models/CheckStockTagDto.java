package com.example.uhf_bt.models;

import com.google.gson.annotations.SerializedName;

public class CheckStockTagDto {

    @SerializedName("CheckStockItemId")
    public int CheckStockItemId;

    @SerializedName("TagNumber")
    public String tagNumber;

    @SerializedName("ItemTypeCheck")
    public String itemTypeCheck;

    @SerializedName("QuantityInBoxCheck")
    public double quantityInBoxCheck;

    public int getCheckStockItemId() { return CheckStockItemId; }
    public void setCheckStockItemId(int CheckStockItemId) { this.CheckStockItemId = CheckStockItemId; }

     public String getTagNumber() { return tagNumber; }
     public void setTagNumber(String tagNumber) { this.tagNumber = tagNumber; }

     public String getItemTypeCheck() { return itemTypeCheck; }
     public void setItemTypeCheck(String itemTypeCheck) { this.itemTypeCheck = itemTypeCheck; }

     public double getQuantityInBoxCheck() { return quantityInBoxCheck; }
     public void setQuantityInBoxCheck(double quantityInBoxCheck) { this.quantityInBoxCheck = quantityInBoxCheck; }
}
