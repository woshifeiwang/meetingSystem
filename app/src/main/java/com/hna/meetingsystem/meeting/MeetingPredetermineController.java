package com.hna.meetingsystem.meeting;

import com.hna.meetingsystem.MyApplication;
import com.hna.meetingsystem.base.common.Common;
import com.hna.meetingsystem.city.model.City;
import com.hna.meetingsystem.model.MeetingInfo;

/**
 * Created by Jie on 2016-05-08.
 */
public class MeetingPredetermineController {//房间预定控制器,小工厂，把预定信息和房间信息结合起来，返回

    private static MeetingPredetermineController instance;

    public static MeetingPredetermineController getInstance() {
        if (instance == null)
            instance = new MeetingPredetermineController();

        return instance;
    }

    public MeetingInfo createCurrentRoomMeeting(MeetingPre meetingPre) {
        return createMeeting(MyApplication.getCityIndex(), meetingPre);
    }

    public MeetingInfo createMeeting(int id, MeetingPre meetingPre) {

        City city = MyApplication.getCityById(id);

        MeetingInfo info = new MeetingInfo();
//        info.department = meetingPre.department;
        info.startTime = meetingPre.startTime;
        info.endTime = meetingPre.endTime;
        info.roomNumber = city.roomNumber;
        info.roomName = city.name;
        info.subject = meetingPre.subject;
        info.userName = Common.DEFAULT_USER;
        info.personnel = meetingPre.personnel;
        return info;
    }

}