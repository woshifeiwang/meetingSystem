package com.hna.meetingsystem.city.model;

import com.hna.meetingsystem.MyApplication;
import com.hna.meetingsystem.city.SelectCityFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/7.
 */
public class MeetingRoomData {//会议室房间信息类,3个list


    private static MeetingRoomData instance;

    public List<City> smallRooms = new ArrayList<>();
    public List<City> middleRooms = new ArrayList<>();
    public List<City> bigRooms = new ArrayList<>();
    public SelectCityFragment mSelectCityFragment;
    public static List<City> allRooms;
    public static MeetingRoomData getInstance() {//单例
        allRooms = MyApplication.getAllCityRooms() ;
        if (instance == null) {
            instance = new MeetingRoomData();
        }

        return instance;
    }

    private MeetingRoomData() {
//        ContainerFragment.selectCityFragment
       /* SelectCityFragment selectCityFragment = SelectCityFragment.getSelectCityFragment();
        selectCityFragment.setCityChangeListener(new SelectCityFragment.CityChangeListener() {
            @Override
            public void getCityData(List<City> cities) {
                allRooms = cities;
            }
        });*/

        for (City c :
                allRooms) {
            if (c.capacity < 8) {
                smallRooms.add(c);
            } else if (c.capacity < 13) {
                middleRooms.add(c);
            } else {
                bigRooms.add(c);
            }
        }
    }
}
