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
import com.example.uhf_bt.models.VehicleDto;

import java.util.ArrayList;
import java.util.List;

public class VehicleAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = "VehicleAdapter";
    private final Context context;
    private List<VehicleDto> vehicles;
    private List<VehicleDto> filteredVehicles;

    public VehicleAdapter(Context context) {
        this.context = context;
        this.vehicles = new ArrayList<>();
        this.filteredVehicles = new ArrayList<>();
    }

    public void setVehicles(List<VehicleDto> vehicles) {
        Log.d(TAG, "Setting Vehicles: " + (vehicles != null ? vehicles.size() : "null"));
        if (vehicles != null) {
            this.vehicles = vehicles;
            this.filteredVehicles = new ArrayList<>(vehicles);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return filteredVehicles.size();
    }

    @Override
    public VehicleDto getItem(int position) {
        return filteredVehicles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_vehicle, parent, false);
            holder = new ViewHolder();
            holder.tvLicensePlate = convertView.findViewById(R.id.tvLicensePlate);
            holder.tvDriver = convertView.findViewById(R.id.tvDriver);
            holder.tvVehicleBrand = convertView.findViewById(R.id.tvVehicleBrand);
            holder.tvVehicleType = convertView.findViewById(R.id.tvVehicleType);
            holder.tvStatus = convertView.findViewById(R.id.tvStatus);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        VehicleDto item = getItem(position);
        if (item != null) {
            try {
                // Set license plate
                holder.tvLicensePlate.setText("License: " + item.getLicensePlate());

                // Set driver
                holder.tvDriver.setText("Driver: " + (item.getDriver() != null ? item.getDriver() : "-"));

                // Set vehicle brand
                holder.tvVehicleBrand.setText("Brand: " + (item.getVehicleBrand() != null ? item.getVehicleBrand() : "-"));

                // Set vehicle type
                setVehicleType(holder.tvVehicleType, item.getVehicleType());

                // Set status with color
                setStatus(holder.tvStatus, item.getVehicleStatus());

            } catch (Exception e) {
                Log.e(TAG, "Error binding data to view at position " + position, e);
            }
        }

        return convertView;
    }

    private void setVehicleType(TextView typeText, int type) {
        String typeStr;

        switch (type) {
            case 1:
                typeStr = "Truck";
                break;
            case 2:
                typeStr = "Van";
                break;
            case 3:
                typeStr = "Pickup";
                break;
            case 4:
                typeStr = "Trailer";
                break;
            case 5:
                typeStr = "Container";
                break;
            case 6:
                typeStr = "10 Wheeler";
                break;
            default:
                typeStr = "Other";
        }

        typeText.setText("Type: " + typeStr);
    }

    private void setStatus(TextView statusText, int status) {
        String statusStr;
        int textColor;

        switch (status) {
            case 1:
                statusStr = "Ready";
                textColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case 11:
                statusStr = "Check In";
                textColor = context.getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case 12:
                statusStr = "Loading";
                textColor = context.getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case 13:
                statusStr = "กำลังจัดส่ง";
                textColor = context.getResources().getColor(android.R.color.holo_purple);
                break;
            case 14:
                statusStr = "จัดส่งสำเร็จ";
                textColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case -1:
                statusStr = "Cancelled";
                textColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            case 0:
                statusStr = "Inactive";
                textColor = context.getResources().getColor(android.R.color.darker_gray);
                break;
            default:
                statusStr = "Unknown (" + status + ")";
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
                if (vehicles == null || vehicles.isEmpty()) {
                    results.values = new ArrayList<>();
                    results.count = 0;
                    return results;
                }

                if (constraint == null || constraint.length() == 0) {
                    results.values = new ArrayList<>(vehicles);
                    results.count = vehicles.size();
                } else {
                    List<VehicleDto> filtered = new ArrayList<>();
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (VehicleDto vehicle : vehicles) {
                        if (vehicle != null && (
                                containsIgnoreCase(vehicle.getLicensePlate(), filterPattern) ||
                                        containsIgnoreCase(vehicle.getDriver(), filterPattern) ||
                                        containsIgnoreCase(vehicle.getVehicleBrand(), filterPattern))) {
                            filtered.add(vehicle);
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
                    filteredVehicles = (List<VehicleDto>) results.values;
                } else {
                    filteredVehicles = new ArrayList<>();
                }
                notifyDataSetChanged();
            }
        };
    }

    private boolean containsIgnoreCase(String source, String target) {
        return source != null && source.toLowerCase().contains(target);
    }

    private static class ViewHolder {
        TextView tvLicensePlate;
        TextView tvDriver;
        TextView tvVehicleBrand;
        TextView tvVehicleType;
        TextView tvStatus;
    }
}
