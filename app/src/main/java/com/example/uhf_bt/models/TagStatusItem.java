package com.example.uhf_bt.models;

public class TagStatusItem {
    private String tagNumber;
    private boolean isReceived;

    public TagStatusItem(String tagNumber, boolean isReceived) {
        this.tagNumber = tagNumber;
        this.isReceived = isReceived;
    }

    public String getTagNumber() {
        return tagNumber;
    }

    public void setTagNumber(String tagNumber) {
        this.tagNumber = tagNumber;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public void setReceived(boolean received) {
        isReceived = received;
    }
}
