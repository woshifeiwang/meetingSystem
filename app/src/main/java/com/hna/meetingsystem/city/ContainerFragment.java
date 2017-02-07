package com.hna.meetingsystem.city;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hna.meetingsystem.MainActivity;
import com.hna.meetingsystem.MyApplication;
import com.hna.meetingsystem.R;
import com.hna.meetingsystem.base.BaseFragment;
import com.hna.meetingsystem.city.model.City;

/**
 * Created by Administrator on 2016/2/15.
 */
public class ContainerFragment extends BaseFragment {//提取出来的共用Fragment基类，可相互跳转

    public interface CurrentCityNameViewListener{
        void ObitainCityName(String cityName);
    }
    public static CurrentCityNameViewListener currentCityNameViewListener;//监听当前会议室名称，显示到自定义的headerLayout中。
    public static void setCurrentCityNameViewListener(CurrentCityNameViewListener listener)
    {
        currentCityNameViewListener = listener;
    }
    public final static String TAG = "BaseFragment";
    private FragmentManager fm;
    private FragmentTransaction ft;

    public static final int SELECT_FRAGMENT = 0;//平面图界面
    public static final int MEETING_ROOMS_FRAGMENT = 1;//所有会议室的界面
    public static final int SCHEDULE_FTAGMENT = 2;//详情预定页面
    public static final int MEETING_FTAGMENT = 3;//选中会议室会议列表界面

    public  MeetingRoomListFragment meetingRoomListFragment = null;
    public  SelectCityFragment selectCityFragment = null;
    public  ScheduleDetailFragment scheduleDetailFragment = null;
    public  com.hna.meetingsystem.city.MeetingListFragment meetingListFragment = null;
    public View view;

    Fragment currentF = null;//当前Fragment


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        meetingRoomListFragment = new MeetingRoomListFragment();
        selectCityFragment = new SelectCityFragment();
        scheduleDetailFragment = new ScheduleDetailFragment();
        meetingListFragment = new com.hna.meetingsystem.city.MeetingListFragment();
       /* meetingListFragment.emptyClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleDetailFragment.currentId = meetingListFragment.nowCityId;
                initView(SCHEDULE_FTAGMENT);
            }
        };
        meetingListFragment.backListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initView(SELECT_FRAGMENT);
            }
        };*/

        fm = getChildFragmentManager();
        ft = fm.beginTransaction();
        if(savedInstanceState==null) {
            ft.add(R.id.content, meetingRoomListFragment, MeetingRoomListFragment.TAG);
            ft.add(R.id.content, selectCityFragment, SelectCityFragment.TAG);
            ft.add(R.id.content, scheduleDetailFragment, SelectCityFragment.TAG);
            ft.add(R.id.content, meetingListFragment, com.hna.meetingsystem.city.MeetingListFragment.TAG);
            ft.hide(meetingRoomListFragment);
            ft.hide(scheduleDetailFragment);
            ft.hide(meetingListFragment);//隐藏其它的Fragment,默认显示平面图页面
            ft.commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_container, null);
        }

        MainActivity activity = (MainActivity) getActivity();
        initView(activity.signalTab3);
        return view;
    }


    public void initView(int i) {
        ft = fm.beginTransaction();
        switch (i) {
            case MEETING_FTAGMENT:
                showFragment(meetingListFragment);
                break;
            case MEETING_ROOMS_FRAGMENT:
                showFragment(meetingRoomListFragment);
                break;
            case SCHEDULE_FTAGMENT:
                showFragment(scheduleDetailFragment);
                break;
            default:
                showFragment(selectCityFragment);
//                showFragment(meetingListFragment);

        }
    }

    private void hideFragment(Fragment... f) {
        ft = fm.beginTransaction();
        for (int i = 0; i < f.length; i++) {
            if (f[i] != null && f[i].isVisible()) {
                ft.hide(f[i]);
            }
        }
        ft.commit();
    }


    private void showFragment(Fragment show) {

        if (show == null | show == currentF)
            return;

        ft = fm.beginTransaction();
        if (currentF != null)
            ft.hide(currentF);
        if(show==selectCityFragment)
        {
            if(currentCityNameViewListener!=null)
            {
                City currentCity = MyApplication.getCurrentCity();
                currentCityNameViewListener.ObitainCityName(currentCity.name);
                /*SelectCityFragment mSelectCityFragment = (SelectCityFragment) fm.findFragmentByTag(com.hna.meetingsystem.city.SelectCityFragment.TAG);
                View selectCityDiaolog = mSelectCityFragment.getSelectCityDiaolog();
                if (selectCityDiaolog!=null) {
                    selectCityDiaolog.setVisibility(View.GONE);
                }*/
            }
        }
        ft.show(show);

        ft.commit();

        currentF = show;
    }

    @Override
    public boolean onBackPress() {//可以隐藏全部地图查找这个功能.
        super.onBackPress();
        if (meetingRoomListFragment.isVisible()) {
            ft = fm.beginTransaction();
            ft.hide(meetingRoomListFragment);

            ft.commit();
            return true;
        }
        return false;
    }

}
