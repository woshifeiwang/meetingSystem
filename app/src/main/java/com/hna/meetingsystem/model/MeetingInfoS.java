package com.hna.meetingsystem.model;


/**
 * Created by Administrator on 2016/4/14.
 */
public class MeetingInfoS extends MeetingInfo{
    public String id;
    public String status;//状态,1是已完成，还有排队中，会议中
    //判断两个会议信息是否一致

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof MeetingInfoS)){
            return false;
        }
        MeetingInfoS that = (MeetingInfoS)o;
        if(!this.startTime.equals(that.startTime)){
            return false;
        }
        if(!this.userName.equals(that.userName)){
            return false;
        }
        if(!this.personnel.equals(that.personnel)){
            return false;
        }
        if(!this.description.equals(that.description)){
            return false;
        }
        if(!this.roomName.equals(that.roomName)){
            return false;
        }
        if(!this.endTime.equals(that.endTime)){
            return false;
        }
        if(!this.roomNumber.equals(that.roomNumber)){
            return false;
        }
        if(!this.subject.equals(that.subject)){
            return false;
        }
        if(!this.id.equals(that.id)){
            return false;
        }
        if(!this.status.equals(that.status)){
            return false;
        }
        return true;
    }
}
