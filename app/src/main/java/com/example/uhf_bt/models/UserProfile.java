package com.example.uhf_bt.models;

import java.util.List;

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
    private List<String> permissions;

    // Getters
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUserType() { return userType; }
    public boolean isDefaultAdmin() { return isDefaultAdmin; }
    public String getSelectedCompany() { return selectedCompany; }
    public List<String> getPermissions() { return permissions; }
    
    // Setters
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
