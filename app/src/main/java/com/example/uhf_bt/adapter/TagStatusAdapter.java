package com.example.uhf_bt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.uhf_bt.R;
import com.example.uhf_bt.models.TagStatusItem;

import java.util.List;

public class TagStatusAdapter extends ArrayAdapter<TagStatusItem> {

    public TagStatusAdapter(@NonNull Context context, @NonNull List<TagStatusItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tag_status, parent, false);
        }

        TagStatusItem item = getItem(position);
        if (item != null) {
            TextView tvStatusIcon = convertView.findViewById(R.id.tvStatusIcon);
            TextView tvTagNumber = convertView.findViewById(R.id.tvTagNumber);

            tvTagNumber.setText(item.getTagNumber());
            tvStatusIcon.setText(item.isReceived() ? "🟢" : "⚪");
        }

        return convertView;
    }
}
