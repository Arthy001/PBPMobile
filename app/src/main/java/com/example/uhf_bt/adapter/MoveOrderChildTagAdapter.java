package com.example.uhf_bt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.uhf_bt.R;
import com.example.uhf_bt.models.MoveOrderItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MoveOrderChildTagAdapter extends BaseAdapter {
    private Context context;
    private List<MoveOrderItem> tags;
    private LayoutInflater inflater;

    public MoveOrderChildTagAdapter(Context context, List<MoveOrderItem> tags) {
        this.context = context;
        // ทำสำเนารายการแท็กเพื่อไม่ให้กระทบต่อข้อมูลต้นฉบับ
        this.tags = new ArrayList<>(tags);

        // เรียงลำดับแท็กตาม TagId
        Collections.sort(this.tags, new Comparator<MoveOrderItem>() {
            @Override
            public int compare(MoveOrderItem item1, MoveOrderItem item2) {
                // ตรวจสอบค่า null ก่อนเปรียบเทียบ
                if (item1.getTagNumber() == null && item2.getTagNumber() == null) {
                    return 0;
                } else if (item1.getTagNumber() == null) {
                    return -1;
                } else if (item2.getTagNumber() == null) {
                    return 1;
                }

                // เรียงตามตัวอักษร (เรียงจากน้อยไปมาก)
                return item1.getTagNumber().compareTo(item2.getTagNumber());

            }
        });

        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return tags.size();
    }

    @Override
    public MoveOrderItem getItem(int position) {
        return tags.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_tag_move_order_child, parent, false);
            holder = new ViewHolder();
            holder.ivTagIcon = convertView.findViewById(R.id.ivTagIcon);
            holder.tvTagId = convertView.findViewById(R.id.tvTagId);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MoveOrderItem item = getItem(position);

        // แสดงลำดับและ TagId
        holder.tvTagId.setText((position + 1) + ". " + item.getTagNumber());

        // เปลี่ยนสีไอคอนตามประเภทของแท็ก
        if (item.getItemType() != null && item.getItemType().equals("Renant")) {
            holder.ivTagIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            holder.ivTagIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_blue_dark));
        }

        return convertView;
    }



    private static class ViewHolder {
        ImageView ivTagIcon;
        TextView tvTagId;
    }
}
