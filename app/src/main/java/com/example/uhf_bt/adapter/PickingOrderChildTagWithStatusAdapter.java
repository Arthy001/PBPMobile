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
import com.example.uhf_bt.models.TagPickWithStatus;
import com.example.uhf_bt.models.TagWithStatus;

import java.util.ArrayList;
import java.util.List;

public class PickingOrderChildTagWithStatusAdapter  extends BaseAdapter {
    private static final String TAG = "PickingOrderTagStatusAdapter";

    private Context context;
    private List<TagPickWithStatus> tagsWithStatus;
    private LayoutInflater inflater;

    @SuppressLint("LongLogTag")
    public PickingOrderChildTagWithStatusAdapter(Context context, List<TagPickWithStatus> tagsWithStatus) {
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
    public TagPickWithStatus getItem(int position) {
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
        PickingOrderChildTagWithStatusAdapter.ViewHolder holder;

        try {
            if (convertView == null) {
                // ใช้ layout เดิม หรือสร้างใหม่ถ้าต้องการ
                convertView = inflater.inflate(R.layout.item_move_order_tag, parent, false);
                Log.d(TAG, "Inflated new view for position: " + position);

                holder = new PickingOrderChildTagWithStatusAdapter.ViewHolder();
                holder.tvTagNumber = convertView.findViewById(R.id.tvTagNumber);
                //holder.tvLot = convertView.findViewById(R.id.tvLot);
                //holder.tvItemType = convertView.findViewById(R.id.tvItemType);
                holder.tvStatus = convertView.findViewById(R.id.tvStatus); // เพิ่มใน layout ถ้าต้องการ
                //holder.tvAllocatedQty = convertView.findViewById(R.id.tvAllocatedQty);

                // Debug: ตรวจสอบ TextView ว่า bind ได้หรือไม่
                Log.d(TAG, "ViewHolder binding results:");
                Log.d(TAG, "  tvTagNumber: " + (holder.tvTagNumber != null ? "Found" : "NULL"));
                //Log.d(TAG, "  tvLot: " + (holder.tvLot != null ? "Found" : "NULL"));
                //Log.d(TAG, "  tvItemType: " + (holder.tvItemType != null ? "Found" : "NULL"));
                Log.d(TAG, "  tvStatus: " + (holder.tvStatus != null ? "Found" : "NULL"));
                //Log.d(TAG, "  tvAllocatedQty: " + (holder.tvAllocatedQty != null ? "Found" : "NULL"));

                convertView.setTag(holder);
            } else {
                holder = (PickingOrderChildTagWithStatusAdapter.ViewHolder) convertView.getTag();
                Log.d(TAG, "Reusing view for position: " + position);
            }

            TagPickWithStatus tagWithStatus = getItem(position);

            if (tagWithStatus != null) {
                // แสดง TagNumber พร้อม debug
                String tagNumber = tagWithStatus.getTagNumber();
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
                String lot = tagWithStatus.getLot() != null ? tagWithStatus.getLot() : "-";
//                if (holder.tvLot != null) {
//                    holder.tvLot.setText("Lot: " + lot);
//                }

                String itemType = tagWithStatus.getItemType() != null ? tagWithStatus.getItemType() : "-";
//                if (holder.tvItemType != null) {
//                    holder.tvItemType.setText("Type: " + itemType);
//                }

                // แสดง AllocatedQty (เอา "Qty:" ออก)
//                if (holder.tvAllocatedQty != null) {
//                    holder.tvAllocatedQty.setText(String.format("%.1f", tagWithStatus.getAllocatedQty()));
//                }

                // แสดงสถานะด้วยสีเขียวและส้ม
                String status = tagWithStatus.getStatus();
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
                    // if (holder.tvItemType != null) holder.tvItemType.setTextColor(successGreen);
                    //if (holder.tvAllocatedQty != null) holder.tvAllocatedQty.setTextColor(successGreen);

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
                    //if (holder.tvItemType != null) holder.tvItemType.setTextColor(orangeColor);
                    // if (holder.tvAllocatedQty != null) holder.tvAllocatedQty.setTextColor(orangeColor);

                } else {
                    // กรณีอื่นๆ - สีดำปกติ
                    if (holder.tvStatus != null) {
                        holder.tvStatus.setText("");
                        holder.tvStatus.setVisibility(View.GONE);
                    }

                    int normalColor = Color.parseColor("#333333");
                    if (holder.tvTagNumber != null) holder.tvTagNumber.setTextColor(normalColor);
                    //if (holder.tvLot != null) holder.tvLot.setTextColor(normalColor);
                    //if (holder.tvItemType != null) holder.tvItemType.setTextColor(normalColor);
                    // if (holder.tvAllocatedQty != null) holder.tvAllocatedQty.setTextColor(normalColor);
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
        //TextView tvItemType;
        TextView tvStatus;
        //TextView tvAllocatedQty;
    }

    // Method เพิ่มเติม
    public void updateData(List<TagPickWithStatus> newTagsWithStatus) {
        this.tagsWithStatus.clear();
        if (newTagsWithStatus != null) {
            this.tagsWithStatus.addAll(newTagsWithStatus);
        }
        notifyDataSetChanged();
    }

    public List<TagPickWithStatus> getItems() {
        return tagsWithStatus;
    }

    public void addItem(TagPickWithStatus tagWithStatus) {
        if (tagWithStatus != null) {
            this.tagsWithStatus.add(tagWithStatus);
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

