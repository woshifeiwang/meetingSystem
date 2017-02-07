package com.hna.meetingsystem.meeting;

/**
 * Created by Jie on 2016-05-08.
 */
public class MeetingPre {//非常规会议信息类

//    public String atendees;
    public String startTime;
    public String endTime;
    public String subject;
    public String personnel;
//    public String department;
    /*public MeetingPre(String atendees, String startTime, String endTime, String subject) {
//        this.atendees = atendees;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subject = subject;
    }*/

    public MeetingPre(String startTime, String endTime, String subject,String personnel) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.subject = subject;
        this.personnel = personnel;
    }
}
