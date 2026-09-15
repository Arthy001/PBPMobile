package com.example.uhf_bt.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PalletGroupSummary {
    private String pickingNumber;
    private int pickingOrderId;
    private String customer;
    private String orderDate;
    private String pickingType;
    private int totalPallets;
    private double totalQuantity;
    private List<PalletTag> palletTags;
    private Map<Integer, Integer> statusCountMap;
    private Set<String> productKeys;

    public PalletGroupSummary() {
        this.palletTags = new ArrayList<>();
        this.statusCountMap = new LinkedHashMap<>();
        this.productKeys = new HashSet<>();
    }

    public PalletGroupSummary(PalletTag initialTag) {
        this();
        if (initialTag != null) {
            this.pickingNumber = initialTag.getPickingNumber();
            this.pickingOrderId = initialTag.getPickingOrderId();
            this.customer = initialTag.getCustomer();
            this.orderDate = initialTag.getOrderDate();
            this.pickingType = initialTag.getPickingType();
            addPalletTag(initialTag);
        }
    }

    public void addPalletTag(PalletTag tag) {
        if (tag == null) return;
        this.palletTags.add(tag);
        this.totalQuantity += tag.getQuantity();
        this.totalPallets = this.palletTags.size();

        String pKey = tag.getProductNumber() != null && !tag.getProductNumber().isEmpty()
                ? tag.getProductNumber()
                : (tag.getProduct() != null && !tag.getProduct().isEmpty() ? tag.getProduct() : String.valueOf(tag.getProductId()));
        if (pKey != null && !pKey.isEmpty() && !pKey.equals("0")) {
            this.productKeys.add(pKey);
        }

        int status = tag.getPalletStatus();
        int currentCount = this.statusCountMap.containsKey(status) ? this.statusCountMap.get(status) : 0;
        this.statusCountMap.put(status, currentCount + 1);
    }

    public int getProductCount() {
        return !productKeys.isEmpty() ? productKeys.size() : (palletTags != null ? palletTags.size() : 0);
    }

    public boolean hasStatus(int status) {
        return this.statusCountMap.containsKey(status) && this.statusCountMap.get(status) > 0;
    }

    public String getFirstPalletTagNumber() {
        if (palletTags != null && !palletTags.isEmpty()) {
            return palletTags.get(0).getPalletTagNumber();
        }
        return "";
    }

    // Getters and Setters
    public String getPickingNumber() {
        return pickingNumber != null ? pickingNumber : "";
    }

    public void setPickingNumber(String pickingNumber) {
        this.pickingNumber = pickingNumber;
    }

    public int getPickingOrderId() {
        return pickingOrderId;
    }

    public void setPickingOrderId(int pickingOrderId) {
        this.pickingOrderId = pickingOrderId;
    }

    public String getCustomer() {
        return customer != null ? customer : "";
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getOrderDate() {
        return orderDate != null ? orderDate : "";
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getPickingType() {
        return pickingType != null ? pickingType : "";
    }

    public void setPickingType(String pickingType) {
        this.pickingType = pickingType;
    }

    public int getTotalPallets() {
        return totalPallets;
    }

    public void setTotalPallets(int totalPallets) {
        this.totalPallets = totalPallets;
    }

    public double getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public List<PalletTag> getPalletTags() {
        return palletTags;
    }

    public void setPalletTags(List<PalletTag> palletTags) {
        this.palletTags = palletTags;
    }

    public Map<Integer, Integer> getStatusCountMap() {
        return statusCountMap;
    }

    public void setStatusCountMap(Map<Integer, Integer> statusCountMap) {
        this.statusCountMap = statusCountMap;
    }
}
