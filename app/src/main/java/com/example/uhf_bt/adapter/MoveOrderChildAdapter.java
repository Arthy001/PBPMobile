package com.example.uhf_bt.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

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
            holder.tvBarcode = convertView.findViewById(R.id.tvBarcode);  // Added for barcode
            holder.tvAllocatedQty = convertView.findViewById(R.id.tvAllocatedQty);
            holder.tvSTD = convertView.findViewById(R.id.tvSTD);
            holder.tvBoxType = convertView.findViewById(R.id.tvBoxType);
            holder.ivStatus = convertView.findViewById(R.id.ivStatus);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MoveOrderItem item = getItem(position);

        holder.tvItem.setText("Item : " + item.getProductNumber());
        holder.tvProduct.setText(item.getProduct());
        holder.tvLot.setText("Lot: " + item.getLot());
        holder.tvBarcode.setText(item.getBarcode());  // Set the barcode text

        // ใช้ format ตัวเลขไม่มีทศนิยมสำหรับข้อมูลปริมาณ
        holder.tvQuantity.setText(String.format("%.0f", item.getQuantity()));
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

        // Set background color using the utility method from ColorConstants
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
        TextView tvBarcode;           // Added for barcode
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
                    // Filter by product name, lot number, or product number
                    if (item.getProduct().toLowerCase().contains(filterPattern) ||
                            item.getLotNumber().toLowerCase().contains(filterPattern) ||
                            item.getProductNumber().toLowerCase().contains(filterPattern)) {
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
}

//package com.example.uhf_bt.adapter;
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.Filter;
//import android.widget.Filterable;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.example.uhf_bt.ColorConstants;
//import com.example.uhf_bt.R;
//import com.example.uhf_bt.models.MoveOrderItem;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MoveOrderChildAdapter extends BaseAdapter implements Filterable {
//    private Context context;
//    private List<MoveOrderItem> originalItems;
//    private List<MoveOrderItem> filteredItems;
//    private LayoutInflater inflater;
//    private ItemFilter itemFilter;
//
//    public MoveOrderChildAdapter(Context context, List<MoveOrderItem> items) {
//        this.context = context;
//        this.originalItems = new ArrayList<>(items);
//        this.filteredItems = new ArrayList<>(items);
//        this.inflater = LayoutInflater.from(context);
//        getFilter();
//    }
//
//    @Override
//    public int getCount() {
//        return filteredItems.size();
//    }
//
//    @Override
//    public MoveOrderItem getItem(int position) {
//        return filteredItems.get(position);
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
//            convertView = inflater.inflate(R.layout.item_move_order_child, parent, false);
//            holder = new ViewHolder();
//            holder.tvItem = convertView.findViewById(R.id.tvItem);
//            holder.tvProduct = convertView.findViewById(R.id.tvProduct);
//            holder.tvLot = convertView.findViewById(R.id.tvLot);
//            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
//            holder.tvQuantitySuccess = convertView.findViewById(R.id.tvQuantitySuccess);
//            holder.tvQuantityScan = convertView.findViewById(R.id.tvQuantityScan);
//            holder.ivStatus = convertView.findViewById(R.id.ivStatus);
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        MoveOrderItem item = getItem(position);
//        double xxx= item.getQuantity();
//
//        holder.tvItem.setText("Item : " + item.getProductNumber());
//        holder.tvProduct.setText(item.getProduct());
//        holder.tvLot.setText("Lot: " + item.getLot());
//        holder.tvQuantity.setText(String.format("Quantity: %.0f", item.getQuantity()));
//        holder.tvQuantitySuccess.setText(String.format("Success: %.0f", item.getQuantitySuccess()));
//        holder.tvQuantityScan.setText(String.format("Scan: %.0f", item.getQuantityScan()));
//
//
//        // Set background color using the utility method from ColorConstants
//        int backgroundColor = ColorConstants.getMoveOrderItemBackground(
//                item.getQuantity(),
//                item.getQuantitySuccess(),
//                item.getQuantityScan()
//        );
//        convertView.setBackgroundColor(backgroundColor);
//
//        // Set text colors using global constants
//        holder.tvItem.setTextColor(ColorConstants.TEXT_PRIMARY);
//        holder.tvProduct.setTextColor(ColorConstants.TEXT_PRIMARY);
//        holder.tvLot.setTextColor(ColorConstants.TEXT_SECONDARY);
//        holder.tvQuantity.setTextColor(ColorConstants.TEXT_SECONDARY);
//        holder.tvQuantitySuccess.setTextColor(ColorConstants.TEXT_SECONDARY);
//        holder.tvQuantityScan.setTextColor(ColorConstants.TEXT_SECONDARY);
//
//        return convertView;
//    }
//
//    private static class ViewHolder {
//        TextView tvItem;
//        TextView tvProduct;
//        TextView tvLot;
//        TextView tvQuantity;
//        ImageView ivStatus;
//        TextView tvQuantitySuccess;
//        TextView tvQuantityScan;
//    }
//
//    @Override
//    public Filter getFilter() {
//        if (itemFilter == null) {
//            itemFilter = new ItemFilter();
//        }
//        return itemFilter;
//    }
//
//    private class ItemFilter extends Filter {
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//            FilterResults results = new FilterResults();
//
//            if (constraint == null || constraint.length() == 0) {
//                results.values = new ArrayList<>(originalItems);
//                results.count = originalItems.size();
//            } else {
//                List<MoveOrderItem> filteredList = new ArrayList<>();
//                String filterPattern = constraint.toString().toLowerCase().trim();
//
//                for (MoveOrderItem item : originalItems) {
//                    // Filter by product name, lot number
//                    if (item.getProduct().toLowerCase().contains(filterPattern) ||
//                            item.getLotNumber().toLowerCase().contains(filterPattern)) {
//                        filteredList.add(item);
//                    }
//                }
//
//                results.values = filteredList;
//                results.count = filteredList.size();
//            }
//
//            return results;
//        }
//
//        @SuppressWarnings("unchecked")
//        @Override
//        protected void publishResults(CharSequence constraint, FilterResults results) {
//            filteredItems = (List<MoveOrderItem>) results.values;
//            notifyDataSetChanged();
//        }
//    }
//
//    public void updateItems(List<MoveOrderItem> newItems) {
//        this.originalItems = new ArrayList<>(newItems);
//        this.filteredItems = new ArrayList<>(newItems);
//        notifyDataSetChanged();
//    }
//
//    public List<MoveOrderItem> getOriginalItems() {
//        return originalItems;
//    }
//
//    public List<MoveOrderItem> getFilteredItems() {
//        return filteredItems;
//    }
//}