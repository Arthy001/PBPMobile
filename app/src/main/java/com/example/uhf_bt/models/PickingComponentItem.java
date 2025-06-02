package com.example.uhf_bt.models;

import java.util.ArrayList;
import java.util.List;

public class PickingComponentItem {
    private String componentCode;
    private int quantityRequired;
    private int quantityPicked;
    private List<String> tagIds;

    public PickingComponentItem(String componentCode, int quantityRequired) {
        this.componentCode = componentCode;
        this.quantityRequired = quantityRequired;
        this.quantityPicked = 0;
        this.tagIds = new ArrayList<>();
    }

    public String getComponentCode() {
        return componentCode;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public int getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(int quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public int getQuantityPicked() {
        return quantityPicked;
    }

    public void setQuantityPicked(int quantityPicked) {
        this.quantityPicked = quantityPicked;
    }

    public List<String> getTagIds() {
        return tagIds;
    }

    public void addTagId(String tagId) {
        if (!tagIds.contains(tagId)) {
            tagIds.add(tagId);
            quantityPicked++;
        }
        else
        {
            quantityPicked++;
        }
    }

    public boolean isComplete() {
        return quantityPicked >= quantityRequired;
    }
}