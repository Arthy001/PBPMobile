package com.example.uhf_bt.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.INotificationSideChannel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.uhf_bt.ColorConstants;
import com.example.uhf_bt.R;
import com.example.uhf_bt.models.PickingOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PickingOrderAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private List<PickingOrder> pickingOrders;
    private List<PickingOrder> originalPickingOrders;
    private PickingOrderFilter filter;
    private SimpleDateFormat dateFormat;

    public PickingOrderAdapter(Context context) {
        this.context = context;
        this.pickingOrders = new ArrayList<>();
        this.originalPickingOrders = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    public void setPickingOrders(List<PickingOrder> pickingOrders) {
        this.pickingOrders = pickingOrders;
        this.originalPickingOrders = new ArrayList<>(pickingOrders);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return pickingOrders.size();
    }

    @Override
    public PickingOrder getItem(int position) {
        return pickingOrders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_picking_order, parent, false);
        }

        PickingOrder order = pickingOrders.get(position);

        TextView tvOrderNumber = view.findViewById(R.id.tvOrderNumber);
        TextView tvCustomer = view.findViewById(R.id.tvCustomer);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvStatus = view.findViewById(R.id.tvStatus);
        TextView tvPickType = view.findViewById(R.id.tvPickType);

        if (order.getNumber() != null) {
            tvOrderNumber.setText("Pick Order: " + order.getNumber());
        } else {
            tvOrderNumber.setText("No Number");
        }

        if (order.getCustomer() != null) {
            tvCustomer.setText("ลูกค้า: " + order.getCustomer());
        } else {
            tvCustomer.setText("No Customer");
        }

        if (order.getOrderDate() != null) {
            tvDate.setText("วันที่: " + dateFormat.format(order.getOrderDate()));
        } else {
            tvDate.setText("No Date");
        }
        if (order.getPickingType() != null) {
            tvPickType.setText("ประเภทการจัด: " + order.getPickingType());
        } else {
            tvPickType.setText("-");
        }

        if (order.getPickingType() != null) {
            switch (order.getPickingType()) {
                case "CD ส่งขนส่งเอกชน":
                    tvPickType.setTextColor(Color.WHITE);
                    tvPickType.setBackgroundColor(Color.parseColor("#00BFFF"));
                    break;
                case "CD ส่งร้าน":
                    tvPickType.setTextColor(Color.WHITE);
                    tvPickType.setBackgroundColor(Color.parseColor("#4F4F4F"));
                    break;
                case "CD site งาน":
                    tvPickType.setTextColor(Color.WHITE);
                    tvPickType.setBackgroundColor(Color.parseColor("#4F4F4F"));
                    break;
                case "งานด่วน":
                    tvPickType.setTextColor(Color.WHITE);
                    tvPickType.setBackgroundColor(Color.parseColor("#FF0000"));
                    break;
                default:
                    tvPickType.setTextColor(Color.GRAY);
                    tvPickType.setBackgroundColor(Color.WHITE);
            }
        }
        else
        {
            tvPickType.setTextColor(Color.GRAY);
            tvPickType.setBackgroundColor(Color.WHITE);
        }

        // แสดงสถานะด้วยสีที่แตกต่างกัน
        if (order.getOrderStatus() != null) {
            switch (order.getOrderStatus()) {
                case -1: // Cancel
                    tvStatus.setText("Cancelled");
                    tvStatus.setTextColor(Color.RED);
                    break;
                case 0: // New
                    tvStatus.setText("New");
                    tvStatus.setTextColor(Color.BLUE);
                    break;
                case 1: // Packed
                    tvStatus.setText("Picked");
                    tvStatus.setTextColor(Color.rgb(255, 165, 0)); // สีส้ม
                    break;
                case 2: // Picked
                    tvStatus.setText("Packed");
                    tvStatus.setTextColor(Color.rgb(0, 128, 0)); // สีเขียวเข้ม
                    break;
                case 3: // ReceiveShipping
                    tvStatus.setText("Receive Shipping");
                    tvStatus.setTextColor(Color.rgb(128, 0, 128)); // สีม่วง
                    break;
                case 4: // Shipped
                    tvStatus.setText("Shipped");
                    tvStatus.setTextColor(Color.BLACK);
                    break;
                default:
                    tvStatus.setText("Unknown");
                    tvStatus.setTextColor(Color.GRAY);
                    break;
            }
        } else {
            tvStatus.setText("Unknown");
            tvStatus.setTextColor(Color.GRAY);
        }

        return view;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new PickingOrderFilter();
        }
        return filter;
    }

    private class PickingOrderFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                // ไม่มีการกรอง ส่งคืนรายการทั้งหมด
                results.values = originalPickingOrders;
                results.count = originalPickingOrders.size();
            } else {
                List<PickingOrder> filteredList = new ArrayList<>();
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (PickingOrder order : originalPickingOrders) {
                    if (order.getNumber() != null && order.getNumber().toLowerCase().contains(filterPattern) ||
                            order.getCustomer() != null && order.getCustomer().toLowerCase().contains(filterPattern)) {
                        filteredList.add(order);
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            pickingOrders = (List<PickingOrder>) results.values;
            notifyDataSetChanged();
        }
    }
}

//package com.example.uhf_bt.adapter;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.Filter;
//import android.widget.Filterable;
//import android.widget.TextView;
//
//import com.example.uhf_bt.models.PickingOrder;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.Filter;
//import android.widget.Filterable;
//import android.widget.TextView;
//
//import com.example.uhf_bt.R;
//import com.example.uhf_bt.models.PickingOrder;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class PickingOrderAdapter extends BaseAdapter implements Filterable {
//    private Context context;
//    private List<PickingOrder> pickingOrders;
//    private List<PickingOrder> filteredPickingOrders;
//    private Filter pickingOrderFilter;
//    private SimpleDateFormat inputFormat;
//    private SimpleDateFormat outputFormat;
//
//    public PickingOrderAdapter(Context context) {
//        this.context = context;
//        this.pickingOrders = new ArrayList<>();
//        this.filteredPickingOrders = new ArrayList<>();
//
//        // Initialize date formatters
//        this.inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
//        this.outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
//    }
//
//    public void setPickingOrders(List<PickingOrder> pickingOrders) {
//        this.pickingOrders = pickingOrders;
//        this.filteredPickingOrders = new ArrayList<>(pickingOrders);
//        notifyDataSetChanged();
//    }
//
//    @Override
//    public int getCount() {
//        return filteredPickingOrders.size();
//    }
//
//    @Override
//    public PickingOrder getItem(int position) {
//        return filteredPickingOrders.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder holder;
//
//        if (convertView == null) {
//            convertView = LayoutInflater.from(context).inflate(R.layout.item_picking_order, parent, false);
//            holder = new ViewHolder();
//            holder.numberText = convertView.findViewById(R.id.numberText);
//            holder.dateText = convertView.findViewById(R.id.dateText);
//            holder.customerText = convertView.findViewById(R.id.customerText);
//            holder.statusText = convertView.findViewById(R.id.statusText);
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        PickingOrder pickingOrder = getItem(position);
//
//        // Format date
//        String formattedDate = formatDate(pickingOrder.getOrderDate());
//
//        // Set values
//        holder.numberText.setText("Order: " + pickingOrder.getNumber());
//        holder.dateText.setText("Date: " + formattedDate);
//        holder.customerText.setText("Customer: " + pickingOrder.getCustomer());
//
//
//        // Set status with color
//        setStatus(holder.statusText, pickingOrder.getOrderStatus());
//
//        return convertView;
//    }
//
//    private String formatDate(String dateString) {
//        try {
//            Date date = inputFormat.parse(dateString);
//            return outputFormat.format(date);
//        } catch (ParseException e) {
//            return dateString;
//        }
//    }
//
//    private void setStatus(TextView statusText, int status) {
//        String statusStr;
//        int textColor;
//
//        switch (status) {
//            case 0:
//                statusStr = "New";
//                textColor = context.getResources().getColor(android.R.color.holo_blue_dark);
//                break;
//            case 1:
//                statusStr = "Packed";
//                textColor = context.getResources().getColor(android.R.color.holo_orange_dark);
//                break;
//            case 2:
//                statusStr = "Picked";
//                textColor = context.getResources().getColor(android.R.color.holo_green_dark);
//                break;
//            case 3:
//                statusStr = "รับเข้าจัดส่ง";
//                textColor = context.getResources().getColor(android.R.color.holo_red_dark);
//                break;
//            case 4:
//                statusStr = "จัดส่งแล้ว";
//                textColor = context.getResources().getColor(android.R.color.holo_red_dark);
//                break;
//            case -1:
//                statusStr = "Cancel";
//                textColor = context.getResources().getColor(android.R.color.holo_red_dark);
//                break;
//            default:
//                statusStr = "Unknown";
//                textColor = context.getResources().getColor(android.R.color.darker_gray);
//        }
//
//        statusText.setText(statusStr);
//        statusText.setTextColor(textColor);
//    }
//
//    @Override
//    public Filter getFilter() {
//        if (pickingOrderFilter == null) {
//            pickingOrderFilter = new Filter() {
//                @Override
//                protected FilterResults performFiltering(CharSequence constraint) {
//                    FilterResults results = new FilterResults();
//
//                    if (constraint == null || constraint.length() == 0) {
//                        results.values = new ArrayList<>(pickingOrders);
//                        results.count = pickingOrders.size();
//                    } else {
//                        String filterPattern = constraint.toString().toLowerCase().trim();
//                        List<PickingOrder> filteredList = new ArrayList<>();
//
//                        for (PickingOrder order : pickingOrders) {
//                            if (order.getNumber().toLowerCase().contains(filterPattern) ||
//                                    (order.getCustomer() != null &&
//                                            order.getCustomer().toLowerCase().contains(filterPattern)) ||
//                                    (order.getWarehouseSource() != null &&
//                                            order.getWarehouseSource().toLowerCase().contains(filterPattern)) ||
//                                    (order.getWarehouseDestination() != null &&
//                                            order.getWarehouseDestination().toLowerCase().contains(filterPattern))) {
//                                filteredList.add(order);
//                            }
//                        }
//
//                        results.values = filteredList;
//                        results.count = filteredList.size();
//                    }
//
//                    return results;
//                }
//
//                @SuppressWarnings("unchecked")
//                @Override
//                protected void publishResults(CharSequence constraint, FilterResults results) {
//                    filteredPickingOrders = (List<PickingOrder>) results.values;
//                    notifyDataSetChanged();
//                }
//            };
//        }
//        return pickingOrderFilter;
//    }
//
//    private static class ViewHolder {
//        TextView numberText;
//        TextView dateText;
//        TextView customerText;
//        TextView statusText;
//    }
//
//    public List<PickingOrder> getOriginalPickingOrders() {
//        return pickingOrders;
//    }
//
//    public List<PickingOrder> getFilteredPickingOrders() {
//        return filteredPickingOrders;
//    }
//}
