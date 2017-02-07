package com.hna.meetingsystem.city;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.hna.meetingsystem.view.HeaderLayout;
import com.hna.meetingsystem.MyApplication;
import com.hna.meetingsystem.R;
import com.hna.meetingsystem.city.adapter.MeetingListAdapter;
import com.hna.meetingsystem.city.model.City;
import com.hna.meetingsystem.city.model.MeetingRoomData;

import java.util.List;

/**
 * Created by Jin on 2016/4/13.
 */
public class MeetingRoomListFragment extends Fragment {//全部室列表界面
    public final static String TAG = "MeetingRoomListFragmentCity";
    private ListView lvMeeting;
    private MeetingListAdapter meetingListAdapter;
    private View view;
    private RadioGroup radioGroup;
    private ContainerFragment parentFragment;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentFragment = (ContainerFragment) getParentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_info_metting_list_city, container, false);

        lvMeeting = (ListView) view.findViewById(R.id.lvMeetingList);
        radioGroup = (RadioGroup) view.findViewById(R.id.rgGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {//根据选中情况分别列表
                switch (checkedId) {
                    case R.id.rbSmall: {
                        updateList(MeetingRoomData.getInstance().smallRooms);
                        break;
                    }
                    case R.id.rbMid: {
                        updateList(MeetingRoomData.getInstance().middleRooms);
                        break;
                    }
                    case R.id.rbBig: {
                        updateList(MeetingRoomData.getInstance().bigRooms);
                        break;
                    }
                    default:
                        updateList(MyApplication.getAllCityRooms());
                }
            }
        });

        RadioButton radioButton = (RadioButton) view.findViewById(R.id.rbSmall);
        radioButton.setChecked(true);


        HeaderLayout headerLayout = (HeaderLayout) view.findViewById(R.id.head);
        headerLayout.rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFragment.initView(ContainerFragment.SELECT_FRAGMENT);//全部列表界面返回地图界面
            }
        });
        lvMeeting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScheduleDetailFragment.currentId = meetingListAdapter.getItem(position).id;
                parentFragment.initView(ContainerFragment.SCHEDULE_FTAGMENT);//预定页面，选中的房间就是变成当前房间
            }
        });
        return view;
    }


    private void updateList(List<City> list) {
        if ((meetingListAdapter = (MeetingListAdapter) lvMeeting.getAdapter()) == null) {
            meetingListAdapter = new MeetingListAdapter(getContext(), list, getActivity(), lvMeeting, MeetingRoomListFragment.this);
            lvMeeting.setAdapter(meetingListAdapter);
        }
        meetingListAdapter.setData(list);

    }


    @Override
    public void onHiddenChanged(boolean hidden) {//Fragment影藏或者出现，更新数据
        super.onHiddenChanged(hidden);
        if (meetingListAdapter != null) {
            meetingListAdapter.notifyDataSetChanged();
        }
    }
}
