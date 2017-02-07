package com.hna.meetingsystem.city;

import android.annotation.TargetApi;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.hna.meetingsystem.base.utils.DateFormat;
import com.hna.meetingsystem.city.adapter.SimpleSpinnerAdapter;
import com.hna.meetingsystem.model.MeetingInfo;
import com.hna.meetingsystem.view.FancyCoverFlow;
import com.hna.meetingsystem.view.FancyCoverFlowSampleAdapter;
import com.hna.meetingsystem.view.HeaderLayout;
import com.hna.meetingsystem.MyApplication;
import com.hna.meetingsystem.R;
import com.hna.meetingsystem.base.BaseFragment;
import com.hna.meetingsystem.base.ui.blur.BlurCommon;
import com.hna.meetingsystem.city.adapter.MeetingsAdapter;
import com.hna.meetingsystem.city.model.City;
import com.hna.meetingsystem.model.MeetingInfoS;
import com.hna.meetingsystem.request.MeetingRequest;
import com.hna.meetingsystem.request.MeetingRequestImpl;
import com.victor.loading.rotate.RotateLoading;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Jie on 2016-05-09.
 */
public class MeetingListFragment extends BaseFragment {

    public static final String TAG = "MeetingListFragment";
    private View root;
    public int nowCityId = 0;//当前城市id
    public City nowCity;
    public List<MeetingInfoS> data;
    /*@Bind(R.id.lv_meeting)
    ListView lvMeetings;

    @Bind(R.id.delete_view)
    View deleteDialog;
    @Bind(R.id.delete_view_bg)
    View deleteViewBg;
    @Bind(R.id.rotate_loading)
    RotateLoading loading;
    @Bind(R.id.empty_view)
    View emptyView;
    @Bind(R.id.head)
    HeaderLayout headerLayout;
    @Bind(R.id.meeting_room_name)
    TextView roomName;

    @Bind(R.id.skip)
    TextView emptySkip;*/
 /*   @Bind(R.id.meeting_detail_roomname)
    TextView tvMeetingRoomName;*/
    @Bind(R.id.btn_back_home)
    Button btnBackMap;

    @Bind(R.id.btn_fancy_meetingDetails)
    Button btnMeetingDetals;

    @Bind(R.id.fancyCoverFlow)
    FancyCoverFlow fancyCoverFlow;
//    private MeetingsAdapter meetingsAdapter;
    private FancyCoverFlowSampleAdapter fancyCoverFlowSampleAdapter;
    private String nowMeetingId = "";
    private Handler mHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:
                    changeLoading(false);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity.getBaseContext(), "请求失败，请检查服务器", Toast.LENGTH_SHORT).show();
                        }
                    });

                    break;
                case 1:
                    //emptyView.setVisibility((data.size() == 0) ? View.VISIBLE : View.GONE);//没有定会议室返回的空列表界面
//                    meetingsAdapter.setData(data);//加载adapter数据
                    fancyCoverFlowSampleAdapter= new FancyCoverFlowSampleAdapter(data);
                    SimpleDateFormat sdf = DateFormat.getDateFormat();
                    long sysTime = System.currentTimeMillis();
                    Date startTime = null;
                    Date endTime = null;
                    MeetingInfoS currentMeeting = null;
                    MeetingInfoS nextMeeting = null;
//                    MeetingInfoS lastMeeting = null;
//                    long offsetLastTime = -1;
                    long offsetTime = -1;
                    try {

                        for (MeetingInfoS meeting : data) {
                            startTime = sdf.parse(meeting.startTime);
                            endTime = sdf.parse(meeting.endTime);
                            if (startTime.getTime() < sysTime && endTime.getTime() > sysTime) {
                                currentMeeting = meeting;
                            }
                            if (startTime.getTime() > sysTime) {
                                if (offsetTime == -1) {
                                    offsetTime = startTime.getTime() - sysTime;
                                } else {
                                    if (offsetTime > startTime.getTime() - sysTime) {
                                        nextMeeting = meeting;
                                        offsetTime = startTime.getTime()-sysTime;
                                    }
                                }
                            }
                         /*   if (endTime.getTime() < sysTime)
                            {
                                if (offsetLastTime==-1){
                                    offsetLastTime = sysTime-endTime.getTime();
                                }else{
                                    if (offsetLastTime>sysTime-endTime.getTime())
                                    {
                                        lastMeeting = meeting;
                                        offsetLastTime = sysTime-endTime.getTime();
                                    }
                                }
                            }*/
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    fancyCoverFlow.setAdapter(fancyCoverFlowSampleAdapter);
                    if(currentMeeting!=null)
                    {
                        int currentIndex = data.indexOf(currentMeeting);
                        currentIndex = data.size() - 1 - currentIndex;
                        fancyCoverFlow.setSelection(currentIndex);
                    }
                    else if(nextMeeting!=null)
                    {
                        int nextIndex = data.indexOf(nextMeeting);
                        nextIndex = data.size() - 1 - nextIndex;
                        fancyCoverFlow.setSelection(nextIndex);
                    }
                   /* else if(lastMeeting!=null)
                    {
                        Toast.makeText(getContext(),"上次会议走了",Toast.LENGTH_LONG).show();
                        int lastIndex = data.indexOf(lastMeeting);
                        lastIndex = data.size() - 1 - lastIndex;
                        fancyCoverFlow.setSelection(lastIndex);
                    }*/
                    fancyCoverFlowSampleAdapter.notifyDataSetChanged();
                    changeLoading(false);

                    break;
              /*  case 3:
                    Toast.makeText(getContext(), "删除失败!请检查网络连接", Toast.LENGTH_SHORT).show();
//                    setDeleteDialogView(false);
                    break;
                case 12:
//                    setDeleteDialogView(false);
//                    requestData();//请求会议数据，删除成功
                    break;*/
            }

        }
    };
    private View root1;

    private RotateLoading loading;
    private FragmentActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        root = inflater.inflate(R.layout.meeting_list_layout, container, false);
        root1 = inflater.inflate(R.layout.fragment_small_detail,container,false);
        ButterKnife.bind(this,root1);
        activity = getActivity();
        fancyCoverFlow = (FancyCoverFlow) root1.findViewById(R.id.fancyCoverFlow);
        fancyCoverFlow.setUnselectedAlpha(1.0f);
        fancyCoverFlow.setUnselectedSaturation(0.0f);
        fancyCoverFlow.setUnselectedScale(0.5f);
        fancyCoverFlow.setSpacing(-180);
        fancyCoverFlow.setMaxRotation(0);
        fancyCoverFlow.setScaleDownGravity(0.2f);
        fancyCoverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);
        init();
        requestData();
        return root1;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void init() {

       /* emptySkip.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//加下划线 马上跳转

        deleteDialog.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });*/


        loading = (RotateLoading) root1.findViewById(R.id.rotate_loading);
        btnBackMap.setBackground(getResources().getDrawable(R.drawable.bg_back_map));
        /* meetingsAdapter = new MeetingsAdapter(data, getContext());
        lvMeetings.setAdapter(meetingsAdapter);//lv加载会议信息*/

        /*headerLayout.rightButton.setOnClickListener(backListener);//接口交给子类处理

        meetingsAdapter.setDeleteCallBack(new MeetingsAdapter.DeleteCallBack() {
            @Override
            public void delete(String id) {//删除按钮事件
                nowMeetingId = id;
                setDeleteDialogView(true);
            }
        });*/
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            nowCity = MyApplication.getCityById(nowCityId);//非隐藏状态
            requestData();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void requestData() {

        if (nowCity == null)
            return;

        changeLoading(true);

        MeetingRequestImpl.getInstance().getMeetingStartToday(nowCity.roomNumber, new MeetingRequest.GetMeetingRoomCallBack() {
            @Override
            public void getSuccess(final List<MeetingInfoS> meetings) {

                if (meetings == null)
                    return;
                data = meetings;
                Message msg = new Message();
                msg.what = 1;//获取城市信息成功
                mHandler.sendMessage(msg);

            }

            @Override
            public void getFailed(String message) {

                mHandler.sendEmptyMessage(0);
                Log.d("test", "getFailed: ----");
            }
        });
    }
    @OnClick(R.id.btn_back_home)
    public void backMap()
    {
        ((ContainerFragment)getParentFragment()).initView(ContainerFragment.SELECT_FRAGMENT);
    }

    @OnClick(R.id.btn_fancy_meetingDetails)
    public void ShowmeetingDetails()
    {
        ((ContainerFragment)getParentFragment()).initView(ContainerFragment.SCHEDULE_FTAGMENT);
    }
    private void changeLoading(boolean isLoading) {//改变加载动画开关
        if (isLoading) {
            loading.start();
        } else if (loading.isStart()) {
            loading.stop();
        }
    }

  /*  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @OnClick(R.id.btn_back)
    public void hideDeleteDialog(View view) {
        setDeleteDialogView(false);
    }

    @OnClick(R.id.btn_ok)
    public void deleteOK(View view) {

        changeLoading(true);

        if (!"".equals(nowMeetingId))
            deleteMeeting(nowMeetingId);//删除选中会议
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setDeleteDialogView(boolean show) {
        if (!show) {
            deleteViewBg.setBackground(null);
            deleteDialog.setVisibility(View.GONE);
            changeLoading(false);//取消
        } else {
            BlurCommon.newInstance(getView(), deleteViewBg)//毛玻璃
                    .setRadius(6)
                    .setScaleFactor(4)
                    .setCallBack(new BlurCommon.CallBack() {
                        @Override
                        public void call(View tragetView) {
                            deleteDialog.setVisibility(View.VISIBLE);
                        }
                    })
                    .applyBlur(getActivity(), false);

        }
    }

    private void deleteMeeting(String id) {
        Log.d(TAG, "delete: ---------" + id);
        MeetingRequestImpl.getInstance().delete(id, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message message = new Message();
                message.what = 3;
                mHandler.sendMessage(message);//删除失败
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Message message = new Message();
                message.what = 12;
                mHandler.sendMessage(message);//删除成功
            }
        });
    }

    @OnClick(R.id.skip)
    public void emptyClick(View view) {//空白点击
        if (emptyClick == null)
            return;
        emptyClick.onClick(view);
    }

    public View.OnClickListener emptyClick;
    public View.OnClickListener backListener;*/
}
