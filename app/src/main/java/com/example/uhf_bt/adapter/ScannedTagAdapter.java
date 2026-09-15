package com.example.uhf_bt.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.uhf_bt.R;
import com.example.uhf_bt.models.ScannedTagItem;

import java.util.ArrayList;
import java.util.List;

public class ScannedTagAdapter extends BaseAdapter {
    private static final String TAG = "ScannedTagAdapter";

    private Context context;
    private List<ScannedTagItem> tagItems;
    private LayoutInflater inflater;

    public ScannedTagAdapter(Context context, List<ScannedTagItem> tagItems) {
        this.context = context;

        // เพิ่ม null check และสร้าง new list ถ้า null
        if (tagItems != null) {
            this.tagItems = tagItems;
        } else {
            this.tagItems = new ArrayList<>();
            Log.w(TAG, "Received null tagItems, creating new ArrayList");
        }

        this.inflater = LayoutInflater.from(context);
        Log.d(TAG, "ScannedTagAdapter created with " + this.tagItems.size() + " items");
    }

    @Override
    public int getCount() {
        int count = tagItems != null ? tagItems.size() : 0;
        Log.d(TAG, "getCount() returning: " + count);
        return count;
    }

    @Override
    public ScannedTagItem getItem(int position) {
        if (tagItems == null || position < 0 || position >= tagItems.size()) {
            Log.w(TAG, "getItem() invalid position: " + position + ", size: " + (tagItems != null ? tagItems.size() : "null"));
            return null;
        }
        return tagItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            Log.d(TAG, "getView() position: " + position);

            ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_scanned_tag, parent, false);
                holder = new ViewHolder();
                holder.tvTagNumber = convertView.findViewById(R.id.tvTagNumber);
                holder.tvLot = convertView.findViewById(R.id.tvLot);
                holder.tvLotNumber = convertView.findViewById(R.id.tvLotNumber);
                holder.tvReqQty = convertView.findViewById(R.id.tvReqQty);
                holder.tvPickedQty = convertView.findViewById(R.id.tvPickedQty);
                holder.tvScanQty = convertView.findViewById(R.id.tvScanQty);
                holder.tvBarcode = convertView.findViewById(R.id.tvBarcode);
                convertView.setTag(holder);
                Log.d(TAG, "Created new ViewHolder for position: " + position);
            } else {
                holder = (ViewHolder) convertView.getTag();
                Log.d(TAG, "Reusing ViewHolder for position: " + position);
            }

            ScannedTagItem item = getItem(position);
            if (item != null) {
                // เพิ่ม null check สำหรับทุก field
                String barcode = item.getBarcode() != null ? item.getBarcode() : "-";
                String tagNumber = item.getTagNumber() != null ? item.getTagNumber() : "-";
                String lot = item.getLot() != null ? item.getLot() : "-";
                String lotNumber = item.getLotNumber() != null ? item.getLotNumber() : "-";

                holder.tvBarcode.setText("Barcode: " + barcode);
                holder.tvTagNumber.setText("Tag: " + tagNumber);
                holder.tvLot.setText("Lot: " + lot);
                holder.tvLotNumber.setText("Lot Number: " + lotNumber);
                holder.tvReqQty.setText(String.format("Req: %.0f", item.getReqQty()));
                holder.tvPickedQty.setText(String.format("Picked: %.0f", item.getPickedQty()));
                holder.tvScanQty.setText(String.format("Scan: %.0f", item.getScanQty()));

                if (item.isHistory()) {
                    convertView.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"));
                } else {
                    convertView.setBackgroundColor(android.graphics.Color.WHITE);
                }

                Log.d(TAG, "Successfully populated view for position: " + position);
            } else {
                Log.w(TAG, "Item is null at position: " + position);
                // ตั้งค่าเป็นค่าเริ่มต้นถ้า item เป็น null
                holder.tvBarcode.setText("Barcode: -");
                holder.tvTagNumber.setText("Tag: -");
                holder.tvLot.setText("Lot: -");
                holder.tvLotNumber.setText("Lot Number: -");
                holder.tvReqQty.setText("Req: 0");
                holder.tvPickedQty.setText("Picked: 0");
                holder.tvScanQty.setText("Scan: 0");
            }

            return convertView;

        } catch (Exception e) {
            Log.e(TAG, "Exception in getView at position " + position, e);
            e.printStackTrace();

            // Return empty view ในกรณี error
            View emptyView = new View(context);
            return emptyView;
        }
    }

    static class ViewHolder {
        TextView tvTagNumber;
        TextView tvLot;
        TextView tvLotNumber;
        TextView tvReqQty;
        TextView tvPickedQty;
        TextView tvScanQty;
        TextView tvBarcode;
    }

    public void addItem(ScannedTagItem item) {
        try {
            Log.d(TAG, "addItem() called");

            if (item == null) {
                Log.w(TAG, "Trying to add null item");
                return;
            }

            if (tagItems == null) {
                Log.w(TAG, "tagItems is null, creating new ArrayList");
                tagItems = new ArrayList<>();
            }

            Log.d(TAG, "Adding item to list, current size: " + tagItems.size());
            Log.d(TAG, "Item details - TagNumber: " + item.getTagNumber() +
                    ", Lot: " + item.getLot() +
                    ", ScanQty: " + item.getScanQty());

            tagItems.add(item);

            Log.d(TAG, "Item added successfully, new size: " + tagItems.size());
            Log.d(TAG, "Calling notifyDataSetChanged()");

            notifyDataSetChanged();

            Log.d(TAG, "notifyDataSetChanged() completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Exception in addItem()", e);
            e.printStackTrace();
            throw e; // re-throw เพื่อให้เห็น stack trace เต็ม
        }
    }

    public List<ScannedTagItem> getItems() {
        return tagItems != null ? tagItems : new ArrayList<>();
    }

    // เพิ่ม method สำหรับ clear items
    public void clearItems() {
        if (tagItems != null) {
            tagItems.clear();
            notifyDataSetChanged();
            Log.d(TAG, "Items cleared");
        }
    }

    // เพิ่ม method สำหรับ remove item
    public void removeItem(int position) {
        if (tagItems != null && position >= 0 && position < tagItems.size()) {
            tagItems.remove(position);
            notifyDataSetChanged();
            Log.d(TAG, "Item removed at position: " + position);
        }
    }
}

//package com.example.uhf_bt.adapter;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.TextView;
//
//import com.example.uhf_bt.R;
//import com.example.uhf_bt.models.ScannedTagItem;
//
//import org.w3c.dom.Text;
//
//import java.util.List;
//
//public class ScannedTagAdapter extends BaseAdapter {
//    private Context context;
//    private List<ScannedTagItem> tagItems;
//    private LayoutInflater inflater;
//
//    public ScannedTagAdapter(Context context, List<ScannedTagItem> tagItems) {
//        this.context = context;
//        this.tagItems = tagItems;
//        this.inflater = LayoutInflater.from(context);
//    }
//
//    @Override
//    public int getCount() {
//        return tagItems.size();
//    }
//
//    @Override
//    public ScannedTagItem getItem(int position) {
//        return tagItems.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder holder;
//
//        if (convertView == null) {
//            convertView = inflater.inflate(R.layout.item_scanned_tag, parent, false);
//            holder = new ViewHolder();
//            holder.tvTagNumber = convertView.findViewById(R.id.tvTagId);
//            holder.tvLot = convertView.findViewById(R.id.tvLot);
//            holder.tvLotNumber = convertView.findViewById(R.id.tvLotNumber);
//            holder.tvPickQty = convertView.findViewById(R.id.tvPickQty);
//            holder.tvBarcode = convertView.findViewById(R.id.tvBarcode);
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        ScannedTagItem item = getItem(position);
//        holder.tvBarcode.setText("Barcode: " + item.getBarcode());
//        holder.tvTagNumber.setText("Tag: " + item.getTagNumber());
//        holder.tvLot.setText("Lot: " +item.getLot());
//        holder.tvLotNumber.setText("Lot Number" + item.getLotNumber());
//        holder.tvPickQty.setText(String.format("Pick: %.0f" , item.getPickQty()) );
//
//        return convertView;
//    }
//
//    static class ViewHolder {
//        TextView tvTagNumber;
//        TextView tvLot;
//        TextView tvLotNumber;
//        TextView tvPickQty;
//        TextView tvBarcode;
//    }
//
//    public void addItem(ScannedTagItem item) {
//        tagItems.add(item);
//        notifyDataSetChanged();
//    }
//
//    public List<ScannedTagItem> getItems() {
//        return tagItems;
//    }
//}
