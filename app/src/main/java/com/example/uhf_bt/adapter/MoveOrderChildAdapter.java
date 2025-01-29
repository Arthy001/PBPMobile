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
            holder.ivStatus = convertView.findViewById(R.id.ivStatus);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MoveOrderItem item = getItem(position);
        double xxx= item.getQuantity();

        holder.tvItem.setText("Item : " + item.getProductNumber());
        holder.tvProduct.setText(item.getProduct());
        holder.tvLot.setText("Lot: " + item.getLot());
        holder.tvQuantity.setText(String.format("Quantity: %.0f", item.getQuantity()));
        holder.tvQuantitySuccess.setText(String.format("Success: %.0f", item.getQuantitySuccess()));
        holder.tvQuantityScan.setText(String.format("Scan: %.0f", item.getQuantityScan()));


        // Set status icon based on moveTransStatus
//        int statusIcon;
//        switch(item.getMoveTransStatus()) {
//            case 1: // Completed
//                statusIcon = R.drawable.ic_check_circle;
//                break;
//            case 2: // In Progress
//                statusIcon = R.drawable.ic_pending;
//                break;
//            default: // Not Started
//                statusIcon = R.drawable.ic_not_started;
//                break;
//        }
//        holder.ivStatus.setImageResource(statusIcon);

        return convertView;
    }

    private static class ViewHolder {
        TextView tvItem;
        TextView tvProduct;
        TextView tvLot;
        TextView tvQuantity;
        ImageView ivStatus;
        TextView tvQuantitySuccess;
        TextView tvQuantityScan;
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
                    // Filter by product name, lot number
                    if (item.getProduct().toLowerCase().contains(filterPattern) ||
                            item.getLotNumber().toLowerCase().contains(filterPattern)) {
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