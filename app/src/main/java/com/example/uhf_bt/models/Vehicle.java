package com.example.uhf_bt.models;

import com.google.gson.annotations.SerializedName;

public class Vehicle {

    @SerializedName("id")
    private long id;

    @SerializedName("rowGuid")
    private String rowGuid;

    @SerializedName("licensePlate")
    private String licensePlate;

    @SerializedName("vehicleType")
    private int vehicleType;

    @SerializedName("vehicleBrand")
    private String vehicleBrand;

    @SerializedName("vehicleGroup")
    private int vehicleGroup;

    @SerializedName("vehicleStatus")
    private int vehicleStatus;

    @SerializedName("driver")
    private String driver;

    // Getters
    public long getId() { return id; }
    public String getRowGuid() { return rowGuid; }
    public String getLicensePlate() { return licensePlate; }
    public int getVehicleType() { return vehicleType; }
    public String getVehicleBrand() { return vehicleBrand; }
    public int getVehicleGroup() { return vehicleGroup; }
    public int getVehicleStatus() { return vehicleStatus; }
    public String getDriver() { return driver; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setRowGuid(String rowGuid) { this.rowGuid = rowGuid; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public void setVehicleType(int vehicleType) { this.vehicleType = vehicleType; }
    public void setVehicleBrand(String vehicleBrand) { this.vehicleBrand = vehicleBrand; }
    public void setVehicleGroup(int vehicleGroup) { this.vehicleGroup = vehicleGroup; }
    public void setVehicleStatus(int vehicleStatus) { this.vehicleStatus = vehicleStatus; }
    public void setDriver(String driver) { this.driver = driver; }
}
