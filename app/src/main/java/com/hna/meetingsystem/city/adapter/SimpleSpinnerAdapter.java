package com.hna.meetingsystem.city.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.hna.meetingsystem.R;

/**
 * Created by Jie on 2016-05-22.
 */
public class SimpleSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {//下拉条城市初始化选项

    String[] citys = {};

    public SimpleSpinnerAdapter(Context context) {
        citys = context.getResources().getStringArray(R.array.cityNames);
    }


    @Override
    public int getCount() {
        return citys.length;
    }

    @Override
    public Object getItem(int position) {
        return citys[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spinner_citys, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.setData(position);
        return convertView;
    }


    class ViewHolder {
        TextView cityName;
        TextView roomNumber;

        public ViewHolder(View parent) {
            cityName = (TextView) parent.findViewById(R.id.spinner_city_name);
            roomNumber = (TextView) parent.findViewById(R.id.spinner_room_number);
        }

        public void setData(int position) {
            cityName.setText(citys[position]);
            roomNumber.setText(10010 + position + "");
        }
    }
}
