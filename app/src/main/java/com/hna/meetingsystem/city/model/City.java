package com.hna.meetingsystem.city.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.hna.meetingsystem.R;


/**
 * Created by Jie on 2016-05-07.
 */
public class City {
//枚举定义房间状态及颜色
    public static enum Status {
//空闲状态
        leisure(false) {
            @Override
            public String getName() {
                return "当前空闲";
            }

            @Override
            public Drawable getIcon(Context context) {
                return getDrawable(context, R.drawable.label_leisure);
            }
        },
    //正常状态
        /*normal(false) {
            @Override
            public String getName() {
                return "正常";
//                return "空闲";
            }
            @Override
            public Drawable getIcon(Context context) {
                return getDrawable(context, R.drawable.label_normal);
//                return getDrawable(context, R.drawable.label_leisure);

            }
        },*/
    //繁忙状态
        busy(true) {
            @Override
            public String getName() {
                return "当前占用";
//                return "空闲";
            }

            @Override
            public Drawable getIcon(Context context) {
                return getDrawable(context, R.drawable.label_busy);
//                return getDrawable(context, R.drawable.label_leisure);
            }
        };


        boolean states;

        Status(boolean b) {
            states = b;
        }

        public abstract String getName();

        public boolean getStates() {
            return states;
        }

        public abstract Drawable getIcon(Context context);

        private static Drawable getDrawable(Context context, int res) {
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(res);
            return imageView.getDrawable();//返回相应大小的图片
        }

        public static Status getStatus(boolean status) {
            Status status1 = leisure;
            if(status) {
                status1 = busy;
            }
            else {
                status1 = leisure;
            }
            return status1;
        }
    }

    public int id;
    public String roomNumber;//房号
    public String address;//
    public String devices;//设备
    public String name;//名字
    public int capacity;//大小
    public boolean status;//状态，0，代表空闲，1，代表正常，2，代表繁忙
    public boolean used;//当前是否在被占用
}
