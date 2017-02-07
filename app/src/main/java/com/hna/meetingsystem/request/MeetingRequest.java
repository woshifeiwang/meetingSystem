package com.hna.meetingsystem.request;


import com.hna.meetingsystem.city.model.Version;
import com.hna.meetingsystem.model.MeetingInfo;
import com.hna.meetingsystem.model.MeetingInfoS;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import okhttp3.Callback;

/**
 * Created by Jie on 2016-04-13.
 */
public interface MeetingRequest {


    interface GetMeetingRoomCallBack {//会议室请求，对okhttp再封装
        void getSuccess(List<MeetingInfoS> registed);//多status，判断是否在使用
        void getFailed(String message);
    }
    interface GetMeetingRegistedCallBack{
        void getSuccess(HashSet<Integer> registeds);
        void getFailed(String message);
    }
    void registedsIndex(Map<String, String> condition,GetMeetingRegistedCallBack callback);
    interface AllCallBack {
        void call(List failed);
    }

    interface VersionCallBack {
        void call(Version version);
    }
    void getMeetingStart7day(String roomNumber,int selectId,GetMeetingRegistedCallBack callBack);

    void request(MeetingInfo info, Callback callback);

    void requestAll(List<MeetingInfo> meetings, AllCallBack callback);

    void queryMeetings(final Map<String, String> condition, final GetMeetingRoomCallBack callBack);

    void getMeetingByTime(String roomNumber, String startTime, String endTime, GetMeetingRoomCallBack callBack);

    void getMeetingStartToday(String roomNumber, GetMeetingRoomCallBack callBack);

    void getMeetingsByRoom(String roomNumber, final GetMeetingRoomCallBack callback);


    void getTodayMeetings(String roomNumber, GetMeetingRegistedCallBack callback);

    void delete(String id, Callback callback);

    void getVersion(VersionCallBack callBack);
}
