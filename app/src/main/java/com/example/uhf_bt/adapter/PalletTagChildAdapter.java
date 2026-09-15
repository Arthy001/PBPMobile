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
import com.example.uhf_bt.fragment.PalletTagChildFragment;
import com.example.uhf_bt.models.PalletTransaction;

import java.util.ArrayList;
import java.util.List;

public class PalletTagChildAdapter extends BaseAdapter implements Filterable {
    private final Context context;
    private List<PalletTransaction> transactions;
    private List<PalletTransaction> filteredTransactions;
    private final LayoutInflater inflater;

    public PalletTagChildAdapter(Context context) {
        this.context = context;
        this.transactions = new ArrayList<>();
        this.filteredTransactions = new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
    }

    public void setTransactions(List<PalletTransaction> transactions) {
        this.transactions = new ArrayList<>(transactions);
        this.filteredTransactions = new ArrayList<>(transactions);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return filteredTransactions.size();
    }

    @Override
    public PalletTransaction getItem(int position) {
        return filteredTransactions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_pallet_tag_child, parent, false);
            holder = new ViewHolder();
            holder.tvItemNumber = convertView.findViewById(R.id.tvItemNumber);
            holder.tvProduct = convertView.findViewById(R.id.tvProduct);
            holder.tvLot = convertView.findViewById(R.id.tvLot);
            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
            holder.tvTagId = convertView.findViewById(R.id.tvTagId);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PalletTransaction transaction = getItem(position);
        if (transaction != null) {
            holder.tvItemNumber.setText(String.format("Item: %s", transaction.getItemNumber()));
            holder.tvProduct.setText(transaction.getProduct());
            holder.tvLot.setText(String.format("Lot: %s", transaction.getLot()));
            holder.tvQuantity.setText(String.format("Quantity: %d", transaction.getQuantity()));
            holder.tvTagId.setText(String.format("Tag: %s", transaction.getTagId()));
        }

        return convertView;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder holder;
//
//        if (convertView == null) {
//            convertView = inflater.inflate(R.layout.item_pallet_tag_child, parent, false);
//            holder = new ViewHolder();
//            holder.tvItemNumber = convertView.findViewById(R.id.tvItemNumber);
//            holder.tvProduct = convertView.findViewById(R.id.tvProduct);
//            holder.tvLotNumber = convertView.findViewById(R.id.tvLotNumber);
//            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
//            holder.tvTransactionDate = convertView.findViewById(R.id.tvTransactionDate);
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        PalletTransaction transaction = getItem(position);
//
//        holder.tvItemNumber.setText(String.format("Item: %s", transaction.getItemNumber()));
//        holder.tvProduct.setText(transaction.getProduct());
//        holder.tvLotNumber.setText(String.format("Lot: %s", transaction.getLotNumber()));
//        holder.tvQuantity.setText(String.format("Quantity: %d", transaction.getQuantity()));
//        holder.tvTransactionDate.setText(String.format("Date: %s", transaction.getCreatedAtUtc()));
//
//        return convertView;
//    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    results.values = transactions;
                    results.count = transactions.size();
                } else {
                    List<PalletTransaction> filtered = new ArrayList<>();
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (PalletTransaction transaction : transactions) {
                        if (transaction.getProduct().toLowerCase().contains(filterPattern) ||
                                transaction.getItemNumber().toLowerCase().contains(filterPattern) ||
                                transaction.getLotNumber().toLowerCase().contains(filterPattern)) {
                            filtered.add(transaction);
                        }
                    }

                    results.values = filtered;
                    results.count = filtered.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredTransactions = (List<PalletTransaction>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private static class ViewHolder {
        TextView tvItemNumber;
        TextView tvProduct;
        TextView tvLot;
        TextView tvQuantity;
        TextView tvTagId;
    }
}
