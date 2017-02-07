package com.hna.meetingsystem.city.adapter;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.hna.meetingsystem.R;
import com.hna.meetingsystem.base.utils.DateUtils;
import java.util.List;

/**
 * Created by pactera on 2016/12/29.
 */

public class DateSpinnerAdapter extends BaseAdapter implements SpinnerAdapter{//日期选择Spinner的adapter
    private final List<String> sevendate;
    private final List<String> weeks;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public DateSpinnerAdapter() {
        sevendate = DateUtils.getSevendate();
        weeks = DateUtils.get7week();
    }

    @Override
    public int getCount() {
        return sevendate.size();
    }

    @Override
    public String getItem(int position) {
        return sevendate.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DateSpinnerAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_spinner_info, parent, false);
            holder = new DateSpinnerAdapter.ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (DateSpinnerAdapter.ViewHolder) convertView.getTag();
        }

        holder.setData(position);
        return convertView;
    }


    class ViewHolder {
        TextView date;
        TextView week;

        public ViewHolder(View parent) {
            date = (TextView) parent.findViewById(R.id.spinner_date);
            week = (TextView) parent.findViewById(R.id.spinner_date_week);
        }

        public void setData(int position) {
            date.setText(sevendate.get(position)+"  ");
            week.setText(weeks.get(position));
        }
    }
}
