package com.hna.meetingsystem.city;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hna.meetingsystem.MainActivity;
import com.hna.meetingsystem.MyApplication;
import com.hna.meetingsystem.R;
import com.hna.meetingsystem.base.utils.DateFormat;
import com.hna.meetingsystem.city.adapter.DateSpinnerAdapter;
import com.hna.meetingsystem.city.adapter.SpinnerAdapter;
import com.hna.meetingsystem.city.model.City;
import com.hna.meetingsystem.meeting.MeetingPre;
import com.hna.meetingsystem.meeting.MeetingPredetermineController;
import com.hna.meetingsystem.model.MeetingInfo;
import com.hna.meetingsystem.request.MeetingRequest;
import com.hna.meetingsystem.request.MeetingRequestImpl;
import com.hna.meetingsystem.view.SpinnerPopWindow;
import com.victor.loading.rotate.RotateLoading;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by Jie on 2016-04-13.
 */
public class ScheduleDetailFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    @Bind({
            R.id.time_9, R.id.time_91, R.id.time_10, R.id.time_101, R.id.time_11, R.id.time_111,
            R.id.time_12, R.id.time_12_1, R.id.time_13, R.id.time_13_1, R.id.time_14, R.id.time_14_1,
            R.id.time_15, R.id.time_15_1, R.id.time_16, R.id.time_16_1, R.id.time_17, R.id.time_17_1
    })
    List<ImageView> timeItem;

    public static final String TAG = "ScheduleDetailFragmentCity";

    private View root;

    private CheckBox currentDepartment;

    private int currentHour;
    private Timer timer;
    private MainActivity mContext;
    private int maxIndex; // 当前可以操作的time在timeItems中的最小下标

    private boolean commitSignal = true;

    private List<Integer> selectedIndexs = new ArrayList<>();

    private String departmentString = "";
    private boolean canSubmit = false;
    private MeetingRequest request = MeetingRequestImpl.getInstance();

    public static int currentId = 0;

    Set<Integer> registed = new HashSet<>(); // 已经预定了的时间

    ContainerFragment parentFragment;

   /* @Bind({R.id.hr, R.id.office, R.id.finance, R.id.manager, R.id.brand, R.id.development, R.id.others})
    List<CheckBox> departments;*/

   /* @Bind({R.id.cb_meeting_subject, R.id.cb_meeting_temporary})
    List<CompoundButton> subjects;*/
    @Bind(R.id.rl_schedule_meeting_parent)
    RelativeLayout rlScheduleMeetingParent;
    @Bind(R.id.tv_meeting_attendees)
    TextView tvMeetingAttendees;
    @Bind(R.id.tv_meeting_custom)
    TextView tvSubject;
    @Bind(R.id.qrcode_view)
    View qrcodeView;
    @Bind(R.id.tv_count_second)
    TextView tvCountSecond;
    @Bind(R.id.rl_dropdown_subject)
    RelativeLayout rlDropdownSubject;
    @Bind(R.id.rl_dropdown_attendees)
    RelativeLayout rlDropdownAttendees;
/*
    @Bind(R.id.qrcode_view_bg)
    View qrcodeViewBg;
*/
   /* @Bind(R.id.head)
    HeaderLayout headerLayout;*/
/*
    @Bind(R.id.meeting_detail_roomname)
    TextView tvDetailRoomname;
*/
    @Bind(R.id.submit)
    View btnSubmit;

    @Bind(R.id.rotate_loading)
    RotateLoading rotateLoading;

   /* @Bind(R.id.currentRoomName)
    TextView tvRoomName;*/

    @Bind(R.id.btn_back_home)
    Button btnBackHome;
   /* @Bind(R.id.select_meeting_name)
    EditText etUserName;*/
    private FragmentActivity activity;
    private String date;
    private int sp_selectItemId;//spinner 选取的角标。
    private Spinner spinner;
    private String currentString;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentFragment = (ContainerFragment) getParentFragment();
        timer = new Timer(true);

    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = getActivity();
        mContext = (MainActivity) getActivity();
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_schedule_detail, container, false);
        }
        ButterKnife.bind(this, root);
        rlScheduleMeetingParent.setOnClickListener(new View.OnClickListener() {//隐藏输入框
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(),0);
            }
        });
        spinner = (Spinner) root.findViewById(R.id.spinner_date_list);
        MainActivity.setTimeLister(new MainActivity.TimeListener() {//0点更新下拉列表。
            @Override
            public void timeCallBack(CharSequence sysTimeStr) {
                try {
                    Date dateUpdate = DateFormat.getDateFormat().parse((String) sysTimeStr);
                    int hours = dateUpdate.getHours();
                    int minutes = dateUpdate.getMinutes();
                    int seconds = dateUpdate.getSeconds();
                    if(hours==0&&minutes==0&&seconds<=1)
                    {
                        spinner.setAdapter(new DateSpinnerAdapter());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });
        spinner.setAdapter(new DateSpinnerAdapter());
        selectedIndexs.clear();//???
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int i, long l) {
                releaseSelecedIndex();
                sp_selectItemId = i;//选取的时段。
                final int j = i;
                if(i>0)
                {
                    maxIndex =0;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkUpTime();
                            getAllMeeting();
                }
            });
                }

                else
                {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkUpTime();
                            getAllMeeting();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                sp_selectItemId = 0;
            }
        });
        initEvent();
        checkUpTime();
        getAllMeeting();
        btnBackHome.setBackground(getResources().getDrawable(R.drawable.bg_back_map));
        return root;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String[] subjectArray = getResources().getStringArray(R.array.subjectArray);
        final ArrayList<String> list = new ArrayList<>();
        for(int i=0;i<subjectArray.length;i++)
        {
            list.add(subjectArray[i]);
        }

        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(mContext, list);
        spinnerAdapter.refreshData(list,-1);
        tvSubject.setText(subjectArray[0]);
        final SpinnerPopWindow spinnerPopWindow = new SpinnerPopWindow(mContext);
        spinnerPopWindow.setAdatper(spinnerAdapter);
        spinnerPopWindow.setItemListener(new SpinnerAdapter.IOnItemSelectListener() {

            private String stringSubject;

            @Override
            public void onItemClick(int pos) {
                if(pos>=0&&pos<=list.size())
                {
                    String value = list.get(pos);
                    if(value.equals("自定义"))
                    {
                        View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_set_subject, null);
                        final EditText etInputSubject = (EditText) dialogView.findViewById(R.id.et_input_subject);
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("自定义会议主题").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                stringSubject = etInputSubject.getText().toString();
                                tvSubject.setText(stringSubject);
                            }
                        }).setCancelable(false).setView(dialogView).show();
                    }
                    else {
                        tvSubject.setText(value.toString());
                    }
                }
            }
        });
        rlDropdownSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinnerPopWindow.setWidth(tvSubject.getWidth());
                spinnerPopWindow.showAsDropDown(tvSubject);

            }
        });


        String[] AttendeesArray = getResources().getStringArray(R.array.attendeesArray);
        final ArrayList<String> attendeeslist = new ArrayList<>();
        for(int i=0;i<AttendeesArray.length;i++)
        {
            attendeeslist.add(AttendeesArray[i]);
        }

        SpinnerAdapter attendeesSpinnerAdapter = new SpinnerAdapter(mContext, attendeeslist);
        attendeesSpinnerAdapter.refreshData(attendeeslist,-1);
        final SpinnerPopWindow attendeesSpinnerPopWindow = new SpinnerPopWindow(mContext);
        attendeesSpinnerPopWindow.setAdatper(attendeesSpinnerAdapter);
        attendeesSpinnerPopWindow.setItemListener(new SpinnerAdapter.IOnItemSelectListener() {

            private String stringAttendees;

            @Override
            public void onItemClick(int pos) {
                if(pos>=0&&pos<=attendeeslist.size())
                {
                    String value = attendeeslist.get(pos);
                    if(value.equals("自定义"))
                    {
                        View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_set_subject, null);
                        final EditText etInputSubject = (EditText) dialogView.findViewById(R.id.et_input_subject);
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("自定义部门").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                stringAttendees = etInputSubject.getText().toString();
                                tvMeetingAttendees.setText(stringAttendees);
                            }
                        }).setCancelable(false).setView(dialogView).show();
                    }
                    else {
                        tvMeetingAttendees.setText(value.toString());
                    }
                }
            }
        });
        rlDropdownAttendees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attendeesSpinnerPopWindow.setWidth(tvMeetingAttendees.getWidth());
                attendeesSpinnerPopWindow.showAsDropDown(tvMeetingAttendees);

            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void checkUpTime() {

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());

        if(sp_selectItemId==0) {
            boolean isOverHalfHour = mCalendar.get(mCalendar.MINUTE) > 30;
            currentHour = mCalendar.get(mCalendar.HOUR_OF_DAY);

            if (currentHour >= 9 && currentHour < 18) {
                    maxIndex = (currentHour - 9) * 2 + (isOverHalfHour ? 2 : 1);


            } else if (currentHour >= 18) {
                maxIndex = timeItem.size();
            } else {
                maxIndex = 0;
            }

            for (int i = 0; i < maxIndex; i++) {
                if (i % 2 == 0) {
                    if(isAdded())
                        timeItem.get(i).setBackground(getResources().getDrawable(R.drawable.bg_left_timeup));
                } else {
                    if(isAdded())
                        timeItem.get(i).setBackground(getResources().getDrawable(R.drawable.bg_right_timeup));
                }
            }
        }
        else {
            for (int i = 0; i < timeItem.size(); i++) {
                if (i % 2 == 0) {
                    if(isAdded())
                    timeItem.get(i).setBackground(getResources().getDrawable(R.drawable.selectable_time_line));
                } else {
                    if(isAdded())
                    timeItem.get(i).setBackground(getResources().getDrawable(R.drawable.selectable_time_line2));
                }
            }
        }
    }
    private void initEvent() {

        /*headerLayout.rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFragment.initView(ContainerFragment.SELECT_FRAGMENT);
            }
        });*/
        /*if (departments == null)
            return;
        for (int i = 0; i < departments.size(); i++) {
            departments.get(i).setOnCheckedChangeListener(this);
        }*/

        qrcodeView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });


       /* subjects.get(0).setChecked(true);*/

       /* Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.icon_edit_blue);
        new CustomTextWatch(etUserName, icon)
                .setCallback(new CustomTextWatch.Callback() {
                    @Override
                    public void worldChang(int total) {
                       *//* for (CompoundButton cb :
                                subjects) {
                            cb.setChecked(false);
                        }

                        if (total == 0) {
                            subjects.get(0).setChecked(true);

                        }*//*
                    }
                });
        new CustomTextWatch(etSubject, icon);*/
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @OnClick({
            R.id.time_9, R.id.time_91, R.id.time_101, R.id.time_10, R.id.time_11, R.id.time_111,
            R.id.time_12, R.id.time_12_1, R.id.time_13, R.id.time_13_1, R.id.time_14, R.id.time_14_1,
            R.id.time_15, R.id.time_15_1, R.id.time_16, R.id.time_16_1, R.id.time_17, R.id.time_17_1
    })
    public void selectTime(View v) {

        int index = timeItem.indexOf(v);
        if (index < maxIndex || registed.contains(index))
            return;

        if (selectedIndexs.contains(index)) {
            selectedIndexs.remove(new Integer(index));
        } else {
            selectedIndexs.add(index);
        }

        v.setSelected(selectedIndexs.contains(new Integer(index)));

        checkSubmitBtn();

    }

    private String getTimeByIndex(int index) {

        Date date = new Date();
        date.setHours(index / 2 + 9);
        date.setMinutes((index % 2 == 0 ? 0 : 30));
        date.setSeconds(0);
        Calendar datehms = Calendar.getInstance();
        datehms.setTime(date);
        String mYear = String.valueOf(datehms.get(Calendar.YEAR));// 获取当前年份
        String mMonth = String.valueOf(datehms.get(Calendar.MONTH) + 1);// 获取当前月份
        String mDay = String.valueOf(datehms.get(Calendar.DAY_OF_MONTH) + sp_selectItemId);// 获取当前日份的日期号码
        String mHour = String.valueOf(datehms.get(Calendar.HOUR_OF_DAY));
        String mMinute = String.valueOf(datehms.get(Calendar.MINUTE));
        String mSecond = String.valueOf(datehms.get(Calendar.SECOND));
        String dateString = mYear+"-"+mMonth + "-" + mDay+" "+mHour+":"+mMinute+":"+mSecond;
        return dateString;
    }


   /* private int getIndexByTime(String dateString) {
        Date date = null;
        try {
            date = DateFormat.getDateFormat().parse(dateString);
        } catch (ParseException e) {
            return -1;
        }

        int h = date.getHours() - 9;
//        int m = date.getMinutes() == 30 ? 1 : 0;
        int m = 0;
        int minute = date.getMinutes();
        if (minute <= 30 && minute > 0) {
            m = 1;
        } else if (minute > 30) {
            m = 2;
        }
        return h * 2 + m;
    }*/

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            CompoundButton temp = currentDepartment;
            currentDepartment = (CheckBox) buttonView;
            if (buttonView != temp && temp != null) {
                temp.setChecked(false);
            }
            departmentString = buttonView.getText().toString();
        } else if (currentDepartment == buttonView) {
            departmentString = "";
        }

        checkSubmitBtn();

    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(isAdded()&&spinner!=null) {

            spinner.setSelection(0, true);
        }
        commitSignal = true;

        if (!hidden) {
            getAllMeeting();
            setQrcodeViewVisibility(false);

            selectedIndexs.clear();
            releaseSelecedIndex();
            checkSubmitBtn();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void releaseSelecedIndex() {
        for(int i = 0;i<timeItem.size();i++)
        {
            timeItem.get(i).setSelected(false);
            selectedIndexs.clear();
            checkSubmitBtn();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void checkSubmitBtn() {
        canSubmit = (!selectedIndexs.isEmpty());
        Drawable reserveNow = getResources().getDrawable(R.drawable.bg_reserve_now);
        Drawable reseverNone = getResources().getDrawable(R.drawable.bg_resever_none);
        btnSubmit.setBackground(canSubmit ? reserveNow :reseverNone );
    }

    @OnClick(R.id.submit)
    public void submit(View v) {

        if (!canSubmit || !commitSignal)
            return;

        commitSignal = false;

        setLoding(true);

        getData();


    }

       /* @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @OnClick(R.id.lookOther)
    public void lookOther(View view) {
        skipMeetingList(view);
    }*/


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @OnClick(R.id.meeting_list)
    public void skipMeetingList(View v) {

       /* parentFragment.meetingListFragment.nowCityId = currentId;
        Log.d("test", "skipMeetingList:------------- " + currentId);*/
        parentFragment.initView(ContainerFragment.SELECT_FRAGMENT);
        setQrcodeViewVisibility(false);
    }

    private void getData() {

        final List<MeetingInfo> meetings = new ArrayList<>();

       /* CheckBox checkBox = null;
        for (int i = 0; i < departments.size(); i++) {
            checkBox = departments.get(i);
            if (checkBox.isChecked()) {
                departmentString = checkBox.getText().toString();
                break;
            }
        }*/

        if (selectedIndexs.isEmpty())
            return;

        Collections.sort(selectedIndexs);

        int startIndex = selectedIndexs.get(0);
        int previous = startIndex;
        for (int i = 1; i < selectedIndexs.size(); i++) {
            int index = selectedIndexs.get(i);
            if (previous + 1 == index) {
                previous++;
                if (i == selectedIndexs.size() - 1)
                    meetings.add(createMeeting(startIndex, previous + 1));

                continue;

            }

            meetings.add(createMeeting(startIndex, previous + 1));
            previous = startIndex = index;
        }

        if (previous == startIndex) {
            meetings.add(createMeeting(startIndex, startIndex + 1));
        }

        request.requestAll(meetings, new MeetingRequest.AllCallBack() {
            @Override
            public void call(List faileds) {


                String message;
                if (faileds.size() < meetings.size()) {
                    Log.d(TAG, "会议预定成功");
                     /*BlurCommon.newInstance(getView(), qrcodeViewBg)
                                    .setRadius(6)
                                    .setScaleFactor(4)
                                    .setCallBack(new BlurCommon.CallBack() {
                                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                        @Override
                                        public void call(View tragetView) {
                                            setQrcodeViewVisibility(true);

                                        }
                                    })
                                    .applyBlur(getActivity(), true);*/

                    activity.runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void run() {
                            setQrcodeViewVisibility(true);
                        }
                    });


                    if (faileds.size() == 0) {
                        return;
                    }

                    message = "有" + faileds.size() + "场会议预定失败";
                } else {
                    Log.d(TAG,"---------------------------------------------------------------------------------------- 预定会议数量  "+meetings.size());
                    Log.d(TAG,"---------------------------------------------------------------------------------------- 失败的数量  "+meetings.size());
                    message = "从后台返回的数据集合小于请求集合。会议预定失败,请检查网络连接";
                }
               /* if(!MyApplication.isWiFiActive(getContext()))//如果wifi没连接，请求失败
                {
                    message = "wifi未连接，请求失败";
                }*/
                final String finalMessage = message;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), finalMessage, LENGTH_SHORT).show();
                        setLoding(false);
                    }
                });

            }

        });


    }


    private CompoundButton checkSubject;

   /* @OnCheckedChanged({R.id.cb_meeting_subject, R.id.cb_meeting_temporary})
    public void subjectChange(CompoundButton button, boolean isSelected) {

        if (!isSelected) {
            if (checkSubject == button) {
                checkSubject = null;
            }
            return;
        }
        checkSubject = button;
      *//*  for (CompoundButton cb :
                subjects) {
            cb.setChecked(cb == button);
        }*//*

    }*/

    private MeetingInfo createMeeting(int startIndex, int endIndex) {

        String subject = "临时会议";
        String cust = tvSubject.getText().toString();
        if(!cust.equals(new String(""))||cust!="")
        {
            subject = cust;
        }
        String personel = "海航生态科技";
        String personString = tvMeetingAttendees.getText().toString();
        if(!personString.equals(new String(""))||personString!="")
        {
            personel = personString;
        }
        /*if(personString=="")
        {
            personString = ""+1/0;
        }*/
        MeetingInfo meetingInfo = MeetingPredetermineController.getInstance().createMeeting(currentId,
                new MeetingPre( getTimeByIndex(startIndex), getTimeByIndex(endIndex), subject,personel));
       /* meetingInfo.userName = username;//部门名称*/
        return meetingInfo;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getAllMeeting() {

        setLoding(true);

        City city = MyApplication.getCityById(currentId);
        //tvRoomName.setText(city.zhName + "(" + city.name + ")");
//        tvRoomName.setCompoundDrawablesRelativeWithIntrinsicBounds((currentId == MyApplication.getCityIndex()) ? getContext().getResources().getDrawable(R.drawable.ic_current_meeting_room) : null, null, null, null);
       /* tvDetailRoomname.setText(city.name);
        tvDetailRoomname.setCompoundDrawablesRelativeWithIntrinsicBounds((currentId == MyApplication.getCityIndex()) ? getContext().getResources().getDrawable(R.drawable.ic_current_meeting_room) : null, null, null, null);*/
        Log.d(TAG,"选中的时间段SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS"+sp_selectItemId);
        if(sp_selectItemId==0)
        {

            request.getTodayMeetings(city.roomNumber, new MeetingRequest.GetMeetingRegistedCallBack() {
                @Override
                public void getSuccess(final HashSet<Integer> registeds) {

                   /* if (meetings == null)
                        return;
                    Log.d(TAG,"99999999999999999999999999999999今日会议走了没有。。。。。？？？？？？？？？？？？？？？？？？？？？？？？？");
                    registed.clear();
                    MeetingInfoS meetingInfoS = null;
                    for (int i = 0; i < meetings.size(); i++) {
                        meetingInfoS = meetings.get(i);
                        int startIndex = getIndexByTime(meetingInfoS.startTime);
                        int endIndex = getIndexByTime(meetingInfoS.endTime);
                        if (startIndex == endIndex) {
                            registed.add(startIndex - 1);
                        }
                        for (int j = startIndex; j < endIndex; j++) {
                            registed.add(j);
                        }
                    }*/

                    registed = registeds;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

//                            Iterator<Integer> indexs = registed.iterator();
                            for (int j = maxIndex; j < timeItem.size(); j++) {
                                if (isAdded()) {
                                    if (j % 2 == 0) {
                                        timeItem.get(j).setBackground(getResources().getDrawable(R.drawable.selectable_time_line));
                                    } else {
                                        timeItem.get(j).setBackground(getResources().getDrawable(R.drawable.selectable_time_line2));
                                    }
                                }
                            }
                           /* int i = 0;
                            while (indexs.hasNext()) {
                                Log.d(TAG,"{{{{{{{{{{{{{{{{{{{{{{{{{}}}}}}}}}}}}}}}}}}}}选定日期已经预定的会议数量————"+registed.size());
                                i = indexs.next();
                                if (i >= maxIndex && i < timeItem.size()) {
                                    if (isAdded()) {
                                        if (i % 2 == 0) {
                                            timeItem.get(i).setBackground(getResources().getDrawable(R.drawable.bg_left_done));
                                        } else {
                                            timeItem.get(i).setBackground(getResources().getDrawable(R.drawable.bg_right_done));
                                        }
                                        timeItem.get(i).setSelected(false);
                                    }
                                }
                            }*/
                            for(int index:registeds)
                            {
                                if (index >= maxIndex && index < timeItem.size()) {
                                    if (isAdded()) {
                                        if (index % 2 == 0) {
                                            timeItem.get(index).setBackground(getResources().getDrawable(R.drawable.bg_left_done));
                                        } else {
                                            timeItem.get(index).setBackground(getResources().getDrawable(R.drawable.bg_right_done));
                                        }
                                        timeItem.get(index).setSelected(false);
                                    }
                                }
                            }
                            checkUpTime();
                            setLoding(false);
                        }
                    });

                }
                @Override
                public void getFailed(String message) {
                    Log.d("test", "----- " + message);
                    if(activity!=null)
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setLoding(false);
                            }
                        });
                }
            });
        }else {//非今日会议请求。
            request.getMeetingStart7day(city.roomNumber,sp_selectItemId, new MeetingRequest.GetMeetingRegistedCallBack() {
                @Override
                public void getSuccess(final HashSet<Integer> registeds) {
                    /*if (meetings == null)
                        return;
                    Log.d(TAG,"非今日会议走了没有。。。。。？？？？？？？？？？？？？？？？？？？？？？？？？");

                    registed.clear();
                    MeetingInfoS meetingInfoS = null;
                    for (int i = 0; i < meetings.size(); i++) {
                        Log.d(TAG,"非今日会议走了没有。。。。。？？？？？？？？？？？？？？？？？？？？？？？？？非今日会议数量："+meetings.size());
                        Log.d(TAG,"非今日会议走了没有。。。。。？？？？？？？？？？？？？？？？？？？？？？？？？非今日会议，spinner角标："+sp_selectItemId);


                        meetingInfoS = meetings.get(i);
                        int startIndex = getIndexByTime(meetingInfoS.startTime);
                        int endIndex = getIndexByTime(meetingInfoS.endTime);
                        if (startIndex == endIndex) {
                            registed.add(startIndex - 1);
                        }
                        for (int j = startIndex; j < endIndex; j++) {
                            registed.add(j);
                        }
                    }*/

                    registed = registeds;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Iterator<Integer> indexs = registed.iterator();
                            for (int j = maxIndex; j < timeItem.size(); j++) {
                                if (isAdded()) {
                                    if (j % 2 == 0) {
                                        timeItem.get(j).setBackground(getResources().getDrawable(R.drawable.selectable_time_line));
                                    } else {
                                        timeItem.get(j).setBackground(getResources().getDrawable(R.drawable.selectable_time_line2));
                                    }
                                }
                            }
                           /* int i = 0;

                            while (indexs.hasNext()) {
                                i = indexs.next();
                                if (i >= maxIndex && i < timeItem.size()) {
                                    if (isAdded()) {
                                        if (i % 2 == 0) {
                                            timeItem.get(i).setBackground(getResources().getDrawable(R.drawable.bg_left_done));
                                        } else {
                                            timeItem.get(i).setBackground(getResources().getDrawable(R.drawable.bg_right_done));
                                        }
                                        timeItem.get(i).setSelected(false);
                                    }
                                }
                            }*/
                            for(int index:registeds)
                            {
                                if (index >= maxIndex && index < timeItem.size()) {
                                    if (isAdded()) {
                                        if (index % 2 == 0) {
                                            timeItem.get(index).setBackground(getResources().getDrawable(R.drawable.bg_left_done));
                                        } else {
                                            timeItem.get(index).setBackground(getResources().getDrawable(R.drawable.bg_right_done));
                                        }
                                        timeItem.get(index).setSelected(false);
                                    }
                                }
                            }
                            setLoding(false);
                        }
                    });

                }


                @Override
                public void getFailed(String message) {
                    Log.d("test", "----- " + message);
                    if(activity!=null)
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setLoding(false);
                            }
                        });
                }
            });
        }
    }
    @OnClick(R.id.btn_back_home)
    public void backMap()
    {
        ((ContainerFragment)getParentFragment()).initView(ContainerFragment.SELECT_FRAGMENT);
    }
    private void setLoding(boolean isShow) {
        if (isShow) {
            rotateLoading.start();
            spinner.setEnabled(false);
        } else{
            spinner.setEnabled(true);
            rotateLoading.stop();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setQrcodeViewVisibility(boolean visibility) {

        if (visibility) {
            timer.purge();
            qrcodeView.setVisibility(View.VISIBLE);
            setLoding(false);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isVisible()) {
                                skipMeetingList(null);
                            }
                        }
                    });
                }
            }, 5000);//5秒跳转地图界面。
            final int timeOut = 5;
            MainActivity.setTimeLister(new MainActivity.TimeListener() {
                int time = timeOut;
                @Override
                public void timeCallBack(CharSequence sysTimeStr) {

                    tvCountSecond.setText((time--)+"s后自动返回地图界面");
                    if(time==-1)
                    {
                        tvCountSecond.setText(" ");
                        MainActivity.removeTimeLister(this);
                    }
                }
            });
        } else if (qrcodeView.isShown()) {
            qrcodeView.setVisibility(View.GONE);
            getAllMeeting();
        }

    }
}
