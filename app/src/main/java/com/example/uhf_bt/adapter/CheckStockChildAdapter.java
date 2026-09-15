package com.example.uhf_bt.adapter;
import android.annotation.SuppressLint;
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
import androidx.core.content.ContextCompat;

import com.example.uhf_bt.ColorConstants;
import com.example.uhf_bt.R;
import com.example.uhf_bt.models.CheckStockItem;

import java.util.ArrayList;
import java.util.List;

public class CheckStockChildAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<CheckStockItem> originalItems;
    private List<CheckStockItem> filteredItems;
    private LayoutInflater inflater;
    private ItemFilter itemFilter;

    public CheckStockChildAdapter(Context context, List<CheckStockItem> items) {
        this.context = context;
        this.originalItems = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>(items);
        this.inflater = LayoutInflater.from(context);
        getFilter();
    }

    public interface OnTagStatusClickListener {
        void onStatusClick(CheckStockItem item, String status, String boxType);
    }
    
    private OnTagStatusClickListener listener;

    public void setOnTagStatusClickListener(OnTagStatusClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Override
    public CheckStockItem getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("LongLogTag")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_check_stock_child, parent, false);
            holder = new ViewHolder();
            holder.tvItem = convertView.findViewById(R.id.tvItem);
            holder.tvProduct = convertView.findViewById(R.id.tvProduct);
            holder.tvLot = convertView.findViewById(R.id.tvLot);
            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
            holder.tvQuantitySuccess = convertView.findViewById(R.id.tvQuantitySuccess);
            holder.tvQuantityScan = convertView.findViewById(R.id.tvQuantityScan);
            holder.tvBarcode = convertView.findViewById(R.id.tvBarcode);
            holder.tvAllocatedQty = convertView.findViewById(R.id.tvAllocatedQty);
            holder.tvSTD = convertView.findViewById(R.id.tvSTD);
            holder.tvBoxType = convertView.findViewById(R.id.tvBoxType);
            holder.ivStatus = convertView.findViewById(R.id.ivStatus);

            holder.tvQuantityRenant = convertView.findViewById(R.id.tvQuantityRenant);
            holder.tvQuantitySuccessRenant = convertView.findViewById(R.id.tvQuantitySuccessRenant);
            holder.tvQuantityScanRenant = convertView.findViewById(R.id.tvQuantityScanRenant);

            holder.tvQuantityPcs = convertView.findViewById(R.id.tvQuantityPcs);
            holder.tvQuantitySuccessPcs = convertView.findViewById(R.id.tvQuantitySuccessPcs);
            holder.tvQuantityScanPcs = convertView.findViewById(R.id.tvQuantityScanPcs);

            holder.tvQuantityRenantPcs = convertView.findViewById(R.id.tvQuantityRenantPcs);
            holder.tvQuantitySuccessRenantPcs = convertView.findViewById(R.id.tvQuantitySuccessRenantPcs);
            holder.tvQuantityScanRenantPcs = convertView.findViewById(R.id.tvQuantityScanRenantPcs);

            holder.layoutFullBoxRow = convertView.findViewById(R.id.layoutFullBoxRow);
            holder.layoutRenantRow = convertView.findViewById(R.id.layoutRenantRow);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CheckStockItem item = getItem(position);

        // เพิ่ม debug logging
        Log.d("CheckStockChildTagAdapter", "Position: " + position +
                ", TagNumber: " + item.getTagNumber() +
                ", ProductId: " + item.getProductId());


        // ตรวจสอบว่า item.getTagNumber() ส่งค่าอะไร
        String tagNumber = item.getTagNumber();
        if (tagNumber == null || tagNumber.isEmpty()) {
            Log.w("CheckStockChildTagAdapter", "TagNumber is null or empty at position: " + position);
            tagNumber = "No Tag Number";
        }

        holder.tvItem.setText("Item : " + item.getProductNumber());
        holder.tvProduct.setText(item.getProduct());
        String stdStr = item.getStd() > 0 ? String.format("%.0f", item.getStd()) : "-";
        String boxTypeStr = item.getBoxType() != null && !item.getBoxType().isEmpty() ? item.getBoxType() : "-";
        holder.tvLot.setText("STD/BoxType: " + stdStr + " / " + boxTypeStr);
        holder.tvBarcode.setText(item.getBarcode());

        // **แสดงสถานะปริมาณและเปลี่ยนสีตามสถานะ**
        double totalScanned = item.getQuantitySuccess() + item.getQuantityScan();
        double maxQty = item.getQuantity();
        double remaining = maxQty - totalScanned;

        // แสดงปริมาณพร้อมสถานะ
        if (maxQty == 0) {
            holder.tvQuantity.setText(String.format("%.0f", maxQty));
            holder.tvQuantity.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            holder.tvQuantityScan.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
            holder.tvQuantitySuccess.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else {
            if (remaining <= 0) {
                // เต็มแล้ว - สีเขียว
                holder.tvQuantity.setText(String.format("%.0f", maxQty));
                holder.tvQuantity.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                holder.tvQuantityScan.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                holder.tvQuantitySuccess.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            } else if (remaining <= maxQty * 0.2) {
                // เหลือน้อย - สีส้ม (เอา (x left) ออกแล้ว)
                holder.tvQuantity.setText(String.format("%.0f", maxQty));
                holder.tvQuantity.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
                holder.tvQuantityScan.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
                holder.tvQuantitySuccess.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
            } else {
                // ปกติ - สีดำ
                holder.tvQuantity.setText(String.format("%.0f", maxQty));
                holder.tvQuantity.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                holder.tvQuantityScan.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
                holder.tvQuantitySuccess.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            }
        }

        // ตรวจสอบถ้าเกินปริมาณ
        if (totalScanned > maxQty) {
            holder.tvQuantity.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            holder.tvQuantityScan.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            holder.tvQuantity.setText(String.format("%.0f", maxQty));
        }

        holder.tvQuantityScan.setText(String.format("%.0f", item.getQuantityScan()));
        holder.tvQuantitySuccess.setText(String.format("%.0f", item.getQuantitySuccess()));
        
        holder.tvQuantityPcs.setText(String.format("%.0f", item.getQuantityPcs()));
        holder.tvQuantityScanPcs.setText(String.format("%.0f", item.getQuantityScanPcs()));
        holder.tvQuantitySuccessPcs.setText(String.format("%.0f", item.getQuantitySuccessPcs()));
        
        // --- Populate Renant Row ---
        holder.tvQuantityRenant.setText(String.format("%.0f", item.getQuantityRenant()));
        holder.tvQuantityScanRenant.setText(String.format("%.0f", item.getQuantityRenantScan())); 
        holder.tvQuantitySuccessRenant.setText(String.format("%.0f", item.getQuantityRenantSuccess())); 

        holder.tvQuantityRenantPcs.setText(String.format("%.0f", item.getQuantityRenantPcs()));
        holder.tvQuantityScanRenantPcs.setText(String.format("%.0f", item.getQuantityRenantScanPcs()));
        holder.tvQuantitySuccessRenantPcs.setText(String.format("%.0f", item.getQuantityRenantSuccessPcs())); 

        double sumBox = item.getQuantity() + item.getQuantityRenant();
        double sumBoxSuccess = item.getQuantitySuccess() + item.getQuantityRenantSuccess();
        double sumBoxScan = item.getQuantityScan() + item.getQuantityRenantScan();

        double sumPcs = item.getQuantityPcs() + item.getQuantityRenantPcs();
        double sumPcsSuccess = item.getQuantitySuccessPcs() + item.getQuantityRenantSuccessPcs();
        double sumPcsScan = item.getQuantityScanPcs() + item.getQuantityRenantScanPcs();

        String sumBoxHtml = String.format("<font color='#000000'>%.0f</font> / <font color='#4CAF50'>%.0f</font> / <font color='#FF9800'>%.0f</font>", sumBox, sumBoxSuccess, sumBoxScan);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.tvAllocatedQty.setText(android.text.Html.fromHtml(sumBoxHtml, android.text.Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.tvAllocatedQty.setText(android.text.Html.fromHtml(sumBoxHtml));
        }

        String sumPcsHtml = String.format("<font color='#000000'>%.0f</font> / <font color='#4CAF50'>%.0f</font> / <font color='#FF9800'>%.0f</font>", sumPcs, sumPcsSuccess, sumPcsScan);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.tvSTD.setText(android.text.Html.fromHtml(sumPcsHtml, android.text.Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.tvSTD.setText(android.text.Html.fromHtml(sumPcsHtml));
        }
        
        if(holder.tvBoxType != null) {
            holder.tvBoxType.setVisibility(View.GONE);
        }

        // Display Renant icon if ItemType is "Renant"
        if (item.getItemType() != null && item.getItemType().equals("Renant")) {
            holder.ivStatus.setVisibility(View.VISIBLE);
            holder.ivStatus.setImageResource(R.drawable.ic_renant);
        } else {
            holder.ivStatus.setVisibility(View.GONE);
        }

        // **ปรับปรุงการใช้สีพื้นหลังของทั้ง Item**
        int backgroundColor = ColorConstants.BG_DEFAULT;
        if (sumBox == 0 && sumPcs == 0) {
            if (totalScanned > 0 || (sumPcsSuccess + sumPcsScan) > 0) {
                backgroundColor = ColorConstants.BG_SCAN;
            }
        } else {
            boolean isAllComplete = (sumBoxSuccess + sumBoxScan >= sumBox) && 
                                    (sumPcsSuccess + sumPcsScan >= sumPcs);
            if (isAllComplete) {
                boolean hasScanData = (sumBoxScan > 0 || sumPcsScan > 0);
                backgroundColor = hasScanData ? ColorConstants.BG_SCAN : ColorConstants.BG_SUCCESS;
            } else if ((sumBoxSuccess + sumBoxScan > 0) || (sumPcsSuccess + sumPcsScan > 0)) {
                backgroundColor = ColorConstants.BG_PARTIAL; // สีเหลืองเมื่อสแกนบางส่วน
            }
        }
        convertView.setBackgroundColor(backgroundColor);

        // **เพิ่ม Highlight สีเขียวให้กับ Full Box ถ้าเช็คครบทั้ง Box และ Pcs**
        if (holder.layoutFullBoxRow != null) {
            boolean isFullBoxMaxZero = (item.getQuantity() == 0 && item.getQuantityPcs() == 0);
            boolean isFullBoxComplete = !isFullBoxMaxZero && 
                ((item.getQuantitySuccess() + item.getQuantityScan()) >= item.getQuantity()) && 
                ((item.getQuantitySuccessPcs() + item.getQuantityScanPcs()) >= item.getQuantityPcs());
            
            if (isFullBoxComplete) {
                boolean hasFullBoxScan = (item.getQuantityScan() > 0 || item.getQuantityScanPcs() > 0);
                holder.layoutFullBoxRow.setBackgroundColor(hasFullBoxScan ? ColorConstants.BG_SCAN : ColorConstants.BG_SUCCESS);
            } else {
                holder.layoutFullBoxRow.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }
        }

        // **เพิ่ม Highlight สีเขียวให้กับ Renant ถ้าเช็คครบทั้ง Box และ Pcs**
        if (holder.layoutRenantRow != null) {
            boolean isRenantMaxZero = (item.getQuantityRenant() == 0 && item.getQuantityRenantPcs() == 0);
            boolean isRenantComplete = !isRenantMaxZero && 
                ((item.getQuantityRenantSuccess() + item.getQuantityRenantScan()) >= item.getQuantityRenant()) && 
                ((item.getQuantityRenantSuccessPcs() + item.getQuantityRenantScanPcs()) >= item.getQuantityRenantPcs());
                
            if (isRenantComplete) {
                boolean hasRenantScan = (item.getQuantityRenantScan() > 0 || item.getQuantityRenantScanPcs() > 0);
                holder.layoutRenantRow.setBackgroundColor(hasRenantScan ? ColorConstants.BG_SCAN : ColorConstants.BG_SUCCESS);
            } else {
                holder.layoutRenantRow.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }
        }

        // **เพิ่ม Click Listener ให้กับตัวเลขสถานะต่างๆ**
        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                if (v == holder.tvQuantitySuccess) listener.onStatusClick(item, "SUCCESS", "Full Box");
                else if (v == holder.tvQuantityScan) listener.onStatusClick(item, "SCANNING", "Full Box");
                else if (v == holder.tvQuantitySuccessRenant) listener.onStatusClick(item, "SUCCESS", "Renant");
                else if (v == holder.tvQuantityScanRenant) listener.onStatusClick(item, "SCANNING", "Renant");
            }
        };

        holder.tvQuantitySuccess.setOnClickListener(clickListener);
        holder.tvQuantityScan.setOnClickListener(clickListener);
        holder.tvQuantitySuccessRenant.setOnClickListener(clickListener);
        holder.tvQuantityScanRenant.setOnClickListener(clickListener);

        // คลิกที่แถว Full Box ทั้งแถว -> โชว์ทุก tag Full Box (ไม่กรอง status)
        if (holder.layoutFullBoxRow != null) {
            holder.layoutFullBoxRow.setOnClickListener(v -> {
                if (listener != null) listener.onStatusClick(item, null, "Full Box");
            });
        }
        // คลิกที่แถว Renant ทั้งแถว -> โชว์ทุก tag Renant (ไม่กรอง status)
        if (holder.layoutRenantRow != null) {
            holder.layoutRenantRow.setOnClickListener(v -> {
                if (listener != null) listener.onStatusClick(item, null, "Renant");
            });
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvItem;
        TextView tvProduct;
        TextView tvLot;
        TextView tvQuantity;
        TextView tvQuantitySuccess;
        TextView tvQuantityScan;
        TextView tvBarcode;
        TextView tvAllocatedQty;
        TextView tvSTD;
        TextView tvBoxType;
        ImageView ivStatus;

        TextView tvQuantityRenant;
        TextView tvQuantitySuccessRenant;
        TextView tvQuantityScanRenant;

        TextView tvQuantityPcs;
        TextView tvQuantitySuccessPcs;
        TextView tvQuantityScanPcs;

        TextView tvQuantityRenantPcs;
        TextView tvQuantitySuccessRenantPcs;
        TextView tvQuantityScanRenantPcs;

        android.widget.LinearLayout layoutFullBoxRow;
        android.widget.LinearLayout layoutRenantRow;
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
                List<CheckStockItem> filteredList = new ArrayList<>();
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (CheckStockItem item : originalItems) {
                    if (item == null) continue;
                    
                    boolean matchProduct = item.getProduct() != null && item.getProduct().toLowerCase().contains(filterPattern);
                    boolean matchLotNumber = item.getLotNumber() != null && item.getLotNumber().toLowerCase().contains(filterPattern);
                    boolean matchLot = item.getLot() != null && item.getLot().toLowerCase().contains(filterPattern);
                    boolean matchProductNumber = item.getProductNumber() != null && item.getProductNumber().toLowerCase().contains(filterPattern);
                    boolean matchBarcode = item.getBarcode() != null && item.getBarcode().toLowerCase().contains(filterPattern);

                    if (matchProduct || matchLotNumber || matchLot || matchProductNumber || matchBarcode) {
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
            if (results != null && results.values != null) {
                filteredItems = (List<CheckStockItem>) results.values;
            } else {
                filteredItems = new ArrayList<>();
            }
            notifyDataSetChanged();
        }
    }

    public void updateItems(List<CheckStockItem> newItems) {
        this.originalItems = new ArrayList<>(newItems);
        this.filteredItems = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    public List<CheckStockItem> getOriginalItems() {
        return originalItems;
    }

    public List<CheckStockItem> getFilteredItems() {
        return filteredItems;
    }

    // **เพิ่มเมธอดใหม่สำหรับตรวจสอบสถานะรายการ**
    public boolean hasExceededItems() {
        for (CheckStockItem item : filteredItems) {
            double totalScanned = item.getQuantitySuccess() + item.getQuantityScan();
            if (totalScanned > item.getQuantity()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCompletedItems() {
        for (CheckStockItem item : filteredItems) {
            double totalScanned = item.getQuantitySuccess() + item.getQuantityScan();
            if (totalScanned >= item.getQuantity()) {
                return true;
            }
        }
        return false;
    }

    public int getCompletedItemsCount() {
        int count = 0;
        for (CheckStockItem item : filteredItems) {
            double totalScanned = item.getQuantitySuccess() + item.getQuantityScan();
            if (totalScanned >= item.getQuantity()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalItemsCount() {
        return filteredItems.size();
    }
}
