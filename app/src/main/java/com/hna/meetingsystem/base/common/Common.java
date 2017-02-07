package com.hna.meetingsystem.base.common;

/**
 * Created by Jie on 2016-04-15.
 */
public class Common {
    //服务器ip，端口http://10.71.160.80:8808/api/v1/meetings　内网 http://10.71.86.222:  测试：10.71.162.33
    public static final String SOCKET = "http://10.71.86.222:8808";
    //订阅地址
    public static final String MEETING_SUBSCRIBE = SOCKET + "/api/v1/meetings";
    //查询地址
    public static final String MEETING_QUERY = SOCKET + "/api/v1/meetings/";
    //删除地址
    public static final String MEETING_DELETE = SOCKET + "/api/v1/meetings/";
    //查询城市信息地址
    public static final String CITY_QUERY = SOCKET + "/api/v1/meetings/city";
    public static final String MEETING_REJISTEDS = SOCKET + "/api/v1/meetings/index";
    //房号key
    public static final String KEY_ROOM_NUMBER = "roomNumber";
    //开始时间
    public static final String KEY_START_TIME = "startTime";
    //结束时间
    public static final String KEY_END_TIME = "endTime";
    //状态
    public static final String KEY_STATUS = "status";

    //城市
    public static final String CITY_FILE_NAME = "city";
//版本信息
    public static final String VERSION_INFO = "http://10.71.160.80:8808/apk/config.json";
//默认部门

    public static final String DEFAULT_USER = "海航生态科技";
    //查询更新频率
    public static final long check_update_frequency = 120;//分钟
}
