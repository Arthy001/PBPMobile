package com.example.uhf_bt.models;

public class UserProfile {
    private String id;
    private String fullName;
    private String jobTitle;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String avatar;
    private String userType;
    private boolean isDefaultAdmin;
    private boolean isOnline;
    private String selectedCompany;
    private String createdAtUtc;

    // Getters
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUserType() { return userType; }
    public boolean isDefaultAdmin() { return isDefaultAdmin; }
    public String getSelectedCompany() { return selectedCompany; }
}
