
package com.example.uhf_bt.models;

import java.util.ArrayList;
import java.util.List;

public class PickingComponentItem {
    private String componentCode;
    private double quantityRequired;
    private double quantityPicked; // ใช้ค่าจาก allocatedQty ของ API โดยตรง
    private List<String> tagIds;
    private String barcode;

    public PickingComponentItem(String componentCode, int quantityRequired) {
        this.componentCode = componentCode;
        this.quantityRequired = quantityRequired;
        this.quantityPicked = 0;
        this.tagIds = new ArrayList<>();
        this.barcode = "";
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getComponentCode() {
        return componentCode;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public double getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(int quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public double getQuantityPicked() {
        return quantityPicked; // return ค่าโดยตรง ไม่ใช้ tagIds.size() แล้ว
    }

    public void setQuantityPicked(double quantityPicked) {
        this.quantityPicked = quantityPicked;
    }

    public List<String> getTagIds() {
        return tagIds;
    }

    public void addTagId(String tagId) {
        if (!tagIds.contains(tagId)) {
            tagIds.add(tagId);
        } else {
            tagIds.add(tagId); // อนุญาตให้ add tag เดียวกันได้หลายครั้ง
        }
        // ไม่ต้อง quantityPicked++ แล้ว เพราะใช้ค่าจาก API
    }

    public void clearTagIds() {
        if (tagIds != null) {
            tagIds.clear();
        } else {
            tagIds = new ArrayList<>();
        }
        // ไม่ต้อง reset quantityPicked = 0 แล้ว เพราะใช้ค่าจาก API
    }

    public boolean isComplete() {
        return quantityPicked >= quantityRequired;
    }
}
//package com.example.uhf_bt.models;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class PickingComponentItem {
//    private String componentCode;
//    private int quantityRequired;
//    private int quantityPicked;
//    private List<String> tagIds;
//
//    public PickingComponentItem(String componentCode, int quantityRequired) {
//        this.componentCode = componentCode;
//        this.quantityRequired = quantityRequired;
//        this.quantityPicked = 0;
//        this.tagIds = new ArrayList<>();
//    }
//
//    public String getComponentCode() {
//        return componentCode;
//    }
//
//    public void setComponentCode(String componentCode) {
//        this.componentCode = componentCode;
//    }
//
//    public int getQuantityRequired() {
//        return quantityRequired;
//    }
//
//    public void setQuantityRequired(int quantityRequired) {
//        this.quantityRequired = quantityRequired;
//    }
//
//    public int getQuantityPicked() {
//        return quantityPicked;
//    }
//
//    public void setQuantityPicked(int quantityPicked) {
//        this.quantityPicked = quantityPicked;
//    }
//
//    public List<String> getTagIds() {
//        return tagIds;
//    }
//
//    public void addTagId(String tagId) {
//        if (!tagIds.contains(tagId)) {
//            tagIds.add(tagId);
//            quantityPicked++;
//        }
//        else
//        {
//            quantityPicked++;
//        }
//    }
//
//    public boolean isComplete() {
//        return quantityPicked >= quantityRequired;
//    }
//}
