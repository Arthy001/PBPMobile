package com.example.uhf_bt.models;

public class PickingOrder {
    private int id;
    private String number;
    private String orderDate;
    private int orderStatus;
    private String description;
    private String warehouseSource;
    private String warehouseDestination;
    private String customer;
    private String tax;
    private String rowGuid;
    private String createdAtUtc;
    private double beforeTaxAmount;
    private double taxAmount;
    private double afterTaxAmount;

    // Getters
    public int getId() { return id; }
    public String getNumber() { return number; }
    public String getOrderDate() { return orderDate; }
    public int getOrderStatus() { return orderStatus; }
    public String getDescription() { return description; }
    public String getWarehouseSource() { return warehouseSource; }
    public String getWarehouseDestination() { return warehouseDestination; }
    public String getCustomer() { return customer; }
    public String getTax() { return tax; }
    public String getRowGuid() { return rowGuid; }
    public String getCreatedAtUtc() { return createdAtUtc; }
    public double getBeforeTaxAmount() { return beforeTaxAmount; }
    public double getTaxAmount() { return taxAmount; }
    public double getAfterTaxAmount() { return afterTaxAmount; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNumber(String number) { this.number = number; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setOrderStatus(int orderStatus) { this.orderStatus = orderStatus; }
    public void setDescription(String description) { this.description = description; }
    public void setWarehouseSource(String warehouseSource) { this.warehouseSource = warehouseSource; }
    public void setWarehouseDestination(String warehouseDestination) { this.warehouseDestination = warehouseDestination; }
    public void setCustomer(String customer) { this.customer = customer; }
    public void setTax(String tax) { this.tax = tax; }
    public void setRowGuid(String rowGuid) { this.rowGuid = rowGuid; }
    public void setCreatedAtUtc(String createdAtUtc) { this.createdAtUtc = createdAtUtc; }
    public void setBeforeTaxAmount(double beforeTaxAmount) { this.beforeTaxAmount = beforeTaxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }
    public void setAfterTaxAmount(double afterTaxAmount) { this.afterTaxAmount = afterTaxAmount; }
}
