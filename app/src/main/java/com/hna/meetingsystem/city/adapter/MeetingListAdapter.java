package com.hna.meetingsystem.city.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hna.meetingsystem.R;
import com.hna.meetingsystem.city.MeetingRoomListFragment;
import com.hna.meetingsystem.city.model.City;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jin on 2016/4/13.
 */
public class MeetingListAdapter extends BaseAdapter {
    public List<City> meetingLists;//会议室集合
    public Context context;
    public int white = Color.parseColor("#FFFFFF");
    public ListView lv;

    public FragmentActivity fa;
    public MeetingRoomListFragment fragment;

    public MeetingListAdapter(Context context, List<City> meetingLists, FragmentActivity fa, ListView lv, MeetingRoomListFragment fragment) {
        this.fa = fa;
        this.context = context;
        this.meetingLists = meetingLists;
        this.lv = lv;
        this.fragment = fragment;
    }


    public void setData(List<City> meetingLists) {//刷新数据
        this.meetingLists = meetingLists;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return meetingLists.size();
    }

    @Override
    public City getItem(int position) {
        return meetingLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.fragment_info_meeting_list_item_city, null);
            viewHolder = init(convertView); //初始化控件
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        City city = meetingLists.get(position);//选择的会议

        viewHolder.id.setText((position + 1) + ". ");//设置条目序号
        viewHolder.name.setText(city.name );//设置姓名


        City.Status status = City.Status.getStatus(city.status);//会议室状态

        viewHolder.tvMeetingIsFree.setText(status.getName());//设置状态
        viewHolder.tvMeetingIsFree.setCompoundDrawablesRelativeWithIntrinsicBounds(status.getIcon(context), null, null, null);//设置图标


        int count = city.capacity;
        viewHolder.tvMeetingStatus.setText((count < 8 ? "小" : (count < 13 ? "中" : "大")) + "型会议室--" + city.capacity + "人");//设置大小


        viewHolder.setDevices(city.devices.toCharArray());//设置设备
        return convertView;
    }

    private ViewHolder init(View view) {

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.id = (TextView) view.findViewById(R.id.id);
        viewHolder.name = (TextView) view.findViewById(R.id.tvMeetName);//会议名称
        viewHolder.tvMeetingStatus = (TextView) view.findViewById(R.id.tvMeetingStatus);//会议繁忙状态
        viewHolder.tvMeetingIsFree = (TextView) view.findViewById(R.id.tvMeetingIsFree);
        viewHolder.screen = (ImageView) view.findViewById(R.id.room_screen_list);//屏幕
        viewHolder.projecter = (ImageView) view.findViewById(R.id.room_projection_list);//摄象仪
        viewHolder.port = (ImageView) view.findViewById(R.id.room_port_list);//端口
        viewHolder.computer = (ImageView) view.findViewById(R.id.room_computer_list);//电脑

        viewHolder.devicesName = (TextView) view.findViewById(R.id.devices_label);
        viewHolder.initDevices();
        return viewHolder;
    }


    private final class ViewHolder {
        TextView id;
        TextView name;
        TextView tvMeetingStatus;//是否繁忙
        ImageView ivState;//没有用到?．．．
        TextView tvMeetingIsFree;

        TextView devicesName;//设备名称
        ImageView screen;
        ImageView projecter;
        ImageView port;
        ImageView computer;

        List<View> devices = new ArrayList<>();

        void initDevices() {
            devices.clear();

            devices.add(projecter);
            devices.add(screen);
            devices.add(port);
            devices.add(computer);
        }

        void setDevices(char[] ds) {

            devicesName.setText("设备：" + (ds.length == 0 ? " 无" : ""));
            for (int i = 0; i < devices.size(); i++) {
                devices.get(i).setVisibility(isContains(ds,i) ? View.VISIBLE : View.GONE);
            }
        }

//判断是否包含i代表的设备；

    }
    public static  boolean isContains(char[] arr,int i) {
        if(arr.length==0)
        {
            return false;
        }
        for (int a = 0;a<arr.length;a++)
        {
            if(String.valueOf(arr[a]).equals(String.valueOf(i)))
                return true;
        }
        return  false;
    }
}
