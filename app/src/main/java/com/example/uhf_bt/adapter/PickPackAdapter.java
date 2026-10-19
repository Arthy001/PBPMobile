package com.example.uhf_bt.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.uhf_bt.R;
import com.example.uhf_bt.models.PickPackItem;

import java.util.List;

public class PickPackAdapter extends BaseAdapter {
    private final Context context;
    private final List<PickPackItem> items;
    private final LayoutInflater inflater;

    public PickPackAdapter(Context context, List<PickPackItem> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        LinearLayout rowLayout;
        TextView tvProductName;
        TextView tvItemAndBarcode;
        TextView tvOrderNumber;
        TextView tvCustomer;
        TextView tvTargetQty;
        TextView tvScannedCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_pick_and_pack, parent, false);
            holder = new ViewHolder();
            holder.rowLayout = convertView.findViewById(R.id.rowLayout);
            holder.tvProductName = convertView.findViewById(R.id.tvProductName);
            holder.tvItemAndBarcode = convertView.findViewById(R.id.tvItemAndBarcode);
            holder.tvOrderNumber = convertView.findViewById(R.id.tvOrderNumber);
            holder.tvCustomer = convertView.findViewById(R.id.tvCustomer);
            holder.tvTargetQty = convertView.findViewById(R.id.tvTargetQty);
            holder.tvScannedCount = convertView.findViewById(R.id.tvScannedCount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PickPackItem item = items.get(position);

        // Product Name
        holder.tvProductName.setText(item.getProductName() != null ? item.getProductName() : "-");

        // Item number and barcode
        String itemNum = item.getItemNumber() != null ? item.getItemNumber() : "-";
        String barcode = item.getBarcode() != null ? item.getBarcode() : "-";
        holder.tvItemAndBarcode.setText("Item: " + itemNum + " | Barcode: " + barcode);

        // Order Number & Customer
        holder.tvOrderNumber.setText(item.getPickingOrderNumber() != null ? item.getPickingOrderNumber() : "-");
        holder.tvCustomer.setText(item.getCustomer() != null ? item.getCustomer() : "-");

        // Target Qty
        double target = item.getTotalTags();
        holder.tvTargetQty.setText(String.valueOf((int) target));

        // Scanned Count
        int scanned = item.getScannedCount();
        holder.tvScannedCount.setText(String.valueOf(scanned));

        // Highlighting logic
        if (target > 0 && scanned >= target) {
            // Completed! Green highlight
            holder.rowLayout.setBackgroundColor(Color.parseColor("#E8F5E9")); // light green
            holder.tvScannedCount.setTextColor(Color.parseColor("#2E7D32")); // dark green
        } else if (scanned > 0) {
            // Partially scanned! Light amber highlight
            holder.rowLayout.setBackgroundColor(Color.parseColor("#FFFDE7")); // light yellow
            holder.tvScannedCount.setTextColor(Color.parseColor("#E65100")); // dark orange
        } else {
            // Unscanned: alternating rows
            if (position % 2 == 0) {
                holder.rowLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            } else {
                holder.rowLayout.setBackgroundColor(Color.parseColor("#F9F9F9"));
            }
            holder.tvScannedCount.setTextColor(Color.parseColor("#9E9E9E")); // gray
        }

        return convertView;
    }
}
