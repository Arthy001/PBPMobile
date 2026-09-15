package com.example.uhf_bt.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.uhf_bt.R;
import com.example.uhf_bt.models.ProductItemLite;

import java.util.ArrayList;
import java.util.List;

public class ProductItemAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<ProductItemLite> originalItems;
    private List<ProductItemLite> filteredItems;
    private LayoutInflater inflater;
    private ItemFilter itemFilter;

    public ProductItemAdapter(Context context, List<ProductItemLite> items) {
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
    public ProductItemLite getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_product_item, parent, false);
            holder = new ViewHolder();
            holder.tvProductNumber = convertView.findViewById(R.id.tvProductNumber);
            holder.tvProductName = convertView.findViewById(R.id.tvProductName);
            holder.tvProductGroup = convertView.findViewById(R.id.tvProductGroup);
            holder.tvTagNumber = convertView.findViewById(R.id.tvTagNumber);
            holder.tvLotNumber = convertView.findViewById(R.id.tvLotNumber);
            holder.tvLocation = convertView.findViewById(R.id.tvLocation);
            holder.tvStatus = convertView.findViewById(R.id.tvStatus);
            holder.ivScannedStatus = convertView.findViewById(R.id.ivScannedStatus);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ProductItemLite item = getItem(position);

        holder.tvProductNumber.setText("Item: " + item.getProductNumber());
        holder.tvProductName.setText(item.getProductName());
        holder.tvProductGroup.setText("Group: " + (item.getProductGroupName() != null ? item.getProductGroupName() : "N/A"));
        holder.tvTagNumber.setText("Tag: " + (item.getTagNumber() != null ? item.getTagNumber() : "N/A"));
        holder.tvLotNumber.setText("SN: " + (item.getLotNumber() != null ? item.getLotNumber() : "N/A"));
       // holder.tvLocation.setText("Location: " + (item.getLocationName() != null ? item.getLocationName() : "N/A"));

        // Set status with color coding
        String statusText = getStatusText(item.getProductItemStatus() != null ? item.getProductItemStatus() : -100);
        holder.tvStatus.setText("Status : " + statusText);
        holder.tvStatus.setTextColor(getStatusColor(item.getProductItemStatus() != null ? item.getProductItemStatus() : -100));

        // Set scanned status icon
        if (item.isScanned()) {
            holder.ivScannedStatus.setVisibility(View.VISIBLE);
            holder.ivScannedStatus.setImageResource(R.drawable.ic_check_circle);
            convertView.setBackgroundColor(Color.parseColor("#E8F5E8")); // Light green
        } else {
            holder.ivScannedStatus.setVisibility(View.GONE);
            convertView.setBackgroundColor(Color.WHITE);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvProductNumber;
        TextView tvProductName;
        TextView tvProductGroup;
        TextView tvTagNumber;
        TextView tvLotNumber;
        TextView tvLocation;
        TextView tvStatus;
        ImageView ivScannedStatus;
    }

    private String getStatusText(int status) {
        switch (status) {
            case -1: return "Cancel";
            case -2: return "Out of Stock";
            case 0: return "NEW";
            case 1: return "STOCK";
            case 2: return "PICK";
            case 3: return "PACK";
            case 4: return "RECEIVE SHIPPING";
            case 5: return "PREPARING SHIP";
            case 6: return "LOADING";
            case 7: return "SHIPPED";
            case 8: return "Move Out";
            case 98: return "Adjust--";
            case 99: return "Adjust++";
            default: return "Unknown (" + status + ")";
        }
    }

    private int getStatusColor(int status) {
        switch (status) {
            case 0: return Color.parseColor("#FF9800"); // NEW - Orange
            case 1: return Color.parseColor("#4CAF50"); // STOCK - Green
            case 2: return Color.parseColor("#2196F3"); // PICK - Blue
            case 3: return Color.parseColor("#673AB7"); // PACK - Deep Purple
            case 4: return Color.parseColor("#009688"); // RECEIVE - Teal
            case 5: return Color.parseColor("#3F51B5"); // PREPARING - Indigo
            case 6: return Color.parseColor("#FFC107"); // LOADING - Amber
            case 7: return Color.parseColor("#9C27B0"); // SHIPPED - Purple
            case 8: return Color.parseColor("#795548"); // MOVE OUT - Brown
            case 98: 
            case 99: return Color.parseColor("#607D8B"); // ADJUST - Blue Gray
            default: return Color.parseColor("#424242"); // Dark Gray
        }
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
                List<ProductItemLite> filteredList = new ArrayList<>();
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (ProductItemLite item : originalItems) {
                    // Filter by product name, product number, tag number, lot number, product group, or location
                    if ((item.getProductName() != null && item.getProductName().toLowerCase().contains(filterPattern)) ||
                            (item.getProductNumber() != null && item.getProductNumber().toLowerCase().contains(filterPattern)) ||
                            (item.getTagNumber() != null && item.getTagNumber().toLowerCase().contains(filterPattern)) ||
                            (item.getLotNumber() != null && item.getLotNumber().toLowerCase().contains(filterPattern)) ||
                            (item.getProductGroupName() != null && item.getProductGroupName().toLowerCase().contains(filterPattern)) ||
                           // (item.getLocationName() != null && item.getLocationName().toLowerCase().contains(filterPattern)) ||
                            (item.getWarehouseName() != null && item.getWarehouseName().toLowerCase().contains(filterPattern))) {
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
            filteredItems = (List<ProductItemLite>) results.values;
            notifyDataSetChanged();
        }
    }

    public void updateItems(List<ProductItemLite> newItems) {
        this.originalItems = new ArrayList<>(newItems);
        this.filteredItems = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    public List<ProductItemLite> getOriginalItems() {
        return originalItems;
    }

    public List<ProductItemLite> getFilteredItems() {
        return filteredItems;
    }
}
