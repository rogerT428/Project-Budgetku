package com.mobile.uph24si3.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mobile.uph24si3.R;
import com.mobile.uph24si3.model.Buah;

import java.util.List;

public class BuahAdapter extends ArrayAdapter<Buah> {

    private Context context;
    private List<Buah> buahList;
    private int lastPosition = -1;

    public BuahAdapter(Context context, List<Buah> buahList) {
        super(context, R.layout.item_buah, buahList);
        this.context = context;
        this.buahList = buahList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_buah, parent, false);
            holder = new ViewHolder();
            holder.tvEmoji = convertView.findViewById(R.id.tvBuahEmoji);
            holder.tvNama = convertView.findViewById(R.id.tvBuahNama);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Buah buah = buahList.get(position);
        holder.tvEmoji.setText(buah.getEmoji());
        holder.tvNama.setText(buah.getNama());

        // Slide-in animation for each new item
        if (position > lastPosition) {
            android.view.animation.Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_item);
            convertView.startAnimation(animation);
            lastPosition = position;
        }

        return convertView;
    }

    static class ViewHolder {
        TextView tvEmoji;
        TextView tvNama;
    }
}
