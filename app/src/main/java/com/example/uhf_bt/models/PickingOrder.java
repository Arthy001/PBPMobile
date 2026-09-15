package com.example.uhf_bt.models;

import java.util.Date;

public class PickingOrder {
    private int id;
    private String number;
    private Date orderDate;
    private Integer orderStatus; // -1 = Cancel, 0 = New, 1 = Packed, 2 = Picked, 3 = ReceiveShipping, 4 = Shipped
    private String description;
    private int customerId;
    private String customer;
    private String warehouseSource;
    private String warehouseDestination;
    private String pickingType;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getWarehouseSource() {
        return warehouseSource;
    }

    public void setWarehouseSource(String warehouseSource) {
        this.warehouseSource = warehouseSource;
    }

    public String getWarehouseDestination() {
        return warehouseDestination;
    }

    public void setWarehouseDestination(String warehouseDestination) {
        this.warehouseDestination = warehouseDestination;
    }
    public String getPickingType() {
        return pickingType;
    }

    public void setPickingType(String pickingType) {
        this.pickingType = pickingType;
    }
}


