package com.example.uhf_bt.models;

public class TagDto {

    public String tagNumber;
    public int tagStatus;
    public boolean isChecked;
    public int productId;
    public double quantityInBox;
    public double quantityInBoxCheck;
    public String itemType;

    public double getQuantityInBox() { return quantityInBox; }
    public void setQuantityInBox(double quantityInBox) { this.quantityInBox = quantityInBox; }

    public double getQuantityInBoxCheck() { return quantityInBoxCheck; }
    public void setQuantityInBoxCheck(double quantityInBoxCheck) { this.quantityInBoxCheck = quantityInBoxCheck; }

    public int getTagStatus() { return tagStatus; }
    public void setTagStatus(int tagStatus) { this.tagStatus = tagStatus; }

    public String getTagNumber() { return tagNumber; }
    public void setTagNumber(String tagNumber) { this.tagNumber = tagNumber; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
}
