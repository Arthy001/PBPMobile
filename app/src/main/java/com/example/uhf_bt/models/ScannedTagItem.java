package com.example.uhf_bt.models;

public class ScannedTagItem {
    private int productId;
    private String tagId;
    private String lot;
    private String lotNumber;
    private double pickQty;
    private String barcode;

    public ScannedTagItem(int productId, String tagId, String lot, String lotNumber, double pickQty, String barcode ) {
        this.productId = productId;
        this.tagId = tagId;
        this.lot = lot;
        this.lotNumber = lotNumber;
        this.pickQty = pickQty;
        this.barcode = barcode;
    }

    // Getters and setters
    public int getProductId() { return productId; }
    public void setProductId(int  productId) { this.productId = productId; }
    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }
    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }
    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
    public double getPickQty() { return pickQty; }
    public void setPickQty(int pickQty) { this.pickQty = pickQty; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
}
