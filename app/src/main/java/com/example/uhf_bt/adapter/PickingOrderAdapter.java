package com.example.uhf_bt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.uhf_bt.models.PickingOrder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.uhf_bt.R;
import com.example.uhf_bt.models.PickingOrder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PickingOrderAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<PickingOrder> pickingOrders;
    private List<PickingOrder> filteredPickingOrders;
    private Filter pickingOrderFilter;
    private SimpleDateFormat inputFormat;
    private SimpleDateFormat outputFormat;

    public PickingOrderAdapter(Context context) {
        this.context = context;
        this.pickingOrders = new ArrayList<>();
        this.filteredPickingOrders = new ArrayList<>();

        // Initialize date formatters
        this.inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        this.outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
    }

    public void setPickingOrders(List<PickingOrder> pickingOrders) {
        this.pickingOrders = pickingOrders;
        this.filteredPickingOrders = new ArrayList<>(pickingOrders);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return filteredPickingOrders.size();
    }

    @Override
    public PickingOrder getItem(int position) {
        return filteredPickingOrders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_picking_order, parent, false);
            holder = new ViewHolder();
            holder.numberText = convertView.findViewById(R.id.numberText);
            holder.dateText = convertView.findViewById(R.id.dateText);
            holder.customerText = convertView.findViewById(R.id.customerText);
            holder.amountText = convertView.findViewById(R.id.amountText);
            holder.statusText = convertView.findViewById(R.id.statusText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PickingOrder pickingOrder = getItem(position);

        // Format date
        String formattedDate = formatDate(pickingOrder.getOrderDate());

        // Set values
        holder.numberText.setText("Order: " + pickingOrder.getNumber());
        holder.dateText.setText("Date: " + formattedDate);
        holder.customerText.setText("Customer: " + pickingOrder.getCustomer());
        holder.amountText.setText(String.format("Amount: ฿%.2f", pickingOrder.getAfterTaxAmount()));

        // Set status with color
        setStatus(holder.statusText, pickingOrder.getOrderStatus());

        return convertView;
    }

    private String formatDate(String dateString) {
        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    private void setStatus(TextView statusText, int status) {
        String statusStr;
        int textColor;

        switch (status) {
            case 1:
                statusStr = "New";
                textColor = context.getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case 2:
                statusStr = "In Progress";
                textColor = context.getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case 3:
                statusStr = "Completed";
                textColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case 4:
                statusStr = "Cancelled";
                textColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            default:
                statusStr = "Unknown";
                textColor = context.getResources().getColor(android.R.color.darker_gray);
        }

        statusText.setText(statusStr);
        statusText.setTextColor(textColor);
    }

    @Override
    public Filter getFilter() {
        if (pickingOrderFilter == null) {
            pickingOrderFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();

                    if (constraint == null || constraint.length() == 0) {
                        results.values = new ArrayList<>(pickingOrders);
                        results.count = pickingOrders.size();
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        List<PickingOrder> filteredList = new ArrayList<>();

                        for (PickingOrder order : pickingOrders) {
                            if (order.getNumber().toLowerCase().contains(filterPattern) ||
                                    (order.getCustomer() != null &&
                                            order.getCustomer().toLowerCase().contains(filterPattern)) ||
                                    (order.getWarehouseSource() != null &&
                                            order.getWarehouseSource().toLowerCase().contains(filterPattern)) ||
                                    (order.getWarehouseDestination() != null &&
                                            order.getWarehouseDestination().toLowerCase().contains(filterPattern))) {
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
                    filteredPickingOrders = (List<PickingOrder>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
        return pickingOrderFilter;
    }

    private static class ViewHolder {
        TextView numberText;
        TextView dateText;
        TextView customerText;
        TextView amountText;
        TextView statusText;
    }

    public List<PickingOrder> getOriginalPickingOrders() {
        return pickingOrders;
    }

    public List<PickingOrder> getFilteredPickingOrders() {
        return filteredPickingOrders;
    }
}
