
package com.example.uhf_bt.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.uhf_bt.R;
import com.example.uhf_bt.models.PalletTag;

import java.util.ArrayList;
import java.util.List;

public class PalletTagAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = "PalletTagAdapter";
    private final Context context;
    private List<PalletTag> palletTags;
    private List<PalletTag> filteredPalletTags;

    public PalletTagAdapter(Context context) {
        this.context = context;
        this.palletTags = new ArrayList<>();
        this.filteredPalletTags = new ArrayList<>();
    }

    public void setPalletTags(List<PalletTag> palletTags) {
        Log.d(TAG, "Setting pallet tags: " + (palletTags != null ? palletTags.size() : "null"));
        if (palletTags != null) {
            this.palletTags = palletTags;
            this.filteredPalletTags = new ArrayList<>(palletTags);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return filteredPalletTags.size();
    }

    @Override
    public PalletTag getItem(int position) {
        return filteredPalletTags.get(position);
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
            holder.tvCustomer = convertView.findViewById(R.id.tvCustomer);
            holder.tvProduct = convertView.findViewById(R.id.tvProduct);
            holder.tvProductNumber = convertView.findViewById(R.id.tvProductNumber);
            holder.tvPalletInfo = convertView.findViewById(R.id.tvPalletInfo);
            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
            holder.tvPalletTagId = convertView.findViewById(R.id.tvPalletTagId);
            holder.tvStatus = convertView.findViewById(R.id.tvStatus);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PalletTag item = getItem(position);
        if (item != null) {
            try {
                // Set picking order and customer
                holder.tvPickingOrder.setText("DO: " + item.getPickingOrder());
                holder.tvCustomer.setText("ลูกค้า: " + item.getCustomer());

                // Set product details
                holder.tvProduct.setText(item.getProduct());
                holder.tvProductNumber.setText("Item: " + item.getProductNumber());

                // Set pallet info and quantity
                String palletInfo = String.format("Pallet: %s (%s)",
                        item.getPallet(), item.getPalletNo());
                holder.tvPalletInfo.setText(palletInfo);
                holder.tvQuantity.setText(String.format("QTY: %.2f", item.getQuantity()));

                // Set tag ID
                holder.tvPalletTagId.setText("Pallet Tag: "  + item.getPalletTagId());

            } catch (Exception e) {
                Log.e(TAG, "Error binding data to view at position " + position, e);
            }
        }

        // Set status with color
        setStatus(holder.tvStatus, PalletTag.getPalletStatus());

        return convertView;
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
                statusStr = "Picked";
                textColor = context.getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case 2:
                statusStr = "Packed";
                textColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case 3:
                statusStr = "รับเข้าจัดส่ง";
                textColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            case 4:
                statusStr = "โหลดสินค้า";
                textColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            case 5:
                statusStr = "จัดส่งแล้ว";
                textColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            case -1:
                statusStr = "Cancel";
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
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                // ตรวจสอบว่ามีข้อมูลต้นฉบับหรือไม่
                if (palletTags == null || palletTags.isEmpty()) {
                    results.values = new ArrayList<>();
                    results.count = 0;
                    return results;
                }

                if (constraint == null || constraint.length() == 0) {
                    results.values = new ArrayList<>(palletTags);
                    results.count = palletTags.size();
                } else {
                    List<PalletTag> filtered = new ArrayList<>();
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (PalletTag tag : palletTags) {
                        if (tag != null && (
                                containsIgnoreCase(tag.getPickingOrder(), filterPattern) ||
                                        containsIgnoreCase(tag.getPalletTagId(), filterPattern) ||
                                        containsIgnoreCase(tag.getProduct(), filterPattern) ||
                                        containsIgnoreCase(tag.getProductNumber(), filterPattern) ||
                                        containsIgnoreCase(tag.getPalletNo(), filterPattern) ||
                                        containsIgnoreCase(tag.getCustomer(), filterPattern))) {
                            filtered.add(tag);
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
                    filteredPalletTags = (List<PalletTag>) results.values;
                } else {
                    filteredPalletTags = new ArrayList<>();
                }
                notifyDataSetChanged();
            }
        };
    }

//    @Override
//    public Filter getFilter() {
//        return new Filter() {
//            @Override
//            protected FilterResults performFiltering(CharSequence constraint) {
//                FilterResults results = new FilterResults();
//
//                if (constraint == null || constraint.length() == 0) {
//                    results.values = palletTags;
//                    results.count = palletTags.size();
//                } else {
//                    List<PalletTag> filtered = new ArrayList<>();
//                    String filterPattern = constraint.toString().toLowerCase().trim();
//
//                    for (PalletTag tag : palletTags) {
//                        if (tag != null && (
//                                containsIgnoreCase(tag.getPickingOrder(), filterPattern) ||
//                                        containsIgnoreCase(tag.getPalletTagId(), filterPattern) ||
//                                        containsIgnoreCase(tag.getProduct(), filterPattern) ||
//                                        containsIgnoreCase(tag.getProductNumber(), filterPattern) ||
//                                        containsIgnoreCase(tag.getPalletNo(), filterPattern) ||
//                                        containsIgnoreCase(tag.getCustomer(), filterPattern))) {
//                            filtered.add(tag);
//                        }
//                    }
//
//                    results.values = filtered;
//                    results.count = filtered.size();
//                }
//                return results;
//            }
//
//            @Override
//            protected void publishResults(CharSequence constraint, FilterResults results) {
//                filteredPalletTags = (List<PalletTag>) results.values;
//                notifyDataSetChanged();
//            }
//        };
//    }

    private boolean containsIgnoreCase(String source, String target) {
        return source != null && source.toLowerCase().contains(target);
    }

    private static class ViewHolder {
        TextView tvPickingOrder;
        TextView tvCustomer;
        TextView tvProduct;
        TextView tvProductNumber;
        TextView tvPalletInfo;
        TextView tvQuantity;
        TextView tvPalletTagId;
        TextView tvStatus;
    }
}