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
import com.example.uhf_bt.models.MoveOrder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoveOrderAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<MoveOrder> moveOrders;
    private List<MoveOrder> filteredMoveOrders;
    private Filter moveOrderFilter;
    private SimpleDateFormat inputFormat;
    private SimpleDateFormat outputFormat;

    public MoveOrderAdapter(Context context) {
        this.context = context;
        this.moveOrders = new ArrayList<>();
        this.filteredMoveOrders = new ArrayList<>();

        // Initialize date formatters
        this.inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        this.outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
    }

    public void setMoveOrders(List<MoveOrder> moveOrders) {
        this.moveOrders = moveOrders;
        this.filteredMoveOrders = new ArrayList<>(moveOrders);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return filteredMoveOrders.size();
    }

    @Override
    public MoveOrder getItem(int position) {
        return filteredMoveOrders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_move_order, parent, false);
            holder = new ViewHolder();
            holder.numberText = convertView.findViewById(R.id.numberText);
            holder.dateText = convertView.findViewById(R.id.dateText);
            holder.sourceText = convertView.findViewById(R.id.sourceText);
            //holder.amountText = convertView.findViewById(R.id.amountText);
            holder.statusText = convertView.findViewById(R.id.statusText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MoveOrder moveOrder = getItem(position);

        // Format date
        String formattedDate = formatDate(moveOrder.getOrderDate());

        // Set values
        holder.numberText.setText("Order: " + moveOrder.getNumber());
        holder.dateText.setText("Date: " + formattedDate);
        holder.sourceText.setText(String.format("From: %s To: %s",
                moveOrder.getWarehouseSource(),
                moveOrder.getWarehouseDestination()));
        //holder.amountText.setText(String.format("Amount: ฿%.2f", moveOrder.getAfterTaxAmount()));

        // Set status with color
        setStatus(holder.statusText, moveOrder.getStatus());

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
            case 0:
                statusStr = "New";
                textColor = context.getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case 1:
                statusStr = "Success";
                textColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case -1:
                statusStr = "Cancel";
                textColor = context.getResources().getColor(android.R.color.holo_orange_dark);
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
        if (moveOrderFilter == null) {
            moveOrderFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();

                    if (constraint == null || constraint.length() == 0) {
                        results.values = new ArrayList<>(moveOrders);
                        results.count = moveOrders.size();
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        List<MoveOrder> filteredList = new ArrayList<>();

                        for (MoveOrder order : moveOrders) {
                            if (order.getNumber().toLowerCase().contains(filterPattern) ||
                                    order.getWarehouseSource().toLowerCase().contains(filterPattern) ||
                                    order.getWarehouseDestination().toLowerCase().contains(filterPattern)) {
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
                    filteredMoveOrders = (List<MoveOrder>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
        return moveOrderFilter;
    }

    private static class ViewHolder {
        TextView numberText;
        TextView dateText;
        TextView sourceText;
        TextView amountText;
        TextView statusText;
    }
}