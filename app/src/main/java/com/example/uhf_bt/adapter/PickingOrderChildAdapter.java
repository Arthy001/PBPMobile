package com.example.uhf_bt.adapter;

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

import com.example.uhf_bt.ColorConstants;
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

    private boolean isPalletLocked = false;
    private String selectedPallet = "";

    public PickingOrderChildAdapter(Context context, List<PickingOrderItem> items) {
        this.context = context;
        this.originalItems = new ArrayList<>(items != null ? items : new ArrayList<>());
        this.filteredItems = new ArrayList<>(items != null ? items : new ArrayList<>());
        this.inflater = LayoutInflater.from(context);
        getFilter();
    }


    public void setPalletLocked(boolean locked, String pallet) {
        this.isPalletLocked = locked;
        this.selectedPallet = pallet != null ? pallet : "";
        getFilter().filter(isPalletLocked ? selectedPallet : "");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_picking_order_child, parent, false);
            holder = new ViewHolder();
            holder.tvPallet = convertView.findViewById(R.id.tvPallet);
            holder.tvItem = convertView.findViewById(R.id.tvItem);
            //holder.tvProduct = convertView.findViewById(R.id.tvProduct);
            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
            holder.tvQuantitySuccess = convertView.findViewById(R.id.tvQuantitySuccess);
            holder.tvQuantityScan = convertView.findViewById(R.id.tvQuantityScan);
            holder.ivStatus = convertView.findViewById(R.id.ivStatus);

            holder.tvLotSuggestion = convertView.findViewById(R.id.tvLotSuggestion);
            holder.tvLocator = convertView.findViewById(R.id.tvLocator);
            holder.tvSequenceNumber = convertView.findViewById(R.id.tvSequenceNumber);
            holder.tvAllocatedQty = convertView.findViewById(R.id.tvAllocatedQty);
            holder.tvQtyBox = convertView.findViewById(R.id.tvQtyBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PickingOrderItem item = getItem(position);

        if (item != null) {
            // Safely set text with null checks
            holder.tvPallet.setText(String.format("Pallet : %s",
                    item.getPallet() != null ? item.getPallet() : ""));

            //holder.tvProduct.setText(item.getProduct() != null ? item.getProduct() : "");
            holder.tvQuantity.setText(String.format("%.0f", item.getQuantity()));
            holder.tvQuantitySuccess.setText(String.format("%.0f", item.getQuantitySuccess()));
            holder.tvQuantityScan.setText(String.format("%.0f", item.getQuantityScan()));
            holder.tvLotSuggestion.setText(String.format("แนะนำ: %s", item.getLotSuggestion()));
            holder.tvLocator.setText(String.format("Locator: %s", item.getLocator()));
            holder.tvAllocatedQty.setText(String.format("%.0f", item.getAllocatedQty()));
            holder.tvQtyBox.setText(String.format("%s", item.getQtyBox()));
            holder.tvSequenceNumber.setText(item.getSequenceNumber());
            // Set BoxType icon
            if (item.getBoxType() != null) {
                String boxType = item.getBoxType().toLowerCase();
                if (boxType.equals("full box")) {
                    holder.tvItem.setText(String.format("Item : %s",item.getProductNumber()));
                    //holder.ivStatus.setImageResource(R.drawable.ic_fullbox);
                    holder.ivStatus.setVisibility(View.GONE);
                } else if (boxType.equals("renant")) {
                    holder.tvItem.setText(String.format("Item : %s \r\n%s" ,item.getProductNumber(), item.getRemark()));
                    holder.ivStatus.setImageResource(R.drawable.ic_renant);
                    holder.ivStatus.setVisibility(View.VISIBLE);
                } else {
                    holder.ivStatus.setVisibility(View.GONE);
                }

                // Set SequenceNumber
//                if (item.getSequenceNumber() != null && !item.getSequenceNumber().isEmpty()) {
//                    holder.tvSequenceNumber.setText(item.getSequenceNumber());
//                    holder.tvSequenceNumber.setVisibility(View.VISIBLE);
//                } else {
//                    holder.tvSequenceNumber.setVisibility(View.GONE);
//                }

            } else {
                holder.tvSequenceNumber.setText("-");
                //holder.ivStatus.setVisibility(View.GONE);
                //holder.tvSequenceNumber.setVisibility(View.GONE);

            }

            // Set background color
            int backgroundColor = ColorConstants.getMoveOrderItemBackground(
                    item.getQuantity(),
                    item.getQuantitySuccess(),
                    item.getQuantityScan()
            );
            convertView.setBackgroundColor(backgroundColor);

            // Set text colors
            holder.tvPallet.setTextColor(ColorConstants.TEXT_PRIMARY);
            holder.tvItem.setTextColor(ColorConstants.TEXT_PRIMARY);
            //holder.tvProduct.setTextColor(ColorConstants.TEXT_PRIMARY);
            //holder.tvQuantity.setTextColor(ColorConstants.TEXT_SECONDARY);
            //holder.tvQuantitySuccess.setTextColor(ColorConstants.TEXT_SECONDARY);
            //holder.tvQuantityScan.setTextColor(ColorConstants.TEXT_SECONDARY);
            holder.tvSequenceNumber.setTextColor(ColorConstants.TEXT_PRIMARY);
            holder.tvLotSuggestion.setTextColor(ColorConstants.TEXT_SECONDARY);
            holder.tvLocator.setTextColor(ColorConstants.TEXT_SECONDARY);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvPallet;
        TextView tvItem;
        //TextView tvProduct;
        TextView tvQuantity;
        TextView tvQuantitySuccess;
        TextView tvQuantityScan;
        ImageView ivStatus;
        TextView tvSequenceNumber;
        TextView tvLotSuggestion;
        TextView tvLocator;
        TextView tvAllocatedQty;
        TextView tvQtyBox;
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

            try {
                if (!isPalletLocked && (constraint == null || constraint.length() == 0)) {
                    results.values = new ArrayList<>(originalItems);
                    results.count = originalItems.size();
                } else {
                    List<PickingOrderItem> filteredList = new ArrayList<>();
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (PickingOrderItem item : originalItems) {
                        if (item != null) {
                            if (isPalletLocked) {
                                if (item.getPallet() != null &&
                                        item.getPallet().equals(selectedPallet)) {
                                    filteredList.add(item);
                                }
                            } else {
                                if ((item.getProduct() != null &&
                                        item.getProduct().toLowerCase().contains(filterPattern)) ||
                                        (item.getLotNumber() != null &&
                                                item.getLotNumber().toLowerCase().contains(filterPattern)) ||
                                        (item.getPallet() != null &&
                                                item.getPallet().toLowerCase().contains(filterPattern))) {
                                    filteredList.add(item);
                                }
                            }
                        }
                    }

                    results.values = filteredList;
                    results.count = filteredList.size();
                }
            } catch (Exception e) {
                Log.e("PickingOrderAdapter", "Error filtering items: " + e.getMessage());
                results.values = new ArrayList<>(originalItems);
                results.count = originalItems.size();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            try {
                filteredItems = (List<PickingOrderItem>) results.values;
                notifyDataSetChanged();
            } catch (Exception e) {
                Log.e("PickingOrderAdapter", "Error publishing filter results: " + e.getMessage());
            }
        }
    }

    // Other required overrides...
    @Override
    public int getCount() {
        return filteredItems != null ? filteredItems.size() : 0;
    }

    @Override
    public PickingOrderItem getItem(int position) {
        return filteredItems != null && position >= 0 && position < filteredItems.size()
                ? filteredItems.get(position)
                : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateItems(List<PickingOrderItem> newItems) {
        this.originalItems = new ArrayList<>(newItems != null ? newItems : new ArrayList<>());
         this.filteredItems = new ArrayList<>(newItems != null ? newItems : new ArrayList<>());
        notifyDataSetChanged();
    }

    public List<PickingOrderItem> getOriginalItems() {
        return originalItems;
    }

    public List<PickingOrderItem> getFilteredItems() {
        return filteredItems;
    }
}