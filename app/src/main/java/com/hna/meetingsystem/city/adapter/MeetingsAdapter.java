package com.hna.meetingsystem.city.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hna.meetingsystem.R;
import com.hna.meetingsystem.base.utils.DateFormat;
import com.hna.meetingsystem.model.MeetingInfoS;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Jie on 2016-05-09.
 */
public class MeetingsAdapter extends BaseAdapter {

    public interface DeleteCallBack {
        void delete(String id);
    }

    private List<MeetingInfoS> meetings = new ArrayList<>();
    Context context;
    DeleteCallBack deleteCallBack;//删除接口

    public MeetingsAdapter(List<MeetingInfoS> meetings, Context context) {
        if (meetings != null)
            this.meetings = meetings;

        this.context = context;
    }


    public void setData(List<MeetingInfoS> data) {//刷新数据
        if (data == null)
            return;

        this.meetings = data;
        notifyDataSetChanged();
    }

    public void setDeleteCallBack(DeleteCallBack deleteCallBack) {
        this.deleteCallBack = deleteCallBack;//设置删除监听器
    }

    @Override
    public int getCount() {
        return meetings.size();
    }

    @Override
    public Object getItem(int position) {
        return meetings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meetings_layout, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final MeetingInfoS ms = meetings.get(position);

        boolean isFinish = "1".equals(ms.status);
        Date now = new Date();


        String sTime = ms.startTime.substring(ms.startTime.indexOf(" ") + 1, ms.startTime.lastIndexOf(":"));
        String eTime = ms.endTime.substring(ms.endTime.indexOf(" ") + 1, ms.endTime.lastIndexOf(":"));

        holder.setData(isFinish, (position + 1) + "." + ms.userName + "·" + ms.personnel,
                sTime + "--" + eTime, (isFinish ? "已完成" : (getTime(ms.startTime).getTime() > now.getTime() ? "排队中" : "会议中")) + "--" + ms.subject);


        try {
            Date endDate = DateFormat.getDateFormat().parse(ms.endTime);
            holder.meetingDay.setText(DateFormat.getDayMark(endDate));
            holder.meetingDay.setTextColor(context.getResources().getColor(isFinish ? R.color.gary : R.color.mainBlue));
        } catch (ParseException e) {
            Log.e("error", "getView: " + e.toString());
        }

        //add delete action
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteCallBack == null)
                    return;
                deleteCallBack.delete(ms.id);//删除选项
            }
        });

        return convertView;
    }


    private Date getTime(String timeString) {//获取Date
        SimpleDateFormat simpleDateFormat = DateFormat.getDateFormat();
        Date date = null;
        try {
            date = simpleDateFormat.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date == null) {
            return new Date();
        }

        return date;
    }

    class ViewHolder {

        private View background;
        public TextView meetingName;
        public TextView meetingTime;
        public TextView meetingStatus;
        public TextView btnDelete;
        public TextView meetingDay;

        public ViewHolder(View parent) {

            background = parent.findViewById(R.id.item_bg);
            meetingName = (TextView) parent.findViewById(R.id.meeting_name);
            meetingTime = (TextView) parent.findViewById(R.id.meeting_time);
            meetingStatus = (TextView) parent.findViewById(R.id.meeting_status);
            btnDelete = (TextView) parent.findViewById(R.id.btn_delete);
            meetingDay = (TextView) parent.findViewById(R.id.tv_meeting_day);

        }

        public void setData(boolean finish, String name, String time, String status) {
            final int black = 0xff777777;
            final int white = context.getResources().getColor(R.color.white);
            final int mainBlur = context.getResources().getColor(R.color.mainBlue);
            if (finish) {
                background.setBackgroundResource(R.drawable.meeting_list_border_finish);
                meetingName.setTextColor(black);
                meetingTime.setTextColor(black);
                meetingStatus.setTextColor(black);
                btnDelete.setTextColor(black);
            } else {
                background.setBackgroundResource(R.drawable.meeting_list_border_queue);
                meetingName.setTextColor(white);
                meetingTime.setTextColor(white);
                meetingStatus.setTextColor(mainBlur);
                btnDelete.setTextColor(mainBlur);
            }
            meetingName.setText(name);
            meetingTime.setText(time);
            meetingStatus.setText(status);
        }
    }
}
