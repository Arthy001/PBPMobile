package com.example.uhf_bt.models;

import com.google.gson.annotations.SerializedName;

public class PickAndPackTagDto {
    @SerializedName("pickingOrderId")
    private Integer pickingOrderId;

    @SerializedName("pickingOrderItemId")
    private Integer pickingOrderItemId;

    @SerializedName("tagId")
    private Integer tagId;

    @SerializedName("tagNumber")
    private String tagNumber;

    @SerializedName("productItemId")
    private Integer productItemId;

    @SerializedName("palletTagNumber")
    private String palletTagNumber;

    @SerializedName("allocatedQty")
    private Double allocatedQty = 0.0;

    @SerializedName("remark")
    private String remark;

    public PickAndPackTagDto() {
    }

    public PickAndPackTagDto(Integer pickingOrderId, Integer pickingOrderItemId, Integer tagId,
                             String tagNumber, Integer productItemId, String palletTagNumber,
                             Double allocatedQty, String remark) {
        this.pickingOrderId = pickingOrderId;
        this.pickingOrderItemId = pickingOrderItemId;
        this.tagId = tagId;
        this.tagNumber = tagNumber;
        this.productItemId = productItemId;
        this.palletTagNumber = palletTagNumber;
        this.allocatedQty = allocatedQty;
        this.remark = remark;
    }

    public Integer getPickingOrderId() {
        return pickingOrderId;
    }

    public void setPickingOrderId(Integer pickingOrderId) {
        this.pickingOrderId = pickingOrderId;
    }

    public Integer getPickingOrderItemId() {
        return pickingOrderItemId;
    }

    public void setPickingOrderItemId(Integer pickingOrderItemId) {
        this.pickingOrderItemId = pickingOrderItemId;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public String getTagNumber() {
        return tagNumber;
    }

    public void setTagNumber(String tagNumber) {
        this.tagNumber = tagNumber;
    }

    public Integer getProductItemId() {
        return productItemId;
    }

    public void setProductItemId(Integer productItemId) {
        this.productItemId = productItemId;
    }

    public String getPalletTagNumber() {
        return palletTagNumber;
    }

    public void setPalletTagNumber(String palletTagNumber) {
        this.palletTagNumber = palletTagNumber;
    }

    public Double getAllocatedQty() {
        return allocatedQty;
    }

    public void setAllocatedQty(Double allocatedQty) {
        this.allocatedQty = allocatedQty;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
