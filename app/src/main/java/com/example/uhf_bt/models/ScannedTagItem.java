package com.example.uhf_bt.models;

public class ScannedTagItem {
    private int productId;
    private String tagNumber;
    private String lot;
    private String lotNumber;
    private double reqQty;
    private double pickedQty;
    private double scanQty;
    private String barcode;
    private boolean isHistory;

    public ScannedTagItem(int productId, String tagNumber, String lot, String lotNumber, double reqQty, double pickedQty, double scanQty, String barcode ) {
        this.productId = productId;
        this.tagNumber = tagNumber;
        this.lot = lot;
        this.lotNumber = lotNumber;
        this.reqQty = reqQty;
        this.pickedQty = pickedQty;
        this.scanQty = scanQty;
        this.barcode = barcode;
        this.isHistory = false;
    }

    // Getters and setters
    public int getProductId() { return productId; }
    public void setProductId(int  productId) { this.productId = productId; }
    public String getTagNumber() { return tagNumber; }
    public void setTagNumber(String tagNumber) { this.tagNumber = tagNumber; }
    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }
    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
    
    public double getReqQty() { return reqQty; }
    public void setReqQty(double reqQty) { this.reqQty = reqQty; }
    
    public double getPickedQty() { return pickedQty; }
    public void setPickedQty(double pickedQty) { this.pickedQty = pickedQty; }
    
    public double getScanQty() { return scanQty; }
    public void setScanQty(double scanQty) { this.scanQty = scanQty; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    
    public boolean isHistory() { return isHistory; }
    public void setHistory(boolean history) { isHistory = history; }
}
