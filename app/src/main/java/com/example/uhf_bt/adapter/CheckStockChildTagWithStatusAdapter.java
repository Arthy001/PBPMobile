package com.example.uhf_bt.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.uhf_bt.R;
import com.example.uhf_bt.models.CheckStockTagWithStatus;

import java.util.ArrayList;
import java.util.List;

public class CheckStockChildTagWithStatusAdapter extends BaseAdapter {
    private static final String TAG = "CheckStockTagStatusAdapter";

    private Context context;
    private List<CheckStockTagWithStatus> tagsWithStatus;
    private LayoutInflater inflater;

    @SuppressLint("LongLogTag")
    public CheckStockChildTagWithStatusAdapter(Context context, List<CheckStockTagWithStatus> tagsWithStatus) {
        this.context = context;
        this.tagsWithStatus = tagsWithStatus != null ? tagsWithStatus : new ArrayList<>();
        this.inflater = LayoutInflater.from(context);

        Log.d(TAG, "Constructor - items count: " + this.tagsWithStatus.size());
    }

    @Override
    public int getCount() {
        return tagsWithStatus.size();
    }

    @Override
    public CheckStockTagWithStatus getItem(int position) {
        if (position >= 0 && position < tagsWithStatus.size()) {
            return tagsWithStatus.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("LongLogTag")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        try {
            if (convertView == null) {
                // ใช้ layout เดิม หรือสร้างใหม่ถ้าต้องการ
                convertView = inflater.inflate(R.layout.item_move_order_tag, parent, false);
                Log.d(TAG, "Inflated new view for position: " + position);

                holder = new ViewHolder();
                holder.tvTagNumber = convertView.findViewById(R.id.tvTagNumber);
                //holder.tvLot = convertView.findViewById(R.id.tvLot);
                holder.tvItemType = convertView.findViewById(R.id.tvItemType);
                holder.tvQtyInBox = convertView.findViewById(R.id.tvQtyInBox); // เพิ่มตัวแปร
                holder.tvStatus = convertView.findViewById(R.id.tvStatus);
                holder.tvAllocatedQty = convertView.findViewById(R.id.tvAllocatedQty);

                // Debug: ตรวจสอบ TextView ว่า bind ได้หรือไม่
                Log.d(TAG, "ViewHolder binding results:");
                Log.d(TAG, "  tvTagNumber: " + (holder.tvTagNumber != null ? "Found" : "NULL"));
                //Log.d(TAG, "  tvLot: " + (holder.tvLot != null ? "Found" : "NULL"));
                //Log.d(TAG, "  tvItemType: " + (holder.tvItemType != null ? "Found" : "NULL"));
                Log.d(TAG, "  tvStatus: " + (holder.tvStatus != null ? "Found" : "NULL"));
                //Log.d(TAG, "  tvAllocatedQty: " + (holder.tvAllocatedQty != null ? "Found" : "NULL"));

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                Log.d(TAG, "Reusing view for position: " + position);
            }

            CheckStockTagWithStatus CheckStockTagWithStatus = getItem(position);

            if (CheckStockTagWithStatus != null) {
                // แสดง TagNumber พร้อม debug
                String tagNumber = CheckStockTagWithStatus.getTagNumber();
                Log.d(TAG, "Position " + position + " - Raw TagNumber: '" + tagNumber + "'");
                Log.d(TAG, "Position " + position + " - TagNumber is null: " + (tagNumber == null));
                Log.d(TAG, "Position " + position + " - TagNumber length: " + (tagNumber != null ? tagNumber.length() : "null"));

                String displayTagNumber = tagNumber != null ? tagNumber : "No Tag";
                Log.d(TAG, "Position " + position + " - Display TagNumber: '" + displayTagNumber + "'");

                if (holder.tvTagNumber != null) {
                    // ทดสอบด้วยข้อความง่ายๆ ก่อน
                    holder.tvTagNumber.setText( (position + 1)  + " : " + displayTagNumber);
                    Log.d(TAG, "Position " + position + " - TextView set to: 'Tag: " + displayTagNumber + "'");
                } else {
                    Log.e(TAG, "Position " + position + " - tvTagNumber is NULL!");
                }

                // แสดงข้อมูลอื่นๆ
                String lot = CheckStockTagWithStatus.getLot() != null ? CheckStockTagWithStatus.getLot() : "-";
//                if (holder.tvLot != null) {
//                    holder.tvLot.setText("Lot: " + lot);
//                }

                String itemType = CheckStockTagWithStatus.getCheckStockItem().getItemType() != null ? CheckStockTagWithStatus.getCheckStockItem().getItemType() : "-";
                if (holder.tvItemType != null) {
                    holder.tvItemType.setText("Type: " + itemType);
                }

                // แสดง Qty เดิม
                if (holder.tvQtyInBox != null) {
                    holder.tvQtyInBox.setText("Qty: " + String.format("%.0f", CheckStockTagWithStatus.getCheckStockItem().getQuantityPerBox()));
                }

                // แสดง Qty Check
                if (holder.tvAllocatedQty != null) {
                    holder.tvAllocatedQty.setText("Check/Scan: " + String.format("%.0f", CheckStockTagWithStatus.getCheckStockItem().getQuantityInBoxCheck()));
                }

                // แสดงสถานะด้วยสีเขียวและส้ม
                String status = CheckStockTagWithStatus.getStatus();
                if ("SUCCESS".equals(status)) {
                    // สำหรับ tag ที่ confirm แล้ว - ใช้สีเขียวเหมือน QtySuccess
                    if (holder.tvStatus != null) {
                        holder.tvStatus.setText(""); // เอา "CONFIRMED" ออก
                        holder.tvStatus.setVisibility(View.GONE); // ซ่อน status TextView
                    }

                    // ตัวหนังสือทั้งหมดเป็นสีเขียวเหมือน QtySuccess
                    int successGreen = Color.parseColor("#4CAF50"); // สีเขียวเหมือน QtySuccess
                    if (holder.tvTagNumber != null) holder.tvTagNumber.setTextColor(successGreen);
                   // if (holder.tvLot != null) holder.tvLot.setTextColor(successGreen);
                    if (holder.tvItemType != null) holder.tvItemType.setTextColor(successGreen);
                    if (holder.tvQtyInBox != null) holder.tvQtyInBox.setTextColor(successGreen);
                    if (holder.tvAllocatedQty != null) holder.tvAllocatedQty.setTextColor(successGreen);

                } else if ("SCANNING".equals(status)) {
                    // สำหรับ tag ที่กำลังสแกน (ยังไม่ confirm) - ใช้สีส้ม
                    if (holder.tvStatus != null) {
                        holder.tvStatus.setText(""); // เอา "PENDING" ออก
                        holder.tvStatus.setVisibility(View.GONE); // ซ่อน status TextView
                    }

                    // ตัวหนังสือทั้งหมดเป็นสีส้ม
                    int orangeColor = Color.parseColor("#FF8800");
                    if (holder.tvTagNumber != null) holder.tvTagNumber.setTextColor(orangeColor);
                    //if (holder.tvLot != null) holder.tvLot.setTextColor(orangeColor);
                    if (holder.tvItemType != null) holder.tvItemType.setTextColor(orangeColor);
                    if (holder.tvQtyInBox != null) holder.tvQtyInBox.setTextColor(orangeColor);
                    if (holder.tvAllocatedQty != null) holder.tvAllocatedQty.setTextColor(orangeColor);

                } else {
                    // กรณีอื่นๆ - สีดำปกติ
                    if (holder.tvStatus != null) {
                        holder.tvStatus.setText("");
                        holder.tvStatus.setVisibility(View.GONE);
                    }

                    int normalColor = Color.parseColor("#333333");
                    if (holder.tvTagNumber != null) holder.tvTagNumber.setTextColor(normalColor);
                    //if (holder.tvLot != null) holder.tvLot.setTextColor(normalColor);
                    if (holder.tvItemType != null) holder.tvItemType.setTextColor(normalColor);
                    if (holder.tvAllocatedQty != null) holder.tvAllocatedQty.setTextColor(normalColor);
                }

                // พื้นหลังขาวธรรมดา
                convertView.setBackgroundColor(Color.WHITE);

                Log.d(TAG, "Position " + position + " - TagNumber: " + tagNumber + ", Status: " + status);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in getView at position " + position, e);
            return new View(context);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView tvTagNumber;
        //TextView tvLot;
        TextView tvItemType;
        TextView tvQtyInBox;
        TextView tvStatus;
        TextView tvAllocatedQty;
    }

    // Method เพิ่มเติม
    public void updateData(List<CheckStockTagWithStatus> newTagsWithStatus) {
        this.tagsWithStatus.clear();
        if (newTagsWithStatus != null) {
            this.tagsWithStatus.addAll(newTagsWithStatus);
        }
        notifyDataSetChanged();
    }

    public List<CheckStockTagWithStatus> getItems() {
        return tagsWithStatus;
    }

    public void addItem(CheckStockTagWithStatus CheckStockTagWithStatus) {
        if (CheckStockTagWithStatus != null) {
            this.tagsWithStatus.add(CheckStockTagWithStatus);
            notifyDataSetChanged();
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < tagsWithStatus.size()) {
            this.tagsWithStatus.remove(position);
            notifyDataSetChanged();
        }
    }

    public void clearItems() {
        this.tagsWithStatus.clear();
        notifyDataSetChanged();
    }
}

//package com.example.uhf_bt.adapter;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.graphics.Color;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.TextView;
//
//import com.example.uhf_bt.R;
//import com.example.uhf_bt.models.CheckStockTagWithStatus;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CheckStockChildTagWithStatusAdapter extends BaseAdapter {
//    private static final String TAG = "CheckStockTagStatusAdapter";
//
//    private Context context;
//    private List<CheckStockTagWithStatus> tagsWithStatus;
//    private LayoutInflater inflater;
//
//    @SuppressLint("LongLogTag")
//    public CheckStockChildTagWithStatusAdapter(Context context, List<CheckStockTagWithStatus> tagsWithStatus) {
//        this.context = context;
//        this.tagsWithStatus = tagsWithStatus != null ? tagsWithStatus : new ArrayList<>();
//        this.inflater = LayoutInflater.from(context);
//
//        Log.d(TAG, "Constructor - items count: " + this.tagsWithStatus.size());
//    }
//
//    @Override
//    public int getCount() {
//        return tagsWithStatus.size();
//    }
//
//    @Override
//    public CheckStockTagWithStatus getItem(int position) {
//        if (position >= 0 && position < tagsWithStatus.size()) {
//            return tagsWithStatus.get(position);
//        }
//        return null;
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @SuppressLint("LongLogTag")
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder holder;
//
//        try {
//            if (convertView == null) {
//                // ใช้ layout เดิม หรือสร้างใหม่ถ้าต้องการ
//                convertView = inflater.inflate(R.layout.item_move_order_tag, parent, false);
//                Log.d(TAG, "Inflated new view for position: " + position);
//
//                holder = new ViewHolder();
//                holder.tvTagNumber = convertView.findViewById(R.id.tvTagNumber);
//                holder.tvLot = convertView.findViewById(R.id.tvLot);
//                holder.tvItemType = convertView.findViewById(R.id.tvItemType);
//                holder.tvStatus = convertView.findViewById(R.id.tvStatus); // เพิ่มใน layout ถ้าต้องการ
//                holder.tvAllocatedQty = convertView.findViewById(R.id.tvAllocatedQty);
//
//                // Debug: ตรวจสอบ TextView ว่า bind ได้หรือไม่
//                Log.d(TAG, "ViewHolder binding results:");
//                Log.d(TAG, "  tvTagNumber: " + (holder.tvTagNumber != null ? "Found" : "NULL"));
//                Log.d(TAG, "  tvLot: " + (holder.tvLot != null ? "Found" : "NULL"));
//                Log.d(TAG, "  tvItemType: " + (holder.tvItemType != null ? "Found" : "NULL"));
//                Log.d(TAG, "  tvStatus: " + (holder.tvStatus != null ? "Found" : "NULL"));
//                Log.d(TAG, "  tvAllocatedQty: " + (holder.tvAllocatedQty != null ? "Found" : "NULL"));
//
//                convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//                Log.d(TAG, "Reusing view for position: " + position);
//            }
//
//            CheckStockTagWithStatus CheckStockTagWithStatus = getItem(position);
//
//            if (CheckStockTagWithStatus != null) {
//                // แสดง TagNumber พร้อม debug
//                String tagNumber = CheckStockTagWithStatus.getTagNumber();
//                Log.d(TAG, "Position " + position + " - Raw TagNumber: '" + tagNumber + "'");
//                Log.d(TAG, "Position " + position + " - TagNumber is null: " + (tagNumber == null));
//                Log.d(TAG, "Position " + position + " - TagNumber length: " + (tagNumber != null ? tagNumber.length() : "null"));
//
//                String displayTagNumber = tagNumber != null ? tagNumber : "No Tag";
//                Log.d(TAG, "Position " + position + " - Display TagNumber: '" + displayTagNumber + "'");
//
//                if (holder.tvTagNumber != null) {
//                    // ทดสอบด้วยข้อความง่ายๆ ก่อน
//                    holder.tvTagNumber.setText("Tag: " + displayTagNumber);
//                    Log.d(TAG, "Position " + position + " - TextView set to: 'Tag: " + displayTagNumber + "'");
//                } else {
//                    Log.e(TAG, "Position " + position + " - tvTagNumber is NULL!");
//                }
//
//                // แสดงข้อมูลอื่นๆ
//                String lot = CheckStockTagWithStatus.getLot() != null ? CheckStockTagWithStatus.getLot() : "-";
//                if (holder.tvLot != null) {
//                    holder.tvLot.setText("Lot: " + lot);
//                }
//
//                String itemType = CheckStockTagWithStatus.getItemType() != null ? CheckStockTagWithStatus.getItemType() : "-";
//                if (holder.tvItemType != null) {
//                    holder.tvItemType.setText("Type: " + itemType);
//                }
//
//                // แสดง AllocatedQty
//                if (holder.tvAllocatedQty != null) {
//                    holder.tvAllocatedQty.setText("Qty: " + String.format("%.1f", CheckStockTagWithStatus.getAllocatedQty()));
//                }
//
//                // แสดงสถานะด้วยสีเขียวและส้ม
//                String status = CheckStockTagWithStatus.getStatus();
//                if ("SUCCESS".equals(status)) {
//                    // สำหรับ tag ที่ confirm แล้ว - ใช้สีเขียว
//                    if (holder.tvStatus != null) {
//                        holder.tvStatus.setText("CONFIRMED");
//                        holder.tvStatus.setTextColor(Color.GREEN);
//                    }
//
//                    // ตัวหนังสือทั้งหมดเป็นสีเขียว
//                    if (holder.tvTagNumber != null) holder.tvTagNumber.setTextColor(Color.GREEN);
//                    if (holder.tvLot != null) holder.tvLot.setTextColor(Color.GREEN);
//                    if (holder.tvItemType != null) holder.tvItemType.setTextColor(Color.GREEN);
//                    if (holder.tvAllocatedQty != null) holder.tvAllocatedQty.setTextColor(Color.GREEN);
//
//                } else if ("SCANNING".equals(status)) {
//                    // สำหรับ tag ที่กำลังสแกน (ยังไม่ confirm) - ใช้สีส้ม
//                    if (holder.tvStatus != null) {
//                        holder.tvStatus.setText("PENDING");
//                        holder.tvStatus.setTextColor(Color.parseColor("#FF8800")); // ส้ม
//                    }
//
//                    // ตัวหนังสือทั้งหมดเป็นสีส้ม
//                    int orangeColor = Color.parseColor("#FF8800");
//                    if (holder.tvTagNumber != null) holder.tvTagNumber.setTextColor(orangeColor);
//                    if (holder.tvLot != null) holder.tvLot.setTextColor(orangeColor);
//                    if (holder.tvItemType != null) holder.tvItemType.setTextColor(orangeColor);
//                    if (holder.tvAllocatedQty != null) holder.tvAllocatedQty.setTextColor(orangeColor);
//
//                } else {
//                    // กรณีอื่นๆ - สีดำปกติ
//                    if (holder.tvStatus != null) {
//                        holder.tvStatus.setText("");
//                    }
//
//                    int normalColor = Color.parseColor("#333333");
//                    if (holder.tvTagNumber != null) holder.tvTagNumber.setTextColor(normalColor);
//                    if (holder.tvLot != null) holder.tvLot.setTextColor(normalColor);
//                    if (holder.tvItemType != null) holder.tvItemType.setTextColor(normalColor);
//                    if (holder.tvAllocatedQty != null) holder.tvAllocatedQty.setTextColor(normalColor);
//                }
//
//                // พื้นหลังขาวธรรมดา
//                convertView.setBackgroundColor(Color.WHITE);
//
//                Log.d(TAG, "Position " + position + " - TagNumber: " + tagNumber + ", Status: " + status);
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "Error in getView at position " + position, e);
//            return new View(context);
//        }
//
//        return convertView;
//    }
//
//    static class ViewHolder {
//        TextView tvTagNumber;
//        TextView tvLot;
//        TextView tvItemType;
//        TextView tvStatus;
//        TextView tvAllocatedQty;
//    }
//
//    // Method เพิ่มเติม
//    public void updateData(List<CheckStockTagWithStatus> newTagsWithStatus) {
//        this.tagsWithStatus.clear();
//        if (newTagsWithStatus != null) {
//            this.tagsWithStatus.addAll(newTagsWithStatus);
//        }
//        notifyDataSetChanged();
//    }
//
//    public List<CheckStockTagWithStatus> getItems() {
//        return tagsWithStatus;
//    }
//
//    public void addItem(CheckStockTagWithStatus CheckStockTagWithStatus) {
//        if (CheckStockTagWithStatus != null) {
//            this.tagsWithStatus.add(CheckStockTagWithStatus);
//            notifyDataSetChanged();
//        }
//    }
//
//    public void removeItem(int position) {
//        if (position >= 0 && position < tagsWithStatus.size()) {
//            this.tagsWithStatus.remove(position);
//            notifyDataSetChanged();
//        }
//    }
//
//    public void clearItems() {
//        this.tagsWithStatus.clear();
//        notifyDataSetChanged();
//    }
//}
