package com.hna.meetingsystem;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.hna.meetingsystem.base.BaseFragment;
import com.hna.meetingsystem.base.utils.CustomTextWatch;
import com.hna.meetingsystem.base.utils.DateFormat;
import com.hna.meetingsystem.base.utils.DisplayUtil;
import com.hna.meetingsystem.base.ui.blur.BlurCommon;
import com.hna.meetingsystem.city.model.City;
import com.hna.meetingsystem.model.MeetingInfoS;
import com.hna.meetingsystem.request.MeetingRequestImpl;
import com.hna.meetingsystem.view.FancyCoverFlow;
import com.hna.meetingsystem.view.FancyCoverFlowSampleAdapter;
import com.hna.meetingsystem.view.HeaderLayout;
import com.hna.meetingsystem.view.MarqueeText;
import com.victor.loading.rotate.RotateLoading;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/2/15.
 */
public class HomeFragment extends BaseFragment  {
    public final static String TAG = "HomeFragment";
    private TextView tvAttendee;//与会人字段
    private TextView tvMeetingStatus;
    private LinearLayout rlCureentContainer;
    private TextView tvCurrentName;
    private TextView tvCurrentTime;
    private FragmentActivity activity;
    private Button btnBackHome;
    private Button btnMeetingDetails;
    private int max;
    private TextView tvProgressValue;
    private MainActivity mainActivity;

    //会议状态信息
    public enum MeetingStatus {//枚举
        none, now,notNow,hasNext
    }
//默认是空闲状态
    private SimpleDateFormat dateFormat = DateFormat.getDateFormat();
    public static MeetingStatus status;
//默认没有下个会议
    public static MeetingStatus statusNext;
    private MeetingInfoS curMeeting;//当前会议
    private MeetingInfoS nextMeeting;//下个会议
    public static long nextMeetingTime = -1;
    public static long curMeetingEndTime = -1;//当前会议结束时间
    private List<MeetingInfoS> list = new ArrayList<MeetingInfoS>();
//自定义view，加载内容时的，loading 旋转动画图
    private RotateLoading rotateloading;
    private TextView progess;
    private TextView progesstext;//显示会议剩余时间的View
    private Date lastMeetEndTime;//距离会议结束的时间
    private int maxProgessLength = 600;
    private MarqueeText title;//房间名
    /*private LinearLayout nextContainer;
    private TextView tvCurrentTime;//预订好后，下一个会议的时间
    private TextView tvCurrentName;//下一个会议的名称
    private TextView tvNextDesc;//下个会议描述*/
    private RelativeLayout currentMeetView;
    private int time;
    private MyApplication myApplication;

    private RelativeLayout fancyContainer;//展开后查看会议布局,类似相册.有滑动动画
    private PopupWindow popupWindow;
    public FrameLayout blur;//毛玻璃布局

    private View homeContent;//
    private Handler mHandler = new Handler();
    private TextView tvCurrentRoomCity;//当前城市名
    Button btnLeft;//预定0.5
    Button btnRight;//预定1

    private TextView part;//正在进行会议布局,部门
    public SimpleDateFormat sdf = DateFormat.getDateFormat();


    private View root;

    public MainActivity.TimeListener timeListener;//时间监听器
    public MyApplication.DataListener dataListener;//会议信息监听器
    public MainActivity.TimeListener curTimeListener;

    private boolean orderStatus = false;
    private boolean backStatus = false;
    public FancyCoverFlow fancyCoverFlow;
    private String currentNextMeetingString;
  /*  @Bind(R.id.meeting_subject)
    TextView tvDetailSubject;//部门
    @Bind(R.id.name)
    TextView detailUserName;//姓名*/

  /*  @Bind(R.id.quick_predetermine_view)
    View quickPredetrmineView;//非常规定制详情页面*/

    @Bind(R.id.pb_lasttime)
    ProgressBar pbLastTime;
    public interface CurrentCityNameViewListener{
        void ObitainCityName(String cityName);
    }
    public static CurrentCityNameViewListener currentCityNameViewListener;//监听当前会议室名称，显示到自定义的headerLayout中。
    public static void setCurrentCityNameViewListener(CurrentCityNameViewListener listener)
    {
        currentCityNameViewListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.e(TAG,savedInstanceState.toString());
        myApplication = (MyApplication) getActivity().getApplicationContext();
        if (dataListener == null) {
            dataListener = new MyApplication.DataListener() {
                @Override
                public void getData(List<MeetingInfoS> data) {
                    Log.d(TAG,data.size()+"dataLisener changeStatus 走了？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    list = data;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            changeLoading(false);
                            changeStatus();
                            fancyCoverFlow.setAdapter(new FancyCoverFlowSampleAdapter(myApplication.curList));
                            if (curMeeting != null) {
                                int curIndex = myApplication.curList.indexOf(curMeeting);
                                curIndex = myApplication.curList.size() - 1 - curIndex;
                                fancyCoverFlow.setSelection(curIndex);
                            }
                            else if (nextMeeting!=null)
                            {
                                int nextIndex = myApplication.curList.indexOf(nextMeeting);
                                nextIndex = myApplication.curList.size() - 1 - nextIndex;
                                fancyCoverFlow.setSelection(nextIndex);
                            }
                        }
                    });
                }
            };

            myApplication.setDataListener(dataListener);
        }
        /*setRetainInstance(true);*/
        if (timeListener == null) {
            timeListener = new MainActivity.TimeListener() {
                @Override
                public void timeCallBack(CharSequence sysTimeStr) {
                    changeStatus();
                }
            };
            ((MainActivity) getActivity()).setTimeLister(timeListener);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = LayoutInflater.from(getContext()).inflate(R.layout.fragment_home, null);
            ButterKnife.bind(this, root);

            initView();
//            City currentCity = MyApplication.getCurrentCity();
            activity = getActivity();
          /*  if(currentCity!=null)
            {
                MainActivity mainActivity = (MainActivity)activity;
                HeaderLayout header = (HeaderLayout) mainActivity.findViewById(R.id.head_layout);
                header.setCityText(currentCity.name);
            }*/
            City currentCity = MyApplication.getCurrentCity();

            if(currentCity!=null)
            {
                if(currentCityNameViewListener!=null)
                {
                    currentCityNameViewListener.ObitainCityName(currentCity.name);
                }
            }
//            initEvent();
        }
        changeStatus();


        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (curMeeting != null) {
                    int curIndex = myApplication.curList.indexOf(curMeeting);
                    curIndex = myApplication.curList.size() - 1 - curIndex;
                    fancyCoverFlow.setSelection(curIndex);
                }
                else if (nextMeeting!=null)
                {
                    int nextIndex = myApplication.curList.indexOf(nextMeeting);
                    nextIndex = myApplication.curList.size() - 1 - nextIndex;
                    fancyCoverFlow.setSelection(nextIndex);
                }
                FancyCoverFlowSampleAdapter adapter = (FancyCoverFlowSampleAdapter) fancyCoverFlow.getAdapter();
                adapter.notifyDataSetChanged();
                changeView(2);
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {//显示详情预定界面
            @Override
            public void onClick(View view) {

                showScheduleMeetingDetal();
            }
        });
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    private void showScheduleMeetingDetal() {//转到预定界面。
        ScheduleDetailFragment.currentId = MyApplication.getCityIndex();
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.showScheduleDatailFragment();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onResume() {
        blur.setBackground(null);
        changeStatus();
//        showPredetermineView(false);
        super.onResume();
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void initView() {
        tvProgressValue = (TextView) root.findViewById(R.id.tv_progress_value);
        btnMeetingDetails = (Button) root.findViewById(R.id.btn_fancy_meetingDetails);
        btnBackHome = (Button) root.findViewById(R.id.btn_back_home);
        title = (MarqueeText) root.findViewById(R.id.tvTitle);
        tvMeetingStatus = (TextView) root.findViewById(R.id.tv_meetingstatus);//会议室状态
        tvAttendee = (TextView) root.findViewById(R.id.tv_attendeeName);
        tvCurrentName = (TextView) root.findViewById(R.id.tvCurrentName);
        tvCurrentTime = (TextView) root.findViewById(R.id.tvCurrentTime);
        homeContent = root.findViewById(R.id.home_content);
        btnLeft = (Button) root.findViewById(R.id.btn_left);
        btnRight = (Button) root.findViewById(R.id.btn_right);
        currentMeetView = (RelativeLayout) root.findViewById(R.id.rlCurrentMeetFragment);
        progess = (TextView) root.findViewById(R.id.progess);
        progesstext = (TextView) root.findViewById(R.id.progessText);
        rotateloading = (RotateLoading) root.findViewById(R.id.rotateloading);
        fancyContainer = (RelativeLayout) root.findViewById(R.id.fancyContainer);
        blur = (FrameLayout) root.findViewById(R.id.blur);
        rlCureentContainer = (LinearLayout) root.findViewById(R.id.rlOrderCureentContainer);
        fancyCoverFlow = (FancyCoverFlow) root.findViewById(R.id.fancyCoverFlow);
        this.fancyCoverFlow.setUnselectedAlpha(1.0f);
        this.fancyCoverFlow.setUnselectedSaturation(0.0f);
        this.fancyCoverFlow.setUnselectedScale(0.5f);
        this.fancyCoverFlow.setSpacing(-180);
        this.fancyCoverFlow.setMaxRotation(0);
        this.fancyCoverFlow.setScaleDownGravity(0.2f);
        this.fancyCoverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);
        title.setText("之后无会议，请预定");
        currentNextMeetingString = title.getText().toString();
        if (myApplication.curList != null && myApplication.curList.size() != 0) {//初始化fancyCoverFlow
            fancyCoverFlow.setAdapter(new FancyCoverFlowSampleAdapter(myApplication.curList));
            if (curMeeting != null) {
                int curIndex = myApplication.curList.indexOf(curMeeting);
                curIndex = myApplication.curList.size() - 1 - curIndex;
                fancyCoverFlow.setSelection(curIndex);
            }
        }
        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeView(0);
            }
        });
        btnMeetingDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showScheduleMeetingDetal();
            }
        });
        /*progess = (TextView)currentMeetView.findViewById(R.id.progess);
        progesstext = (TextView) currentMeetView.findViewById(R.id.progessText);
        part = (TextView) currentMeetView.findViewById(R.id.part);*/

//        tvCurrentRoomCity = (TextView) root.findViewById(R.id.tv_current_city_name);
//        showPredetermineView(false);
    }
    /*public void marqueeTitle()
    {

        String s = title.getText().toString();
        TextPaint paint = title.getPaint();
        final int textWidth = (int) paint.measureText(s);
        new Runnable(){
            int currentScrollX = textWidth;
            @Override
            public void run() {
                currentScrollX -= 2;//滚动速度。
                title.scrollTo(currentScrollX,0);

                if(title.getScrollX() <= -(title.getWidth()))
                {
                    title.scrollTo(textWidth,0);
                    currentScrollX = textWidth;
                }
                mHandler.postDelayed(this,5);
            }
        }.run();

    }*/

/*    @OnLongClick(R.id.title_head)
    public boolean checkUpdate(View view) {
        ((MainActivity) getActivity()).checkVersion();
        Log.d(TAG, "checkUpdate==========: long click update");
        return true;
    }*/

 /*   @OnClick(R.id.title_btn_skip)
    public void titleSkip(View v) {
        MainActivity activity = (MainActivity) getActivity();
       *//* activity.signalTab2 = 0;
        activity.mTabHost.setCurrentTab(3);*//*
    }*/

   /* private void initEvent() {

       *//* quickPredetrmineView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {//允许触摸选择
                return true;
            }
        });*//*


        root.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {//正在举行会议的删除选项逻辑
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if (backStatus) {
                    return;
                }

                backStatus = true;
                View contentView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.del_current_dialog, null);
                Button btnBack = (Button) contentView.findViewById(R.id.btnBack);
                Button btnConfirm = (Button) contentView.findViewById(R.id.btnConfirm);
                if (DisplayUtil.isPad(getContext())) {
                    popupWindow = new PopupWindow(contentView,
                            ViewGroup.LayoutParams.MATCH_PARENT, 592, true);
                    popupWindow.setBackgroundDrawable(new BitmapDrawable());
                    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            backStatus = false;
                            blur.setBackground(null);
                        }
                    });
                    final View view = v;

                    blur.setBackground(null);
                    BlurCommon.newInstance(getView(), blur)
                            .setRadius(6)
                            .setScaleFactor(4)
                            .setCallBack(new BlurCommon.CallBack() {
                                @Override
                                public void call(View tragetView) {
                                    popupWindow.showAsDropDown(view, 0, -204);
                                }
                            })
                            .applyBlur(getContext(), false);


                } else {
                    popupWindow = new PopupWindow(contentView,
                            ViewGroup.LayoutParams.MATCH_PARENT, 880, true);
                    popupWindow.setBackgroundDrawable(new BitmapDrawable());
                    popupWindow.showAsDropDown(v, 0, -300);
                    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            backStatus = false;
                            blur.setBackground(null);
                        }
                    });
                }


                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (curMeeting != null) {
                            changeLoading(true);

                            MeetingRequestImpl.getInstance().delete(curMeeting.id,
                                    new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    changeLoading(false);
                                                    Toast.makeText(getContext(), "删除失败", Toast.LENGTH_SHORT).show();//非主线程，，，
                                                }
                                            });
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            System.out.println("---------->调用成功" + response.body().string());
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    myApplication.fetchData();
                                                }
                                            });
                                        }
                                    });

                        }
                        popupWindow.dismiss();
                    }
                });

            }
        });*/


        /*Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.icon_edit_white);
        new CustomTextWatch(etSubjectCustom, icon)//实现内容观察
                .setCallback(new CustomTextWatch.Callback() {
                    @Override
                    public void worldChang(int total) {

                        for (CompoundButton cb :
                                meetings) {//每一个checkbox
                            cb.setChecked(false);
                        }

                        if (total == 0) {
                            meetings.get(0).setChecked(true);
                        }

                    }
                });
        new CustomTextWatch(etUserName, icon);//设置自定义图标显示与否的观察者，观察文字内容是否为0；

    }*/


    /*@OnClick({R.id.close, R.id.open})
    public void closeOrOpen(View v) {
        changeView((v.getId() == R.id.close) ? 1 : 2);
    }*/

    private void changeLoading(boolean isShow) {
        if (isShow) {
            rotateloading.start();
        } else if (rotateloading.isStart()) {
            rotateloading.stop();
        }

    }

    public void changeStatus() {//改变状态
        /*City currentCity = MyApplication.getCurrentCity();
        if (currentCity != null) {
            tvCurrentRoomCity.setText(currentCity.zhName + "(" + currentCity.name + ")");//设置当前城市名称
        }*/

        long sysTime = System.currentTimeMillis();
        SimpleDateFormat sdf = DateFormat.getDateFormat();

        Date nowDate = new Date();

        if ((nowDate.getHours() == 9 || nowDate.getHours() == 14) && nowDate.getMinutes() == 0 && nowDate.getSeconds() < 4) {
            //每天9点和14点检查更新
            mainActivity.checkVersion();
        }

        nextMeetingTime = -1;//距离下一个会议时间
        curMeetingEndTime = -1;//正开会议的剩余时间
        curMeeting = null;
        nextMeeting = null;
        if (list.size() != 0) {
            Log.i(TAG, "changeStatus: " + list.size());//打印当前会议室会议数量

            MeetingInfoS meetingInfoS;
            Date startTime, endTime;
            for (int i = 0; i < list.size(); i++) {//1，判断记录当前会议是否有，还有剩余时间。2，判断记录下个会议是否有，距离下个会议的时间。

                meetingInfoS = list.get(i);
                try {
                    startTime = sdf.parse(meetingInfoS.startTime);//会议的开始时间
                    endTime = sdf.parse(meetingInfoS.endTime);//会议结束时间

                    if (sysTime > startTime.getTime() && sysTime < endTime.getTime()) {
                        curMeeting = meetingInfoS;
                        curMeetingEndTime = (endTime.getTime() - sysTime);//剩余时间
                    }


                    if (sysTime < startTime.getTime()) {
                        long offsetTime = startTime.getTime() - sysTime;//距离下一场会议时间
                        if (nextMeetingTime == -1 || nextMeetingTime > offsetTime) {
                            nextMeeting = meetingInfoS;
                            nextMeetingTime = offsetTime;//替换下个会议时间。
                        }
                        break;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }


            if (curMeeting != null) {//当前会议结束判断。
                try {
                    endTime = sdf.parse(curMeeting.endTime);
                    if (sysTime > endTime.getTime()) {      //会议已经结束，重置空闲状态
                        curMeeting = null;
                        curMeetingEndTime = -1;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            try {
                MeetingInfoS meetingLast = list.get(list.size() - 1);
                startTime = sdf.parse(meetingLast.startTime);
                if (startTime.getTime() < sysTime) {//如果到了最后一个会议时间，重置下个会议为null的状态
                    nextMeeting = null;
                    nextMeetingTime = -1;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else {//如果没有会议信息，全部初始状态
            nextMeetingTime = -1;
            curMeetingEndTime = -1;
            curMeeting = null;
            nextMeeting = null;
            return;
        }

        if (curMeeting != null) {//给会议添加不同的标记状态,后面根据这些不同的状态显示不同的布局内容。
            status = MeetingStatus.now;//正在举行会议
        } else {
            status = MeetingStatus.notNow;
        }
        if (nextMeeting == null) {
            statusNext = MeetingStatus.none;//空闲
        } else {
            statusNext = MeetingStatus.hasNext;
        }


       /* Log.d(TAG, "changeStatus: 下个。。。"+nextMeeting.toString());
        Log.d(TAG, "changeStatus: 现在的。。。"+curMeeting.toString());*/
        Log.d(TAG, "!!!!!!!!!!!!!!!!!!!!" + status);
        Log.d(TAG, "????????????????????" + statusNext);
        switch (statusNext){
            case none: {
               /* setBtnListener(btnLeft, 30);
                setBtnListener(btnRight, 60);*/
                if(!currentNextMeetingString.equals("之后无会议，请预定")) {
                    title.setText("之后无会议，请预定");
                    currentNextMeetingString = "之后无会议，请预定";
                }
               /* if(homeContent.getVisibility()==View.VISIBLE)
                changeView(0);//显示0脚标布局，临时预定布局。*/
                break;
            }
            case hasNext:
                    String subject = nextMeeting.subject;
                    String nextStartTime = nextMeeting.startTime;
                    String nextEndTime = nextMeeting.endTime;
                    String nextPersonel = nextMeeting.personnel;
                    if(subject.equals("")||subject!="")
                    {
                        subject = "临时会议";
                    }
                    if(nextPersonel.equals("")||nextPersonel!="")
                    {
                        nextPersonel = "海航生态科技";
                    }
                    if(!currentNextMeetingString.equals("下场会议：" + subject + " " + nextStartTime.substring(0,nextStartTime.length()-3) + " - " + nextEndTime.substring(nextEndTime.length()-8,nextEndTime.length()-3)+" 与会人员："+nextPersonel)) {
                        title.setText("下场会议：" + subject + " " + nextStartTime.substring(0,nextStartTime.length()-3) + " - " + nextEndTime.substring(nextEndTime.length()-8,nextEndTime.length()-3)+" 与会人员："+nextPersonel);//显示下场会议信息。
                        currentNextMeetingString="下场会议：" + subject + " " + nextStartTime.substring(0,nextStartTime.length()-3) + " - " + nextEndTime.substring(nextEndTime.length()-8,nextEndTime.length()-3)+" 与会人员："+nextPersonel;
                    }
                break;
        }

        switch (status) {//根据状态，显示不同的结果信息
            case now: {
                tvMeetingStatus.setText("会议中");
                String nowStartTime = curMeeting.startTime;
                String nowEndTime = curMeeting.endTime;
                pbLastTime.setVisibility(View.VISIBLE);
                tvProgressValue.setVisibility(View.VISIBLE);
                rlCureentContainer.setVisibility(View.VISIBLE);
                try {
                    Date startDate = sdf.parse(nowStartTime);
                    Date endDate = sdf.parse(nowEndTime);
                    long time = endDate.getTime() - startDate.getTime();
                    int timeMin = (int) (time / 60000);//以分为单位的时间
                    initCurrentMeetDate(curMeeting, timeMin);//加载正在进行会议布局。
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            }
            case notNow:
                tvMeetingStatus.setText("空闲");
                pbLastTime.setVisibility(View.INVISIBLE);
                rlCureentContainer.setVisibility(View.GONE);
                tvProgressValue.setVisibility(View.GONE);
                if (curTimeListener != null) {
                    MainActivity.removeTimeLister(curTimeListener);
                }
                break;



       /*     case lessThanHalf: {//距离下个会议少于半小时,提醒用户
                int hour = (int) (nextMeetingTime / (60000 * 60));
                int min = (int) ((nextMeetingTime % (60000 * 60)) / 60000);
                int sec = (int) (nextMeetingTime % 60000) / 1000;
                if (hour > 0) {
                    title.setText("距离下一个会议还剩" + hour + "小时" + min + "分钟" + sec + "秒开始");
                } else {
                    title.setText("距离下一个会议还剩" + min + "分钟" + sec + "秒开始");
                }

                btnLeft.setVisibility(View.VISIBLE);
                btnRight.setVisibility(View.GONE);
//                nextContainer.setVisibility(View.VISIBLE);


                btnLeft.setText("预定" + min + "分钟");
                if (curTimeListener != null) {
                    MainActivity.removeTimeLister(curTimeListener);
                }
                changeView(0);
               *//* setBtnListener(btnLeft, min);*//*

               // changeNextContainer();//显示下个会议布局
                break;
            }
            case halfToHour: {
                int hour = (int) (nextMeetingTime / (60000 * 60));
                int min = (int) ((nextMeetingTime % (60000 * 60)) / 60000);
                int sec = (int) (nextMeetingTime % 60000) / 1000;
                if (hour > 0) {
                    title.setText("距离下一个会议还剩" + hour + "小时" + min + "分钟" + sec + "秒开始");
                } else {
                    title.setText("距离下一个会议还剩" + min + "分钟" + sec + "秒开始");
                }
                btnLeft.setVisibility(View.VISIBLE);
                btnRight.setVisibility(View.GONE);
//                nextContainer.setVisibility(View.VISIBLE);
                btnLeft.setText("预定0.5小时");
                if (curTimeListener != null) {
                    MainActivity.removeTimeLister(curTimeListener);
                }
                changeView(0);
                *//*setBtnListener(btnLeft, 30);*//*
               // changeNextContainer();

                break;
            }
            case moreThanHour: {

                int hour = (int) (nextMeetingTime / (60000 * 60));
                int min = (int) ((nextMeetingTime % (60000 * 60)) / 60000);
                int sec = (int) (nextMeetingTime % 60000) / 1000;
                if (hour > 0) {
                    title.setText("距离下一个会议还剩" + hour + "小时" + min + "分钟" + sec + "秒开始");
                } else {
                    title.setText("距离下一个会议还剩" + min + "分钟" + sec + "秒开始");
                }
                btnLeft.setVisibility(View.VISIBLE);
                btnRight.setVisibility(View.VISIBLE);
//                nextContainer.setVisibility(View.GONE);
                btnLeft.setText("预定0.5小时");
               *//* setBtnListener(btnLeft, 30);
                setBtnListener(btnRight, 60);*//*
                if (curTimeListener != null) {
                    MainActivity.removeTimeLister(curTimeListener);
                }
                changeView(0);
                break;
            }*/
        }
    }

    public void initCurrentMeetDate(MeetingInfoS meeting, int mTime) throws ParseException {//会议还剩余多少时间函数

        time = mTime;//以分为单位的现在会议中的会议持续时间
        tvCurrentName.setText(meeting.subject);
        String startTime = meeting.startTime;
        String endTime = meeting.endTime;
        tvCurrentTime.setText(startTime.substring(startTime.length()-8,startTime.length()-3)+" - "+endTime.substring(endTime.length()-8,endTime.length()-3));
        tvAttendee.setText(meeting.personnel);
        Date startDate = sdf.parse(startTime);
        Date endDate = sdf.parse(endTime);
        max = (int)(endDate.getTime() - startDate.getTime()) / 60000;
        pbLastTime.setMax((int) (endDate.getTime()-startDate.getTime())/60000);

//        lastMeetEndTime = new Date(System.currentTimeMillis() + time * 60 * 1000);//会议结束时间
        //显示progress进度。。。。。
        if(curTimeListener==null) {
            curTimeListener = new MainActivity.TimeListener() {
                @Override
                public void timeCallBack(CharSequence sysTimeStr) {
//                try {
                    int min = (int) (HomeFragment.curMeetingEndTime / 60000);
                    int progress = max - min;
//                    int sec = (int) (HomeFragment.curMeetingEndTime % 60000) / 1000;
                    pbLastTime.setProgress(progress);
                    tvProgressValue.setText((progress * 100 / max) + "%");
                    /*if (min <= 0 && sec <= 0) {
                        progesstext.setText("会议结束,即将推出退出");
                    } else {
                        progesstext.setText("会议剩余" + String.valueOf(min) + "分钟" + sec + "秒");
                    }
                    int progessWidth = (int) (min * 10);
                    if (progessWidth > maxProgessLength) {//大于600。。。一个小时的宽度
                        progessWidth = maxProgessLength;
                    }
                    progess.setLayoutParams(new RelativeLayout.LayoutParams(progessWidth, 250));//设置prograss条,最大600;
                } catch (Exception ce) {
                    ce.printStackTrace();
                }*/
                }
            };
        }
        ((MainActivity) getContext()).setTimeLister(curTimeListener);

/*
        progesstext.setText("会议剩余" + mTime + "分钟");

        int index = meeting.userName.indexOf("(");
        if (index < 0) {
            index = meeting.userName.indexOf("（");
        }
        detailUserName.setText(meeting.userName.substring(0, index > 0 ? index : meeting.userName.length()));//。。。。

        if ("".equals(meeting.department)) {
            part.setVisibility(View.GONE);
        } else {
            part.setVisibility(View.VISIBLE);
            part.setText(meeting.department);//展示正在进行会议布局
        }

        tvDetailSubject.setText(meeting.subject);*/
        /*btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fancyContainer.isShown())
                    changeView(1);
            }
        });*/
    // 展示正在进行会议布局
    }


    private void changeView(int i) {//显示不同布局判断标记

        homeContent.setVisibility(i == 0 ? View.VISIBLE : View.GONE);
        //currentMeetView.setVisibility(i == 1 ? View.VISIBLE : View.GONE);
        fancyContainer.setVisibility(i == 2 ? View.VISIBLE : View.GONE);
    }

    /*public void setBtnListener(View btns, final int times) {//预定times时长的一个非常规会议按钮
        btns.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(final View v) {
                if (orderStatus) {
                    return;
                }

                orderStatus = true;//预定界面?...
                time = times;




                ((MainActivity) getContext()).setTimeLister(new MainActivity.TimeListener() {//时间监听器添加。
                    @Override
                    public void timeCallBack(CharSequence sysTimeStr) {
                        String str = String.valueOf(sysTimeStr);
                        SimpleDateFormat sdf = DateFormat.getDateFormat();
                        try {

                            Date date = sdf.parse(str);
                            long diffOfMin = (lastMeetEndTime.getTime() - date.getTime()) / 60000;
                            if (diffOfMin > 0)
                                progesstext.setText("会议剩余" + String.valueOf(diffOfMin) + "分钟");
                            else {
                                progesstext.setText("会议结束,请退出");
                            }
                            //  WidgetController.setLayoutWidth(progess, (int) diffOfMin);
                            //linearParams.width = (int) diffOfMin;// 控件的宽强制设成30
                            //System.out.print(progess.getWidth());
                            if (diffOfMin <= 0) {
                                diffOfMin = 0;
                            }

                            int progessWidth = (int) (diffOfMin * 10);
//                            if (diffOfMin == 0 ){
//                                progessWidth = 0;
//                            }


                            if (progessWidth > maxProgessLength) {
                                progessWidth = maxProgessLength;
                            }

                            progess.setLayoutParams(new RelativeLayout.LayoutParams(progessWidth, 250));

                        } catch (Exception ce) {
                            ce.printStackTrace();
                        }
                    }
                });

                showPredetermineView(true);

            }
        });
    }*/

   /* public void changeNextContainer() {//下一个会议的时间显示，rlOrderNextContainer中

  if (nextMeeting == null) {
            *//*tvCurrentTime.setText("无");
            tvCurrentName.setText("无");
            tvNextDesc.setText("无");*//*
        } else {
            try {
                Date startTime = sdf.parse(nextMeeting.startTime);
                Date endTime = sdf.parse(nextMeeting.endTime);
                String startHour = startTime.getHours() < 10 ? "0" + String.valueOf(startTime.getHours()) : String.valueOf(startTime.getHours());
                String startMin = startTime.getMinutes() < 10 ? "0" + String.valueOf(startTime.getMinutes()) : String.valueOf(startTime.getMinutes());
                String endHour = endTime.getHours() < 10 ? "0" + String.valueOf(endTime.getHours()) : String.valueOf(endTime.getHours());
                String endMin = endTime.getMinutes() < 10 ? "0" + String.valueOf(endTime.getMinutes()) : String.valueOf(endTime.getMinutes());
//                tvCurrentTime.setText(startHour + ":" + startMin + "--" + endHour + ":" + endMin);
            } catch (ParseException e) {
                e.printStackTrace();
            }
       *//*     tvCurrentName.setText(nextMeeting.department + "·" + nextMeeting.userName);
            tvNextDesc.setText(nextMeeting.subject);*//*
        }
    }*/


   /* @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void showPredetermineView(boolean show) {//显示非常规会议订阅细节界面

        if (!show) {
            blur.setBackground(null);
            orderStatus = false;//细节状态
            quickPredetrmineView.setVisibility(View.GONE);
            return;
        }

        BlurCommon.newInstance(getView(), blur)//毛玻璃处理背景界面
                .setRadius(6)
                .setScaleFactor(4)
                .setCallBack(new BlurCommon.CallBack() {
                    @Override
                    public void call(View tragetView) {
                        quickPredetrmineView.setVisibility(View.VISIBLE);

                        //输入内容初始化
                        etUserName.setText("海航生态科技");
                        etSubjectCustom.setText("自定义");

                        meetings.get(0).setChecked(true);
                    }
                })
                .applyBlur(getContext(), false);

    }*/


    /*@OnClick(R.id.btn_ok)
    public synchronized void predetermineOK(View view) {//枷锁，解决重复请求bug
        if (!dtChecked)
            return;


        String username = etUserName.getText().toString();
        if ("".equals(username)) {
            username = "海航生态科技";
        }

        String subject = "自定义";

        String custom = etSubjectCustom.getText().toString();
        if (!"".equals(custom))
            subject = custom;

        for (CompoundButton cb :
                meetings) {
            if (cb.isChecked()) {
                subject = cb.getText().toString();
            }
        }


        String department = checkDepartment.getText().toString();


        long sysTime = System.currentTimeMillis();
        String startTime = DateFormat.getDateFormat().format(sysTime);
        String endTime = DateFormat.getDateFormat().format(sysTime + time * 60 * 1000);//结束时间。。。这里写的很怪
        changeLoading(true);
        MeetingPre meetingPre = new MeetingPre(department, startTime, endTime, subject);//创建会议。。。。

        MeetingRequest request = MeetingRequestImpl.getInstance();
        MeetingInfo meetingInfo = MeetingPredetermineController.getInstance().createCurrentRoomMeeting(meetingPre);
        meetingInfo.userName = username;//自定义赋值发起人集团名字。

        request.request(meetingInfo, new Callback() {//非常规会议请求实现
            int count1 = count++;
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "failed!");
                mHandle.sendEmptyMessage(0);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    Log.d(TAG,count1+"!!!!!!!!!!!!!!!!!");
                    Log.d(TAG, "预定成功!");
                    mHandle.sendEmptyMessage(1);
                }
            }
        });

    }*/

    /*@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @OnClick(R.id.btn_cancel)
    public void predetermineCancel(View view) {
        showPredetermineView(false);
    }
*/

    CompoundButton checkDepartment;
/*
    @Bind({R.id.cb_hr, R.id.cb_office, R.id.cb_finance, R.id.cb_managers, R.id.cb_brand, R.id.cb_development, R.id.cb_others})
    List<CompoundButton> cbsDepartment;//会议部门

    @Bind({R.id.cb_meeting_temporary, R.id.cb_meeting_subject})
    List<CompoundButton> meetings;//会议主题

    @Bind(R.id.btn_ok)
    Button btnCommit;//确认按钮

    @Bind(R.id.et_username)
    EditText etUserName;
    @Bind(R.id.et_meeting_custom)
    EditText etSubjectCustom;*/

   // private boolean dtChecked = false;


    /*private Handler mHandle = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


            changeLoading(false);
            showPredetermineView(false);
            switch (msg.what) {
                case 0:
                    Toast.makeText(getContext(), "网络请求失，请检查连接！", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Log.d(TAG+" handler",count+++"!!!!!!!");
                    ((MyApplication) getActivity().getApplication()).fetchData();//分配会议
                    break;
            }

        }
    };*/

   /* @OnCheckedChanged({R.id.cb_hr, R.id.cb_office, R.id.cb_finance, R.id.cb_managers, R.id.cb_brand, R.id.cb_development, R.id.cb_others})
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        if (!isChecked) {
            dtChecked = ( checkDepartment != button);
        } else {
            dtChecked = true;//被选中？。。。
            checkDepartment = button;

            checkGroupChange(button, cbsDepartment);
        }
        //inspectCanCommit();
    }

    @OnCheckedChanged({R.id.cb_meeting_temporary, R.id.cb_meeting_subject})
    public void meetingChange(CompoundButton button, boolean isChecked) {
        if (!isChecked)
            return;
        checkGroupChange(button, meetings);

    }


    private void checkGroupChange(CompoundButton button, List<CompoundButton> buttons) {//改变选中checkBox选中视图
        for (CompoundButton cb :
                buttons) {
            cb.setChecked(cb == button);
        }
    }

   *//* private void inspectCanCommit() {//观察是否被选中改变确定按钮颜色
        btnCommit.setBackgroundColor(getResources().getColor(dtChecked ? R.color.mainRed : R.color.gary));
    }*//*
    int count = 0;*/
}
