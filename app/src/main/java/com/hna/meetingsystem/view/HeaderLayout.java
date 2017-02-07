package com.hna.meetingsystem.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hna.meetingsystem.HomeFragment;
import com.hna.meetingsystem.MainActivity;
import com.hna.meetingsystem.MyApplication;
import com.hna.meetingsystem.R;
import com.hna.meetingsystem.base.utils.DisplayUtil;
import com.hna.meetingsystem.city.ContainerFragment;
import com.hna.meetingsystem.city.SelectCityFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2016/4/13.
 */
//自定义顶部linearlayout布局，因为是共同的，提取出来．根据右部button的类型设置不同状态的button样式.用MainActivity的setTimeListner添加时间到显示时间的
// TimeLayout布局

public class HeaderLayout extends RelativeLayout {
    //时间布局
    private TimeLayout time;
    //右边按钮
    private static final int ID_BTN2 = 2;
    public ImageView rightButton;
    //按钮类型
    private String rightButtonType;
    private TextView tvCityName;


    public HeaderLayout(Context context) {
        super(context);
        init(null);
    }

    public HeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public HeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void setCurrentCityName(String cityName)
    {
        tvCityName.setText(cityName);
    }
    public void init(AttributeSet attrs) {

        if (attrs != null) {
            //自定义属性
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CustomHeaderView, 0, 0);
            int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case R.styleable.CustomHeaderView_right:
                        rightButtonType = a.getString(attr);
                        break;
                }
            }
            //存贮，方便复用
            a.recycle();
        } else {
            rightButtonType = "null";
        }


        time = new TimeLayout(getContext());
        //设置time布局的布局控件
        LayoutParams layoutParams1 =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams1.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams1.setMargins(0, 0, 20, 0);
        time.setLayoutParams(layoutParams1);
        addView(time);
       /* CityLayout cityLayout = new CityLayout(getContext());
        LayoutParams layoutParams3 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams3.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams1.setMargins(0, 35, 20, 25);
        addView(cityLayout);*/

        //三种不同种类的右边点击按钮

        switch (rightButtonType) {
            case  "city":
                tvCityName = new TextView(getContext());
                tvCityName.setTextColor(Color.WHITE);
                tvCityName.setTextSize(TypedValue.COMPLEX_UNIT_DIP,48);
                tvCityName.setId(ID_BTN2);
                LayoutParams layoutParam2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutParam2.addRule(RelativeLayout.CENTER_IN_PARENT);
                layoutParam2.setMargins(20,65,0,0);
                layoutParam2.width = 250;
                layoutParam2.height = 250;
                tvCityName.setLayoutParams(layoutParam2);
                addView(tvCityName);

                tvCityName.setLayoutParams(layoutParam2);
                TextView tvpreName = new TextView(getContext());
                tvpreName.setText("会议室：");
                tvpreName.setSingleLine();
                tvpreName.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
                LayoutParams layoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutParam.setMargins(0,65,5,0);
                layoutParam.addRule(RelativeLayout.LEFT_OF,ID_BTN2);
                layoutParam.addRule(RelativeLayout.ALIGN_BASELINE,ID_BTN2);
                layoutParam.width = 125;
                layoutParam.height = 80;
                tvpreName.setTextColor(Color.parseColor("#c2c2c2"));
                tvpreName.setLayoutParams(layoutParam);
                addView(tvpreName);




                MyApplication.setCurrentCityNameViewListener(new MyApplication.CurrentCityNameViewListener() {
                    @Override
                    public void ObitainCityName(String cityName) {
                        tvCityName.setText(cityName);
                       tvCityName.setSingleLine(true);
                         tvCityName.postInvalidate();
                    }
                });
                HomeFragment.setCurrentCityNameViewListener(new HomeFragment.CurrentCityNameViewListener() {
                    @Override
                    public void ObitainCityName(String cityName) {
                        tvCityName.setText(cityName);
                    }
                });
                SelectCityFragment.setCurrentCityNameViewListener(new SelectCityFragment.CurrentCityNameViewListener() {
                    @Override
                    public void ObitainCityName(String cityName) {
                        tvCityName.setText(cityName);
                    }
                });
                MainActivity.setCurrentCityNameViewListener(new MainActivity.CurrentCityNameViewListener() {
                    @Override
                    public void ObitainCityName(String cityName) {
                        tvCityName.setText(cityName);
                    }
                });
                ContainerFragment.setCurrentCityNameViewListener(new ContainerFragment.CurrentCityNameViewListener() {
                    @Override
                    public void ObitainCityName(String cityName) {
                        tvCityName.setText(cityName);
                    }
                });

                break;
            case "null":
                break;
            case "back"://返回按钮
                rightButton = new ImageView(getContext());
                rightButton.setImageResource(R.drawable.icon_back);
                LayoutParams layoutParams =
                        new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                layoutParams.setMargins(0, 35, 20, 0);
                layoutParams.width = 120;
                layoutParams.height = 70;
                rightButton.setLayoutParams(layoutParams);
                addView(rightButton);
                break;
            case "schedule": //详情按钮
                 {
                rightButton = new ImageView(getContext());
                rightButton.setImageResource(R.drawable.icon_schedule_detail);//换预定详情图片的地方
                LayoutParams layoutParams2 = null;
                     //判断是不是pad
                if (DisplayUtil.isPad(getContext())) {
                    layoutParams2 =
                            new LayoutParams(DisplayUtil.dip2px(getContext(), 170), DisplayUtil.dip2px(getContext(), 100));
                    layoutParams2.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    layoutParams2.setMargins(0, 20, 20, 0);

                } else {
                    layoutParams2 =
                            new LayoutParams(DisplayUtil.dip2px(getContext(), 130), DisplayUtil.dip2px(getContext(), 70));
                    layoutParams2.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    layoutParams2.setMargins(0, 10, 20, 0);
                }

                rightButton.setLayoutParams(layoutParams2);
                addView(rightButton);
                break;
            }
            default:
                break;
        }


    }

    public void setCityText(String cityText) {
        tvCityName.setText(cityText);
    }

    public class TimeLayout extends RelativeLayout {

        private TextView time;//时间
        private TextView week;//星期
        private TextView year;//年
        MainActivity.TimeListener timeListener;

        private int showYear, showMonth, showDay, dayOfWeek;

        SimpleDateFormat timeF = new SimpleDateFormat("HH:mm");//十： 分
        SimpleDateFormat dateF = new SimpleDateFormat("yyyy.MM.dd");//年月日

        public TimeLayout(Context context) {
            super(context);
            init();
        }

        public TimeLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public TimeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }
    //onResume()后面是onAttachedToWindow()，并且onAttachedToWindow只会调用一次，
    // 不会受用户操作行为影响。所以在onAttachedToWindow中进行窗口尺寸的修改再合适不过了。
        /*•DecorView的LayoutParams是在ActivityThread的handleResumeActivity中设置的，
        并且该函数会调用Activity的onResume生命周期，所以在onResume之后可以设置窗体尺寸；*/
        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
        }

        public void init() {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.layout_time, null);
            time = (TextView) view.findViewById(R.id.tvTime);//时间
            week = (TextView) view.findViewById(R.id.tvWeed);//星期
            year = (TextView) view.findViewById(R.id.tvYear);//年
            addView(view);
            if (timeListener == null) {
                timeListener = new MainActivity.TimeListener() {
                    @Override
                    public void timeCallBack(CharSequence sysTimeStr) {
                        time.setText(timeF.format(System.currentTimeMillis()));
                        Calendar calendar = Calendar.getInstance();
                        int nowYear = calendar.get(Calendar.YEAR);
                        int nowMonth = calendar.get(Calendar.MONTH);
                        int nowDay = calendar.get(Calendar.DAY_OF_MONTH);

                        if (nowYear != showYear || nowMonth != showMonth || nowDay != showDay) {
                            showYear = nowYear;
                            showMonth = nowMonth;
                            showDay = nowDay;
                            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                            updateDate();
                        }
                    }
                };

                ((MainActivity) getContext()).setTimeLister(timeListener);
//   返回 timeListeners.add(timeLister);增加一个timeLister
            }


        }

//最终发到了主界面的handler，可以更新布局。
        public void updateDate() {

            year.setText(dateF.format(new Date()));
            switch (dayOfWeek) {

                case 1: {
                    week.setText("星期日");
                    break;
                }
                case 2: {
                    week.setText("星期一");
                    break;
                }
                case 3: {
                    week.setText("星期二");
                    break;
                }
                case 4: {
                    week.setText("星期三");
                    break;
                }
                case 5: {
                    week.setText("星期四");
                    break;
                }
                case 6: {
                    week.setText("星期五");
                    break;
                }
                case 7: {
                    week.setText("星期六");
                    break;
                }
            }

        }
    }

   /* public class CityLayout extends RelativeLayout {
        MyApplication.CurrentCityNameViewListener currentCityNameViewListener;

        public CityLayout(Context context) {
            super(context);
            init();
        }

        public CityLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public CityLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        //onResume()后面是onAttachedToWindow()，并且onAttachedToWindow只会调用一次，
        // 不会受用户操作行为影响。所以在onAttachedToWindow中进行窗口尺寸的修改再合适不过了。
        *//*•DecorView的LayoutParams是在ActivityThread的handleResumeActivity中设置的，
        并且该函数会调用Activity的onResume生命周期，所以在onResume之后可以设置窗体尺寸；*//*
        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
        }

        public void init() {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.layout_city, null);
            final TextView tvCity = (TextView) view.findViewById(R.id.tv_city);
            addView(view);
            MyApplication.setCurrentCityNameViewListener(new MyApplication.CurrentCityNameViewListener() {
                @Override
                public void ObitainCityName(String cityName) {
                    tvCity.setText(cityName);

                }
            });
        }
    }*/

}
