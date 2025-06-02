package com.example.uhf_bt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.uhf_bt.R;
import com.example.uhf_bt.models.ScannedTagItem;

import org.w3c.dom.Text;

import java.util.List;

public class ScannedTagAdapter extends BaseAdapter {
    private Context context;
    private List<ScannedTagItem> tagItems;
    private LayoutInflater inflater;

    public ScannedTagAdapter(Context context, List<ScannedTagItem> tagItems) {
        this.context = context;
        this.tagItems = tagItems;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return tagItems.size();
    }

    @Override
    public ScannedTagItem getItem(int position) {
        return tagItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_scanned_tag, parent, false);
            holder = new ViewHolder();
            holder.tvTagId = convertView.findViewById(R.id.tvTagId);
            holder.tvLot = convertView.findViewById(R.id.tvLot);
            holder.tvLotNumber = convertView.findViewById(R.id.tvLotNumber);
            holder.tvPickQty = convertView.findViewById(R.id.tvPickQty);
            holder.tvBarcode = convertView.findViewById(R.id.tvBarcode);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ScannedTagItem item = getItem(position);
        holder.tvBarcode.setText("Barcode: " + item.getBarcode());
        holder.tvTagId.setText("Tag: " + item.getTagId());
        holder.tvLot.setText("Lot: " +item.getLot());
        holder.tvLotNumber.setText("Lot Number" + item.getLotNumber());
        holder.tvPickQty.setText(String.format("Pick: %.0f" , item.getPickQty()) );

        return convertView;
    }

    static class ViewHolder {
        TextView tvTagId;
        TextView tvLot;
        TextView tvLotNumber;
        TextView tvPickQty;
        TextView tvBarcode;
    }

    public void addItem(ScannedTagItem item) {
        tagItems.add(item);
        notifyDataSetChanged();
    }

    public List<ScannedTagItem> getItems() {
        return tagItems;
    }
}