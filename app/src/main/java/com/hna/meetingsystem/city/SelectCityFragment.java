package com.hna.meetingsystem.city;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hna.meetingsystem.MainActivity;
import com.hna.meetingsystem.MyApplication;
import com.hna.meetingsystem.R;
import com.hna.meetingsystem.base.BaseFragment;
import com.hna.meetingsystem.base.common.Common;
import com.hna.meetingsystem.city.adapter.MeetingListAdapter;
import com.hna.meetingsystem.city.model.City;
import com.hna.meetingsystem.request.CityRequest;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Jie on 2016-05-06.
 */
/*地图界面，有基类ContainerFragment，实现各种Fragment跳转，包括：选定房会议列表，全部房会议列表，详细预定界面；
实现地图布局，实现点击弹出Dialog界面，实现所在地五角星的添加，以及房间状态的添加*/
public class SelectCityFragment extends BaseFragment {

    public interface CurrentCityNameViewListener{
        void ObitainCityName(String cityName);
    }
    public static CurrentCityNameViewListener currentCityNameViewListener;//监听当前会议室名称，显示到自定义的headerLayout中。
    public static void setCurrentCityNameViewListener(CurrentCityNameViewListener listener)
    {
        currentCityNameViewListener = listener;
    }
    public static final String TAG = SelectCityFragment.class.getSimpleName();//类名

    private final static int[] CITY_IDS = {
            R.id.city_haikou, R.id.city_xian, R.id.city_guizhou, R.id.city_tianjin, R.id.city_nanchang,
            R.id.city_guangzhou, R.id.city_zhengzhou, R.id.city_changsha, R.id.city_chongqing, R.id.city_taiyuan,
            R.id.city_shenyang, R.id.city_hangzhou, R.id.city_qingdao, R.id.city_wuhan, R.id.city_fuzhou,
            R.id.city_shanghai, R.id.city_dalian, R.id.city_nanjing, R.id.city_lanzhou, R.id.city_kunming,
            R.id.city_beijing, R.id.city_chengdu, R.id.city_jinan
    };
    private List<Integer> citys;//
    private List<TextView> citysView;
    private View root;
    private int currentCityId = 0;//当前选中城市的id
    public interface CityChangeListener{
        void getCityData(List<City> cities);
    }
    static CityChangeListener cityChangelistener;//会议室信息变化接口，提供外界获取。
    public static void setCityChangeListener(CityChangeListener listener)
    {
        cityChangelistener = listener;
    }

    private ContainerFragment parentFragment;
    private Thread cityThread;
    public static SelectCityFragment fragment;
    @Bind(R.id.city_dialog)
    View dialog;

   /* @Bind(R.id.city_name)
    TextView tvCityName;//拼音名字*/
    @Bind(R.id.city_zh_name)
    TextView tvCityZHName;//名字

    @Bind(R.id.city_status)
    TextView cityStatus;//状态

    @Bind(R.id.rl_city_dialog_bg)
    RelativeLayout rlCityDialogBg;

    @Bind(R.id.starParent)
    ViewGroup parent;

   /* @Bind(R.id.head)
    HeaderLayout headerLayout;//
*/
    @Bind(R.id.dialog_icon)
    TextView dialogIcon;

    private ImageView start;//所在位置图标

    @Bind(R.id.dialog_contains)
    TextView dialogContains;//大小

    @Bind({R.id.room_projection, R.id.room_screen, R.id.room_port, R.id.room_computer})
    List<TextView> devices;

    @Bind(R.id.ll_citymap_bg)
    LinearLayout llCityMapBg;
    @Bind(R.id.type)
    TextView roomType;//房间类型
    @Bind(R.id.address)
    TextView roomAddress;//地址
    private MainActivity mainActivity;
    public static SelectCityFragment getSelectCityFragment()
    {
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(cityThread==null) {
            cityThread = new CityThread();
            cityThread.start();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_city_select, container, false);
            ButterKnife.bind(this, root);
            getSelectCityDiaolog();
            start = new ImageView(getContext());
            start.setImageResource(R.drawable.ic_here);
            parent.addView(start);
            final int[] local = new int[2];
           /* MyApplication.setCurrentRoomIndexListener(new MyApplication.CurrentRoomIndexListener() {
                @Override
                public void getCurrentRoomIndex(int index) {
                    currentRoomIndex = index;
                }
            });*/
            final View currentCityView = root.findViewById(CITY_IDS[MyApplication.getCityIndex()]);
            currentCityView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                @Nullable
                public boolean onPreDraw() {
                    mainActivity = (MainActivity)getActivity();
                    currentCityView.getLocationOnScreen(local);
//                    Log.d(TAG,local[0]+"----------------------------------------------------------------"+local[1]);
                    if(isAdded())
                    {
                        start.setX(local[0] - mainActivity.rlNavigateWidth);
                        start.setY(local[1] - mainActivity.headerHeight);//算法，五角星坐标
//                    fragment = (SelectCityFragment) getFragmentManager().findFragmentByTag(TAG);
                    }
                    return true;
                }
            });
            rlCityDialogBg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.setVisibility(View.GONE);
                    if(currentCityNameViewListener!=null)
                    {
                        City currentCity = MyApplication.getCurrentCity();
                        currentCityNameViewListener.ObitainCityName(currentCity.name);
                    }
                }
            });
        }

        parentFragment = (ContainerFragment) getParentFragment();//父容器
        init();
        getCitys();//获取会议室状态信息
        return root;
    }

    public View getSelectCityDiaolog() {
        return dialog;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 1:
                    getCitys();
                    break;
                case 0:

                default:
                    break;
            }
        }
    };
    class CityThread extends Thread//会议室信息线程
    {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(10* 1000);
                    Message msg = new Message();
                    msg.what = 1;
//                    mHandler.sendMessage(msg);
                    mHandler.sendMessageDelayed(msg,3*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void getCitys() {//获取会议室信息
        CityRequest.getInstance().cityInfoRequest(Common.CITY_QUERY, new CityRequest.GetCityInfoCallBack() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void getSuccess(List<City> citys) {
//                Looper.prepare();
                if(cityChangelistener!=null)
                {
                    cityChangelistener.getCityData(citys);//把改变的数据传出去；
                }
                for(int i=0;i<citys.size();i++)
                {
                    if(isAdded())
                        citysView.get(i).setBackground(citys.get(i).used?getResources().getDrawable(R.drawable.shape_label_busy): getResources().getDrawable(R.drawable.shape_label_leisure));
                }

            }

            @Override
            public void getFailed(String message) {
                Log.d(TAG,"获取会议室信息失败 "+message);
            }
        });
    }

    private void init() {//初始化会议室布局信息
        citys = new ArrayList<>();
        citysView = new ArrayList<>();
        for (int i = 0; i < CITY_IDS.length; i++) {
            TextView view = (TextView) root.findViewById(CITY_IDS[i]);//获取每一个城市的TextView，用于显示使用状态变色
            citysView.add(view);
            citys.add(CITY_IDS[i]);
        }

        dialog.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });//一个对话框选项
    }


    @OnClick({
            R.id.city_beijing, R.id.city_changsha, R.id.city_chengdu, R.id.city_chongqing, R.id.city_dalian,
            R.id.city_fuzhou, R.id.city_guangzhou, R.id.city_guizhou, R.id.city_haikou, R.id.city_hangzhou,
            R.id.city_kunming, R.id.city_lanzhou, R.id.city_nanchang, R.id.city_nanjing, R.id.city_qingdao,
            R.id.city_shanghai, R.id.city_taiyuan, R.id.city_shenyang, R.id.city_tianjin, R.id.city_wuhan,
            R.id.city_xian, R.id.city_zhengzhou, R.id.city_jinan
    })

    public void clickCity(View view) {
        showCityDialog(citys.indexOf(view.getId()));
    }

    @OnClick(R.id.city_close)
    public void closeDialog(View view) {
        dialog.setVisibility(View.GONE);
        if(currentCityNameViewListener!=null)
        {
            City currentCity = MyApplication.getCurrentCity();
            currentCityNameViewListener.ObitainCityName(currentCity.name);
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void showCityDialog(int id) {//show dialog方法

        dialog.setVisibility(View.VISIBLE);

        City city = MyApplication.getCityById(id);
//        tvCityName.setText("(" + city.name + ")");
        tvCityZHName.setText(city.name);
        if(currentCityNameViewListener!=null)
        {
            currentCityNameViewListener.ObitainCityName(city.name);
        }

        int count = city.capacity;
        roomType.setText((count < 8 ? "小" : (count < 13 ? "中" : "大")) + "型");
        dialogContains.setText(count + "人");

        roomAddress.setText(city.address);
        char[] charsDevices = city.devices.toCharArray();
        for (int i = 0; i < devices.size(); i++) {
            devices.get(i).setSelected(MeetingListAdapter.isContains(charsDevices,i));
        }

      /*  boolean isNow = city.id == MyApplication.getCityIndex();
        dialogIcon.setVisibility(isNow ? View.VISIBLE : View.GONE);//当前会议室图片*/
        City.Status status = City.Status.getStatus(city.used);//会议室状态
        cityStatus.setText(status.getName());
        cityStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(status.getIcon(getContext()), null, null, null);//就是这里

        currentCityId = id;
    }

    @OnClick(R.id.selectCity)
    public void selectCity() {//确定后，主房间变为选中的，展示常规详细预定页面
        if (!dialog.isShown())
            return;
        ScheduleDetailFragment.currentId = currentCityId;
        parentFragment.initView(ContainerFragment.SCHEDULE_FTAGMENT);
        dialog.setVisibility(View.GONE);
//        dialog.setVisibility(View.GONE);
    }

 /*   @Override
    public void onHiddenChanged(boolean hidden) {
        dialog.setVisibility(View.GONE);
        super.onHiddenChanged(hidden);
    }
*/
    @OnClick(R.id.detailMeeting)
    public void detailMeeting(View view) {//显示会议列表界面
        parentFragment.meetingListFragment.nowCityId = currentCityId;
        parentFragment.initView(ContainerFragment.MEETING_FTAGMENT);
        dialog.setVisibility(View.GONE);
    }
   /* @OnClick(R.id.tvChangeView)//右上角切换试图按钮，转到所有房间会议列表界面
    public void changeView() {
        parentFragment.initView(1);
    }*/
}
