package com.example.uhf_bt.adapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import com.example.uhf_bt.ColorConstants;
import com.example.uhf_bt.R;
import com.example.uhf_bt.models.MoveOrderItem;

import java.util.ArrayList;
import java.util.List;

public class MoveOrderChildAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<MoveOrderItem> originalItems;
    private List<MoveOrderItem> filteredItems;
    private LayoutInflater inflater;
    private ItemFilter itemFilter;

    public MoveOrderChildAdapter(Context context, List<MoveOrderItem> items) {
        this.context = context;
        this.originalItems = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>(items);
        this.inflater = LayoutInflater.from(context);
        getFilter();
    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Override
    public MoveOrderItem getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("LongLogTag")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_move_order_child, parent, false);
            holder = new ViewHolder();
            holder.tvItem = convertView.findViewById(R.id.tvItem);
            holder.tvProduct = convertView.findViewById(R.id.tvProduct);
            holder.tvLot = convertView.findViewById(R.id.tvLot);
            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
            holder.tvQuantitySuccess = convertView.findViewById(R.id.tvQuantitySuccess);
            holder.tvQuantityScan = convertView.findViewById(R.id.tvQuantityScan);
            holder.tvBarcode = convertView.findViewById(R.id.tvBarcode);
            holder.tvAllocatedQty = convertView.findViewById(R.id.tvAllocatedQty);
            holder.tvSTD = convertView.findViewById(R.id.tvSTD);
            holder.tvBoxType = convertView.findViewById(R.id.tvBoxType);
            holder.ivStatus = convertView.findViewById(R.id.ivStatus);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MoveOrderItem item = getItem(position);

        // เพิ่ม debug logging
        Log.d("MoveOrderChildTagAdapter", "Position: " + position +
                ", TagNumber: " + item.getTagNumber() +
                ", ProductId: " + item.getProductId());


        // ตรวจสอบว่า item.getTagNumber() ส่งค่าอะไร
        String tagNumber = item.getTagNumber();
        if (tagNumber == null || tagNumber.isEmpty()) {
            Log.w("MoveOrderChildTagAdapter", "TagNumber is null or empty at position: " + position);
            tagNumber = "No Tag Number";
        }

        holder.tvItem.setText("Item : " + item.getProductNumber());
        holder.tvProduct.setText(item.getProduct());
        holder.tvLot.setText("Lot: " + item.getLot());
        holder.tvBarcode.setText(item.getBarcode());

        // **แสดงสถานะปริมาณและเปลี่ยนสีตามสถานะ**
        double totalScanned = item.getQuantitySuccess() + item.getQuantityScan();
        double maxQty = item.getQuantity();
        double remaining = maxQty - totalScanned;

        // แสดงปริมาณพร้อมสถานะ
        if (remaining <= 0) {
            // เต็มแล้ว - สีเขียว
            holder.tvQuantity.setText(String.format("%.0f", maxQty));
            holder.tvQuantity.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            holder.tvQuantityScan.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            holder.tvQuantitySuccess.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else if (remaining <= maxQty * 0.2) {
            // เหลือน้อย - สีส้ม
            holder.tvQuantity.setText(String.format("%.0f (%.0f left)", maxQty, remaining));
            holder.tvQuantity.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
            holder.tvQuantityScan.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
            holder.tvQuantitySuccess.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
        } else {
            // ปกติ - สีดำ
            holder.tvQuantity.setText(String.format("%.0f", maxQty));
            holder.tvQuantity.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            holder.tvQuantityScan.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
            holder.tvQuantitySuccess.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        }

        // ตรวจสอบถ้าเกินปริมาณ
        if (totalScanned > maxQty) {
            holder.tvQuantity.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            holder.tvQuantityScan.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            holder.tvQuantity.setText(String.format("%.0f", maxQty));
        }

        holder.tvQuantityScan.setText(String.format("%.0f", item.getQuantityScan()));
        holder.tvQuantitySuccess.setText(String.format("%.0f", item.getQuantitySuccess()));
        holder.tvAllocatedQty.setText(String.format("%.0f", item.getAllocatedQty()));
        holder.tvSTD.setText(String.format("%.0f", item.getQuantityPerBox()));
        holder.tvBoxType.setText(item.getBoxType());

        // Display Renant icon if ItemType is "Renant"
        if (item.getItemType() != null && item.getItemType().equals("Renant")) {
            holder.ivStatus.setVisibility(View.VISIBLE);
            holder.ivStatus.setImageResource(R.drawable.ic_renant);
        } else {
            holder.ivStatus.setVisibility(View.GONE);
        }

        // **ปรับปรุงการใช้สีพื้นหลัง**
        int backgroundColor = ColorConstants.getMoveOrderItemBackground(
                item.getQuantity(),
                item.getQuantitySuccess(),
                item.getQuantityScan()
        );
        convertView.setBackgroundColor(backgroundColor);

        return convertView;
    }

    private static class ViewHolder {
        TextView tvItem;
        TextView tvProduct;
        TextView tvLot;
        TextView tvQuantity;
        TextView tvQuantitySuccess;
        TextView tvQuantityScan;
        TextView tvBarcode;
        TextView tvAllocatedQty;
        TextView tvSTD;
        TextView tvBoxType;
        ImageView ivStatus;
    }

    @Override
    public Filter getFilter() {
        if (itemFilter == null) {
            itemFilter = new ItemFilter();
        }
        return itemFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = new ArrayList<>(originalItems);
                results.count = originalItems.size();
            } else {
                List<MoveOrderItem> filteredList = new ArrayList<>();
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (MoveOrderItem item : originalItems) {
                    // Filter by product name, lot number, product number, or barcode
                    if (item.getProduct().toLowerCase().contains(filterPattern) ||
                            item.getLotNumber().toLowerCase().contains(filterPattern) ||
                            item.getProductNumber().toLowerCase().contains(filterPattern) ||
                            (item.getBarcode() != null && item.getBarcode().toLowerCase().contains(filterPattern))) {
                        filteredList.add(item);
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems = (List<MoveOrderItem>) results.values;
            notifyDataSetChanged();
        }
    }

    public void updateItems(List<MoveOrderItem> newItems) {
        this.originalItems = new ArrayList<>(newItems);
        this.filteredItems = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    public List<MoveOrderItem> getOriginalItems() {
        return originalItems;
    }

    public List<MoveOrderItem> getFilteredItems() {
        return filteredItems;
    }

    // **เพิ่มเมธอดใหม่สำหรับตรวจสอบสถานะรายการ**
    public boolean hasExceededItems() {
        for (MoveOrderItem item : filteredItems) {
            double totalScanned = item.getQuantitySuccess() + item.getQuantityScan();
            if (totalScanned > item.getQuantity()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCompletedItems() {
        for (MoveOrderItem item : filteredItems) {
            double totalScanned = item.getQuantitySuccess() + item.getQuantityScan();
            if (totalScanned >= item.getQuantity()) {
                return true;
            }
        }
        return false;
    }

    public int getCompletedItemsCount() {
        int count = 0;
        for (MoveOrderItem item : filteredItems) {
            double totalScanned = item.getQuantitySuccess() + item.getQuantityScan();
            if (totalScanned >= item.getQuantity()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalItemsCount() {
        return filteredItems.size();
    }
}
