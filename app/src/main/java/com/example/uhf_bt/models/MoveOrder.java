package com.example.uhf_bt.models;

public class MoveOrder {
    private int id;
    private String number;
    private String orderDate;
    private int status;
    private String description;
    private String warehouseSource;
    private String warehouseDestination;
    private String tax;
    private String rowGuid;
    private String createdAtUtc;
    private double beforeTaxAmount;
    private double taxAmount;
    private double afterTaxAmount;

    // Getters
    public int getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public int getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getWarehouseSource() {
        return warehouseSource;
    }

    public String getWarehouseDestination() {
        return warehouseDestination;
    }

    public String getTax() {
        return tax;
    }

    public String getRowGuid() {
        return rowGuid;
    }

    public String getCreatedAtUtc() {
        return createdAtUtc;
    }

    public double getBeforeTaxAmount() {
        return beforeTaxAmount;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public double getAfterTaxAmount() {
        return afterTaxAmount;
    }
}