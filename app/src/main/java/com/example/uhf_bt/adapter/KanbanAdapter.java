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
import com.example.uhf_bt.fragment.KanbanFragment;
import com.example.uhf_bt.models.Kanban;

import java.util.ArrayList;
import java.util.List;

public class KanbanAdapter  extends BaseAdapter implements Filterable {
    private static final String TAG = "KanbanAdapter";
    private final Context context;
    private List<Kanban> kanbans;
    private List<Kanban> filteredKanbans;

    public KanbanAdapter(Context context) {
        this.context = context;
        this.kanbans = new ArrayList<>();
        this.filteredKanbans = new ArrayList<>();
    }

    public void setKanbans(List<Kanban> kanbans) {
        Log.d(TAG, "Setting Kanban tags: " + (kanbans != null ? kanbans.size() : "null"));
        if (kanbans != null) {
            this.kanbans = kanbans;
            this.filteredKanbans = new ArrayList<>(kanbans);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return filteredKanbans.size();
    }

    @Override
    public Kanban getItem(int position) {
        return filteredKanbans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        KanbanAdapter.ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_kanban, parent, false);
            holder = new KanbanAdapter.ViewHolder();
            holder.tvKanbanNo = convertView.findViewById(R.id.tvKanbanNo);
            holder.tvKanbanName = convertView.findViewById(R.id.tvKanbanName);
            holder.tvKanbanTagId = convertView.findViewById(R.id.tvKanbanTagId);
            holder.tvStatus = convertView.findViewById(R.id.tvStatus);
            holder.tvNumber = convertView.findViewById(R.id.tvNumber);
            convertView.setTag(holder);
        } else {
            holder = (KanbanAdapter.ViewHolder) convertView.getTag();
        }

        Kanban item = getItem(position);
        if (item != null) {
            try {
                // Set picking order and customer
                holder.tvKanbanNo.setText("Kanban NO: " + item.getKanbanNo());
                holder.tvKanbanName.setText("Kanban Name: " + item.getKanbanName());                // Set tag ID
                holder.tvKanbanTagId.setText("Kaban Tag: "  + item.getKanbanTagId());
                String number = "-";
                if ( item.getNumber() != null) number    = item.getNumber();
                holder.tvNumber.setText("Number: "  + number);
                // Set status with color
                setStatus(holder.tvStatus, item.getKanbanStatus());

            } catch (Exception e) {
                Log.e(TAG, "Error binding data to view at position " + position, e);
            }
        }



        return convertView;
    }

    private void setStatus(TextView statusText, int status) {
        String statusStr;
        int textColor;

        switch (status) {
            case 0:
                statusStr = "All";
                textColor = context.getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case 1:
                statusStr = "Ready";
                textColor = context.getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case 2:
                statusStr =  "Preparing";
                textColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case 3:
                statusStr = "Success";
                textColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            case 4:
                statusStr = "Loading";
                textColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            case 5:
                statusStr = "Shipped";
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
                if (kanbans == null || kanbans.isEmpty()) {
                    results.values = new ArrayList<>();
                    results.count = 0;
                    return results;
                }

                if (constraint == null || constraint.length() == 0) {
                    results.values = new ArrayList<>(kanbans);
                    results.count = kanbans.size();
                } else {
                    List<Kanban> filtered = new ArrayList<>();
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (Kanban tag : kanbans) {
                        if (tag != null && (
                                containsIgnoreCase(tag.getKanbanNo(), filterPattern) ||
                                        containsIgnoreCase(tag.getKanbanName(), filterPattern) ||
                                        containsIgnoreCase(tag.getKanbanTagId(), filterPattern) ||
                                        containsIgnoreCase(tag.getNumber(), filterPattern))) {
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
                    filteredKanbans = (List<Kanban>) results.values;
                } else {
                    filteredKanbans = new ArrayList<>();
                }
                notifyDataSetChanged();
            }
        };
    }

    private boolean containsIgnoreCase(String source, String target) {
        return source != null && source.toLowerCase().contains(target);
    }

    private static class ViewHolder {
        TextView tvKanbanNo;
        TextView tvKanbanName;
        TextView tvKanbanTagId;
        TextView tvStatus;
        TextView tvNumber;
    }
}
