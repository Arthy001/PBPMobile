package com.example.uhf_bt.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ReceivePalletAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<PickingOrderItem> originalItems;
    private List<PickingOrderItem> filteredItems;
    private LayoutInflater inflater;
    private ReceivePalletAdapter.ItemFilter itemFilter;

    private boolean isPalletLocked = false;
    private String selectedPallet = "";

    public ReceivePalletAdapter(Context context, List<PickingOrderItem> items) {
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
        ReceivePalletAdapter.ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_receive_pallet, parent, false);
            holder = new ReceivePalletAdapter.ViewHolder();
            holder.tvPallet = convertView.findViewById(R.id.tvPallet);
            holder.tvItem = convertView.findViewById(R.id.tvItem);
            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
            holder.tvQuantitySuccess = convertView.findViewById(R.id.tvQuantitySuccess);
            holder.tvQuantityScan = convertView.findViewById(R.id.tvQuantityScan);
            holder.ivStatus = convertView.findViewById(R.id.ivStatus);
            holder.tvOrder = convertView.findViewById(R.id.tvOrder);
            holder.tvCustomer = convertView.findViewById(R.id.tvCustomer);
            holder.tvSequenceNumber = convertView.findViewById(R.id.tvSequenceNumber);
            holder.tvAllocatedQty = convertView.findViewById(R.id.tvAllocatedQty);
            holder.tvQtyBox = convertView.findViewById(R.id.tvQtyBox);
            convertView.setTag(holder);
        } else {
            holder = (ReceivePalletAdapter.ViewHolder) convertView.getTag();
        }

        PickingOrderItem item = getItem(position);

        if (item != null) {
            // *** เปลี่ยนการแสดงผล Pallet และ DO ให้ชัดเจนขึ้น ***
            holder.tvPallet.setText(String.format("Pallet : %s",
                    item.getPalletName() != null ? item.getPalletName() : ""));

            holder.tvQuantity.setText(String.format("%.0f", item.getQuantity()));
            holder.tvQuantitySuccess.setText(String.format("%.0f", item.getQuantitySuccess()));
            holder.tvQuantityScan.setText(String.format("%.0f", item.getQuantityScan()));

            // *** ปรับปรุงการแสดง DO ให้เด่นขึ้น ***
            String doText = item.getPickingNumber();
            if (doText != null && doText.contains(",")) {
                // หากมีหลาย DO ให้แสดงจำนวน
                String[] doArray = doText.split(",");
                holder.tvOrder.setText(String.format("DO: %s (+%d more)",
                        doArray[0].trim(), doArray.length - 1));
                holder.tvOrder.setTextColor(Color.parseColor("#FF9800")); // Orange for multiple DO
            } else {
                holder.tvOrder.setText(String.format("DO: %s", doText != null ? doText : ""));
                holder.tvOrder.setTextColor(ColorConstants.TEXT_PRIMARY);
            }

            holder.tvCustomer.setText(String.format("ลูกค้า: %s", item.getCustomer()));
            holder.tvAllocatedQty.setText(String.format("%.0f", item.getAllocatedQty()));
            holder.tvQtyBox.setText(String.format("%s", item.getQtyBox()));
            holder.tvSequenceNumber.setText(item.getSequenceNumber());

            // *** ปรับปรุงการแสดง Item information ***
            if (item.getBoxType() != null) {
                String boxType = item.getBoxType().toLowerCase();
                if (boxType.equals("full box")) {
                    // สำหรับ full box แสดง product number และ box type
                    holder.tvItem.setText(String.format("Item : %s [%s]",
                            item.getProductNumber(), item.getBoxType()));
                    holder.ivStatus.setVisibility(View.GONE);
                } else if (boxType.equals("kanban")) {
                    // สำหรับ Kanban แสดงหัวข้อให้ชัดเจน
                    holder.tvItem.setText(String.format("Kanban : %s", item.getProductNumber()));
                    holder.tvItem.setTextColor(Color.parseColor("#0277BD"));
                    holder.ivStatus.setVisibility(View.GONE);
                } else if (boxType.equals("renant")) {
                    // สำหรับ renant แสดง product number, box type และ remark
                    String displayText = String.format("Item : %s [%s]",
                            item.getProductNumber(), item.getBoxType());
                    if (item.getRemark() != null && !item.getRemark().isEmpty()) {
                        displayText += "\n" + item.getRemark();
                    }
                    holder.tvItem.setText(displayText);
                    holder.ivStatus.setImageResource(R.drawable.ic_renant);
                    holder.ivStatus.setVisibility(View.VISIBLE);
                } else {
                    // สำหรับ box type อื่นๆ
                    holder.tvItem.setText(String.format("Item : %s [%s]",
                            item.getProductNumber(), item.getBoxType()));
                    holder.ivStatus.setVisibility(View.GONE);
                }
            } else {
                holder.tvItem.setText(String.format("Item : %s", item.getProductNumber()));
                holder.tvSequenceNumber.setText("-");
            }

            // *** ปรับปรุงการแสดงสี background ตามสถานะ ***
            int backgroundColor = ColorConstants.getMoveOrderItemBackground(
                    item.getQuantity(),
                    item.getQuantitySuccess(),
                    item.getQuantityScan()
            );
            convertView.setBackgroundColor(backgroundColor);

            // *** เพิ่มการแสดง border สำหรับ DO ที่แตกต่าง ***
            if (position > 0) {
                PickingOrderItem previousItem = getItem(position - 1);
                if (previousItem != null &&
                        !Objects.equals(item.getPickingNumber(), previousItem.getPickingNumber())) {
                    // เพิ่ม divider สำหรับ DO ใหม่
                    convertView.setPadding(0, 8, 0, 0);
                } else {
                    convertView.setPadding(0, 0, 0, 0);
                }
            } else {
                convertView.setPadding(0, 0, 0, 0);
            }

            // Set text colors
            holder.tvPallet.setTextColor(ColorConstants.TEXT_PRIMARY);
            holder.tvItem.setTextColor(ColorConstants.TEXT_PRIMARY);
            holder.tvSequenceNumber.setTextColor(ColorConstants.TEXT_PRIMARY);
            holder.tvCustomer.setTextColor(ColorConstants.TEXT_PRIMARY);
        }

        return convertView;
    }

    // *** เพิ่ม method สำหรับนับจำนวน DO ที่แตกต่างกัน ***
    public int getUniqueDOCount() {
        if (filteredItems == null || filteredItems.isEmpty()) {
            return 0;
        }

        Set<String> uniqueDOs = new HashSet<>();
        for (PickingOrderItem item : filteredItems) {
            if (item.getPickingNumber() != null) {
                // Handle multiple DOs in single item
                String[] doArray = item.getPickingNumber().split(",");
                for (String doNumber : doArray) {
                    uniqueDOs.add(doNumber.trim());
                }
            }
        }

        return uniqueDOs.size();
    }

    // *** เพิ่ม method สำหรับแสดงข้อมูล DO ***
    public String getDOSummary() {
        if (filteredItems == null || filteredItems.isEmpty()) {
            return "No DOs";
        }

        Map<String, Integer> doItemCount = new HashMap<>();

        for (PickingOrderItem item : filteredItems) {
            if (item.getPickingNumber() != null) {
                String[] doArray = item.getPickingNumber().split(",");
                for (String doNumber : doArray) {
                    String trimmedDO = doNumber.trim();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        doItemCount.put(trimmedDO, doItemCount.getOrDefault(trimmedDO, 0) + 1);
                    }
                }
            }
        }

        StringBuilder summary = new StringBuilder();
        for (Map.Entry<String, Integer> entry : doItemCount.entrySet()) {
            if (summary.length() > 0) {
                summary.append(", ");
            }
            summary.append(entry.getKey()).append("(").append(entry.getValue()).append(")");
        }

        return summary.toString();
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
        TextView tvOrder;
        TextView tvCustomer;
        TextView tvAllocatedQty;
        TextView tvQtyBox;
    }

    @Override
    public Filter getFilter() {
        if (itemFilter == null) {
            itemFilter = new ReceivePalletAdapter.ItemFilter();
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
                                if (item.getPalletName() != null &&
                                        item.getPalletName().equals(selectedPallet)) {
                                    filteredList.add(item);
                                }
                            } else {
                                if ((item.getProductName() != null &&
                                        item.getProductName().toLowerCase().contains(filterPattern)) ||
                                        (item.getLotNumber() != null &&
                                                item.getLotNumber().toLowerCase().contains(filterPattern)) ||
                                        (item.getPalletName() != null &&
                                                item.getPalletName().toLowerCase().contains(filterPattern))) {
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
