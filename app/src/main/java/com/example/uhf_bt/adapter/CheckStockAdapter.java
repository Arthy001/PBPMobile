package com.example.uhf_bt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.uhf_bt.R;
import com.example.uhf_bt.models.CheckStock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckStockAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<CheckStock> checkStocks;
    private List<CheckStock> filteredCheckStocks;
    private Filter checkStockFilter;
    private SimpleDateFormat inputFormat;
    private SimpleDateFormat outputFormat;

    public CheckStockAdapter(Context context) {
        this.context = context;
        this.checkStocks = new ArrayList<>();
        this.filteredCheckStocks = new ArrayList<>();

        // Initialize date formatters
        this.inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        this.outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
    }

    public void setCheckStocks(List<CheckStock> checkStocks) {
        this.checkStocks = checkStocks;
        this.filteredCheckStocks = new ArrayList<>(checkStocks);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return filteredCheckStocks.size();
    }

    @Override
    public CheckStock getItem(int position) {
        return filteredCheckStocks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_check_stock, parent, false);
            holder = new ViewHolder();
            holder.numberText = convertView.findViewById(R.id.numberText);
            holder.dateText = convertView.findViewById(R.id.dateText);
            holder.warehouseText = convertView.findViewById(R.id.warehouseText);
            holder.statusText = convertView.findViewById(R.id.statusText);
            holder.descriptionText = convertView.findViewById(R.id.descriptionText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CheckStock checkStock = getItem(position);

        // Format date
        String formattedDate = formatDate(checkStock.getCheckStockDate());

        // Set values
        holder.numberText.setText("Check Stock: " + checkStock.getCheckStockNumber());
        holder.dateText.setText("Date: " + formattedDate);
        holder.warehouseText.setText(String.format("Warehouse: %s", checkStock.getWarehouseName()));

        if (checkStock.getDescription() != null && !checkStock.getDescription().trim().isEmpty()) {
            holder.descriptionText.setText(String.format("Description: %s", checkStock.getDescription()));
            holder.descriptionText.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionText.setVisibility(View.GONE);
        }

        // Set status with color
        setStatus(holder.statusText, checkStock.getStatus());

        return convertView;
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "-";
        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    private void setStatus(TextView statusText, String status) {
        String statusStr = status != null ? status : "Unknown";
        int textColor;

        switch (statusStr.toLowerCase()) {
            case "new":
                textColor = context.getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case "inprogress":
            case "in progress":
                textColor = context.getResources().getColor(android.R.color.holo_orange_light);
                break;
            case "completed":
            case "success":
                textColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case "cancel":
            case "cancelled":
                textColor = context.getResources().getColor(android.R.color.holo_orange_dark);
                break;
            default:
                textColor = context.getResources().getColor(android.R.color.darker_gray);
        }

        statusText.setText(statusStr);
        statusText.setTextColor(textColor);
    }

    @Override
    public Filter getFilter() {
        if (checkStockFilter == null) {
            checkStockFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();

                    if (checkStocks == null || checkStocks.isEmpty()) {
                        results.values = new ArrayList<>();
                        results.count = 0;
                        return results;
                    }

                    if (constraint == null || constraint.length() == 0) {
                        results.values = new ArrayList<>(checkStocks);
                        results.count = checkStocks.size();
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        List<CheckStock> filteredList = new ArrayList<>();

                        for (CheckStock order : checkStocks) {
                            if (order.getCheckStockNumber() != null && order.getCheckStockNumber().toLowerCase().contains(filterPattern) ||
                                    order.getWarehouseName() != null && order.getWarehouseName().toLowerCase().contains(filterPattern)) {
                                filteredList.add(order);
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
                    filteredCheckStocks = (List<CheckStock>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
        return checkStockFilter;
    }

    private static class ViewHolder {
        TextView numberText;
        TextView dateText;
        TextView warehouseText;
        TextView statusText;
        TextView descriptionText;
    }
}
