package com.example.uhf_bt.models;

public class CheckStock {
    private int id;
    private String checkStockNumber;
    private String checkStockDate;
    private Integer warehouseId;
    private String warehouseName;
    private String status;
    private String createdAtUtc;
    private String updatedAtUtc;
    private String description;
    private String rowGuid;

    // Getters
    public int getId() { return id; }
    public String getCheckStockNumber() { return checkStockNumber; }
    public String getCheckStockDate() { return checkStockDate; }
    public Integer getWarehouseId() { return warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public String getStatus() { return status; }
    public String getCreatedAtUtc() { return createdAtUtc; }
    public String getUpdatedAtUtc() { return updatedAtUtc; }
    public String getDescription() { return description; }
    public String getRowGuid() { return rowGuid; }
}
