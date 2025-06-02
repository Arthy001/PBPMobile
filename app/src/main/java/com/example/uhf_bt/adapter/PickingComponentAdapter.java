package com.example.uhf_bt.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.uhf_bt.R;
import com.example.uhf_bt.models.PickingComponentItem;

import java.util.List;

public class PickingComponentAdapter extends BaseAdapter {
    private Context context;
    private List<PickingComponentItem> components;
    private LayoutInflater inflater;
    private ComponentScanListener listener;

    public interface ComponentScanListener {
        void onScanComponent(PickingComponentItem component, int position);
    }

    public PickingComponentAdapter(Context context, List<PickingComponentItem> components, ComponentScanListener listener) {
        this.context = context;
        this.components = components;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return components.size();
    }

    @Override
    public PickingComponentItem getItem(int position) {
        return components.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_picking_component, parent, false);
            holder = new ViewHolder();
            holder.tvComponentCode = convertView.findViewById(R.id.tvComponentCode);
            holder.tvQuantityRequired = convertView.findViewById(R.id.tvQuantityRequired);
            holder.tvQuantityPicked = convertView.findViewById(R.id.tvQuantityPicked);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PickingComponentItem component = getItem(position);

        // Set values
        holder.tvComponentCode.setText(component.getComponentCode());
        holder.tvQuantityRequired.setText(String.valueOf(component.getQuantityRequired()));
        holder.tvQuantityPicked.setText(String.valueOf(component.getQuantityPicked()));

        // Set item background color based on completion status
        if (component.isComplete()) {
            convertView.setBackgroundColor(Color.parseColor("#E8F5E9")); // Light green background
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView tvComponentCode;
        TextView tvQuantityRequired;
        TextView tvQuantityPicked;
    }

    public void updateComponent(int position) {
        notifyDataSetChanged();
    }
}