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
import com.example.uhf_bt.models.PickingOrderItem;

import java.util.ArrayList;
import java.util.List;

public class PickingOrderChildAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<PickingOrderItem> originalItems;
    private List<PickingOrderItem> filteredItems;
    private LayoutInflater inflater;
    private ItemFilter itemFilter;

    public PickingOrderChildAdapter(Context context, List<PickingOrderItem> items) {
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
    public PickingOrderItem getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_picking_order_child, parent, false);
            holder = new ViewHolder();
            holder.tvProduct = convertView.findViewById(R.id.tvProduct);
            holder.tvLot = convertView.findViewById(R.id.tvLot);
            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
            holder.ivStatus = convertView.findViewById(R.id.ivStatus);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PickingOrderItem item = getItem(position);

        holder.tvProduct.setText(item.getProduct());
        holder.tvLot.setText("Lot: " + item.getLotNumber());
        holder.tvQuantity.setText(String.format("Quantity: %d", item.getQuantity()));

        // Set status icon based on status
//        int statusIcon;
//        switch(item.getPickingTransStatus()) {
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
        TextView tvProduct;
        TextView tvLot;
        TextView tvQuantity;
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
                List<PickingOrderItem> filteredList = new ArrayList<>();
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (PickingOrderItem item : originalItems) {
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
            filteredItems = (List<PickingOrderItem>) results.values;
            notifyDataSetChanged();
        }
    }

    public void updateItems(List<PickingOrderItem> newItems) {
        this.originalItems = new ArrayList<>(newItems);
        this.filteredItems = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    public List<PickingOrderItem> getOriginalItems() {
        return originalItems;
    }

    public List<PickingOrderItem> getFilteredItems() {
        return filteredItems;
    }
}