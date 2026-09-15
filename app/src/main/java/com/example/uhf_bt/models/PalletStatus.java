package com.example.uhf_bt.models;

public enum PalletStatus {
    CANCELLED(-1, "Cancelled"),
    REGISTER(0, "New"),
    PICK(1, "Pick"),
    PACK(2, "Pack"),
    RECEIVE_SHIPPING(3, "รับเข้าจัดส่ง"),
    RFID_LOADING(4, "RFIDLoading"),
    RFID_LOADING_SUCCESS(5, "RFID SUCCESS"),
    LOADING(6, "โหลดขึ้นรถ"),
    SHIPPED(7, "Shipped");

    private final int value;
    private final String description;

    PalletStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static String getDescription(int value) {
        for (PalletStatus status : values()) {
            if (status.value == value) {
                return status.description;
            }
        }
        return "Unknown";
    }

    public static PalletStatus fromValue(int value) {
        for (PalletStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }
}
