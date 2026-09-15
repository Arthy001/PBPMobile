package com.example.uhf_bt.models;

public class PickingTagDto {
    public int pickingOrderItemId;
    public String tagNumber;
    public String palletTagNumber;
    public String remark;



    public double allocatedQty;
    public int getPickingOrderItemId() { return pickingOrderItemId; }
    public void setPickingOrderItemId(int pickingOrderItemId) { this.pickingOrderItemId = pickingOrderItemId; }
    public String getTagNumber() { return tagNumber; }
    public void setTagNumber(String tagNumber) { this.tagNumber = tagNumber; }
    public String getPalletTagNumber() { return palletTagNumber; }
    public void setPalletTagNumber(String palletTagNumber) { this.palletTagNumber = palletTagNumber; }

    public  String getRemark() {return remark;}
    public void setRemark(String remark) {this.remark = remark;};
    public  Double getAllocatedQty() {return allocatedQty;}
    public void setAllocatedQty(double allocatedQty) {this.allocatedQty = allocatedQty;};
}
