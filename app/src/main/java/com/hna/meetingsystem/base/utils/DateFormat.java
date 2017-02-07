package com.hna.meetingsystem.base.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Jie on 2016-04-15.
 */
//时间格式
public class DateFormat {
    private static class InstanceHolder {
        public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public static SimpleDateFormat getDateFormat() {
        return InstanceHolder.dateFormat;
    }
//年月日
    public static SimpleDateFormat formatNoTime = new SimpleDateFormat("yyyy.MM.dd");
//是否是今天
    public static boolean isToday(Date date) {
        Date today = new Date();
        return formatNoTime.format(date).equals(formatNoTime.format(today));
    }
//获取今天或者明天,后天的日期
    public static String getDayMark(Date date) {
        Calendar today = Calendar.getInstance();

        int year = today.get(Calendar.YEAR);
        int day = today.get(Calendar.DAY_OF_YEAR);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int dYear = calendar.get(Calendar.YEAR);
        int dDay = calendar.get(Calendar.DAY_OF_YEAR);

        if (year == dYear) {
            if (day == dDay) {
                return "今天";
            } else if (day == dDay - 1) {
                return "明天 " + formatNoTime.format(date);
            }else if(day == dDay -2) {
                return "后天 "+ formatNoTime.format(date);
            }
        }


        return formatNoTime.format(date);
    }

}
