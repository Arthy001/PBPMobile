package com.example.uhf_bt.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.uhf_bt.R;
import com.example.uhf_bt.models.PalletGroupSummary;
import com.example.uhf_bt.models.PalletStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PalletTagAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = "PalletTagAdapter";
    private final Context context;
    private List<PalletGroupSummary> groupList;
    private List<PalletGroupSummary> filteredGroupList;

    public PalletTagAdapter(Context context) {
        this.context = context;
        this.groupList = new ArrayList<>();
        this.filteredGroupList = new ArrayList<>();
    }

    public void setGroupList(List<PalletGroupSummary> groupList) {
        Log.d(TAG, "Setting group list: " + (groupList != null ? groupList.size() : "null"));
        if (groupList != null) {
            this.groupList = groupList;
            this.filteredGroupList = new ArrayList<>(groupList);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return filteredGroupList.size();
    }

    @Override
    public PalletGroupSummary getItem(int position) {
        return filteredGroupList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pallet_tag, parent, false);
            holder = new ViewHolder();
            holder.tvPickingOrder = convertView.findViewById(R.id.tvPickingOrder);
            holder.tvPickingType = convertView.findViewById(R.id.tvPickingType);
            holder.tvCustomer = convertView.findViewById(R.id.tvCustomer);
            holder.tvProductCount = convertView.findViewById(R.id.tvProductCount);
            holder.tvTotalBoxes = convertView.findViewById(R.id.tvTotalBoxes);
            holder.tvPalletCount = convertView.findViewById(R.id.tvPalletCount);
            holder.layoutBadges = convertView.findViewById(R.id.layoutBadges);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PalletGroupSummary item = getItem(position);
        if (item != null) {
            try {
                // DO Number
                holder.tvPickingOrder.setText("DO: " + item.getPickingNumber());

                // Picking Type
                if (item.getPickingType() != null && !item.getPickingType().isEmpty()) {
                    holder.tvPickingType.setVisibility(View.VISIBLE);
                    holder.tvPickingType.setText("[" + item.getPickingType() + "]");
                } else {
                    holder.tvPickingType.setVisibility(View.GONE);
                }

                // Customer
                holder.tvCustomer.setText("ลูกค้า: " + item.getCustomer());

                // Row 3: สินค้า xxx รายการ (ซ้าย) | xxxx กล่อง (ขวา)
                holder.tvProductCount.setText("สินค้า " + item.getProductCount() + " รายการ");
                holder.tvTotalBoxes.setText(String.format(java.util.Locale.US, "%,.0f กล่อง", item.getTotalQuantity()));

                // Row 4: xxx พาเลท (ซ้าย) ต่อด้วย Status Badges
                holder.tvPalletCount.setText(item.getTotalPallets() + " พาเลท");

                // Populate Status Badges
                populateStatusBadges(holder.layoutBadges, item.getStatusCountMap());

            } catch (Exception e) {
                Log.e(TAG, "Error binding data to view at position " + position, e);
            }
        }

        return convertView;
    }

    private void populateStatusBadges(LinearLayout container, Map<Integer, Integer> statusCountMap) {
        container.removeAllViews();
        if (statusCountMap == null || statusCountMap.isEmpty()) return;

        for (Map.Entry<Integer, Integer> entry : statusCountMap.entrySet()) {
            int status = entry.getKey();
            int count = entry.getValue();

            TextView badge = new TextView(context);
            badge.setText(PalletStatus.getDescription(status) + " (" + count + ")");
            badge.setTextColor(Color.WHITE);
            badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            badge.setTypeface(null, Typeface.BOLD);

            // Compact padding (fit to text)
            int padH = dpToPx(6);
            int padV = dpToPx(2);
            badge.setPadding(padH, padV, padH, padV);

            // Rounded background
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dpToPx(8));
            bg.setColor(getStatusColor(status));
            badge.setBackground(bg);

            // Margin between badges
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, dpToPx(4), 0);
            badge.setLayoutParams(params);

            container.addView(badge);
        }
    }

    private int getStatusColor(int status) {
        switch (status) {
            case 0:  return Color.parseColor("#0288D1"); // New - Light Blue
            case 1:  return Color.parseColor("#F57C00"); // Pick - Deep Orange
            case 2:  return Color.parseColor("#1976D2"); // Pack - Blue
            case 3:  return Color.parseColor("#00897B"); // ReceiveShipping - Teal
            case 4:  return Color.parseColor("#3F51B5"); // RFIDLoading - Indigo
            case 5:  return Color.parseColor("#43A047"); // RFIDLoadingSuccess - Green
            case 6:  return Color.parseColor("#FB8C00"); // Loading - Amber/Orange
            case 7:  return Color.parseColor("#8E24AA"); // Shipped - Purple
            case -1: return Color.parseColor("#E53935"); // Cancelled - Red
            default: return Color.parseColor("#78909C"); // Unknown - Blue Gray
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (groupList == null || groupList.isEmpty()) {
                    results.values = new ArrayList<>();
                    results.count = 0;
                    return results;
                }

                if (constraint == null || constraint.length() == 0) {
                    results.values = new ArrayList<>(groupList);
                    results.count = groupList.size();
                } else {
                    List<PalletGroupSummary> filtered = new ArrayList<>();
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (PalletGroupSummary group : groupList) {
                        if (group != null && (
                                containsIgnoreCase(group.getPickingNumber(), filterPattern) ||
                                        containsIgnoreCase(group.getCustomer(), filterPattern) ||
                                        containsIgnoreCase(group.getPickingType(), filterPattern) ||
                                        containsIgnoreCase(group.getOrderDate(), filterPattern))) {
                            filtered.add(group);
                        }
                    }

                    results.values = filtered;
                    results.count = filtered.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null) {
                    filteredGroupList = (List<PalletGroupSummary>) results.values;
                } else {
                    filteredGroupList = new ArrayList<>();
                }
                notifyDataSetChanged();
            }
        };
    }

    private boolean containsIgnoreCase(String source, String target) {
        return source != null && source.toLowerCase().contains(target);
    }

    private static class ViewHolder {
        TextView tvPickingOrder;
        TextView tvPickingType;
        TextView tvCustomer;
        TextView tvProductCount;
        TextView tvTotalBoxes;
        TextView tvPalletCount;
        LinearLayout layoutBadges;
    }
}
