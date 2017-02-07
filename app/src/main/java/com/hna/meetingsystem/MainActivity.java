package com.hna.meetingsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.hna.meetingsystem.base.BackHandleInterface;
import com.hna.meetingsystem.base.utils.DownloadManager;
import com.hna.meetingsystem.city.ContainerFragment;
import com.hna.meetingsystem.city.SelectCityFragment;
import com.hna.meetingsystem.city.adapter.SimpleSpinnerAdapter;
import com.hna.meetingsystem.city.model.City;
import com.hna.meetingsystem.city.model.Version;
import com.hna.meetingsystem.request.MeetingRequest;
import com.hna.meetingsystem.request.MeetingRequestImpl;
import com.hna.meetingsystem.view.HeaderLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import com.hnair.iot.meeting.repository.EWSRepository;

//设置屏幕永亮，初始化布局(FrameLayout和tabHost)， 并每隔1秒检查现在的时间信息，还有版本更新情况。检查房间号码初始化没有，如果没有，就进行初始化。
public class MainActivity extends AppCompatActivity implements Runnable{
    public interface CurrentCityNameViewListener{
        void ObitainCityName(String cityName);
    }
    public static CurrentCityNameViewListener currentCityNameViewListener;//监听当前会议室名称，显示到自定义的headerLayout中。
    public static void setCurrentCityNameViewListener(CurrentCityNameViewListener listener)
    {
        MainActivity.currentCityNameViewListener = listener;
    }
    public int signalTab2 = 0;//状态标记
    public int signalTab3 = -1;//状态标记
    private WindowManager wm = null;
    private WindowManager.LayoutParams wmParams = null;
    private ImageView homebtn = null;//悬浮按钮，home
    private ImageView mapbtn = null;//悬浮按钮，map

    PowerManager powerManager = null;
    //设置屏幕永远亮屏
    PowerManager.WakeLock mWakeLock = null;
    //定义FragmentTabHost对象
    public FragmentTabHost mTabHost;
    //一个接口
    private BackHandleInterface backHandle;


    //定义一个布局
    private LayoutInflater layoutInflater;

    //定义数组来存放Fragment界面
//    private Class fragmentArray[] = {HomeFragment.class, ContainerFragment.class};

    //定义数组来存放按钮图片
/*    private int mImageViewArray[] = {R.drawable.tab_home,
            R.drawable.tab_map};

    //Tab选项卡的文字
    private String mTextviewArray[] = {"主页", "地图"};*/
    public  HomeFragment homeFragment = null;
    public  ContainerFragment containerFragment = null;
    public  ScheduleDetailFragment scheduleDetailFragment = null;
    //对话框
    private FragmentTransaction fragmentTransaction;//管理fragment的事物
    private FragmentManager fragmentManager;
    private AlertDialog updateDialog;
    public  HeaderLayout headerLayout;
    public int headerHeight;
    public int rlNavigateWidth;
    private RelativeLayout rlTitleHead;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        homeFragment = new HomeFragment();
        containerFragment = new ContainerFragment();
        scheduleDetailFragment = new ScheduleDetailFragment();
        initView(savedInstanceState);
//        initFloatView();//初始化悬浮按钮
/*
        try {
            test();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
//保持屏幕永亮
        powerManager = (PowerManager) MainActivity.this.getSystemService(POWER_SERVICE);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //保持CPU 运转，保持屏幕高亮显示，键盘灯也保持亮度
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
//每秒检查时间信息和版本更新信息
        new TimeThread().start();
//检查房间信息

        checkRoomNumber();

       /* final City currentCity = MyApplication.getCurrentCity();
        if (currentCity != null) {
            if(currentCityNameViewListener!=null)
            {
                currentCityNameViewListener.ObitainCityName(currentCity.zhName + "(" + currentCity.name + ")");
            }
        }*/

    }
 /*   private void initFloatView() {
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;//设置window type
//        wmParams.format = PixelFormat.RGB_888;//设置图片格式，效果为背景透明。
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;//设置Wiondow flag
        wmParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        wmParams.x = 0;
        wmParams.y = 0;

        wmParams.width = 120;
        wmParams.height = 100;

        createHomeFloatView();//创建home悬浮按钮
        createMapFloatView();//创建map悬浮按钮
    }

    private void createMapFloatView() {
        mapbtn = new ImageView(this);
        mapbtn.setBackgroundResource(R.drawable.tab_map);

        mapbtn.setSelected(false);
        mapbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mapbtn.isSelected()) {
                    mapbtn.setSelected(true);
                    homebtn.setSelected(false);
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.hide(homeFragment);
                    fragmentTransaction.show(containerFragment);
                    fragmentTransaction.commit();
                } else {
                    return;
                }
            }
        });
        //调整悬浮窗口
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 50;
        wmParams.y = 300;
        //显示FloatView图像；
        wm.addView(mapbtn, wmParams);
    }

    private void createHomeFloatView() {
        homebtn = new ImageView(this);
        homebtn.setBackgroundResource(R.drawable.tab_home);
        homebtn.setSelected(true);
        homebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!homebtn.isSelected()) {
                    homebtn.setSelected(true);
                    mapbtn.setSelected(false);
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.hide(containerFragment);
                    fragmentTransaction.show(homeFragment);
                    fragmentTransaction.commit();
                } else {
                    return;
                }
            }
        });
        //调整悬浮窗口
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 50;
        wmParams.y = 150;
        //显示FloatView图像；
        wm.addView(homebtn, wmParams);

    }

    public void hideFloatView(View v)//销毁悬浮view
    {
//        wm.removeView(v);
    }*/

    /*private void test() throws Exception {
        Log.d("EWS-tes","==================================");
        EWSRepository repo = new EWSRepository();
        List<String> list = new LinkedList<>();
        list.add("sss");
        list.add("fff");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = formatter.parse("2016-11-22 9:00:00");
        Date endDate = formatter.parse("2016-11-21 14:00:00");
        Meeting meeting = new Meeting();
        meeting.setStartTime(startDate);
        meeting.setEndTime(endDate);
        meeting.setUsername("ZB");
        meeting.setRoomName("贵州");
        meeting.setSubject("Test reserve meeting by ZB");
        meeting.setDescription("Test reserve meeting!!");
        // System.out.println(repo.tryCreate(meeting));
        // String result = repo.createMeeting(meeting, null);

        // System.out.println(result);
        // System.out.println(result + "----------------");
        List<Meeting> list2 = repo.list("2016-11-21 7:00:00", "2016-11-30 19:00:00");
        System.out.println(list2);
        Log.d("EWS-test",list2.toString());
        Log.d("EWS-test","==================================");
    }*/


    View dialogView;
    AlertDialog dialog;
    Spinner spinner;

    public void checkRoomNumber() {
        if (!MyApplication.isSettingRoomNumber) {
            dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_room_number, null);
            //下拉条
            spinner = (Spinner) dialogView.findViewById(R.id.city_spinner);
            spinner.setAdapter(new SimpleSpinnerAdapter(MainActivity.this));
            //对话框，不可取消
            dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .show();
        }
    }


    private Version nowVersion;

    //检查版本更新信息
    public void checkVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    this.getPackageName(), 0);
            final int localVersion = packageInfo.versionCode;
            //去服务器查看是否有最新版本
            MeetingRequestImpl.getInstance().getVersion(new MeetingRequest.VersionCallBack() {
                @Override
                public void call(Version version) {

                    nowVersion = version;
                    if (localVersion < version.version) {
                        mHandler.sendEmptyMessage(9);
                    }
                }
            });

        } catch (PackageManager.NameNotFoundException e) {
            Log.d("MainActivity", "检查更新失败");
        }
    }


    ProgressDialog pd;

    protected void downloadApk(final String apk_url) {
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在下载更新");
        pd.setCancelable(false);
        pd.show();
//耗时操作
        new Thread() {
            @Override
            public void run() {
                super.run();
                download_InstallApk(apk_url);
            }
        }.start();

    }

    //下载安装包
    void download_InstallApk(String apk_url) {
        try {
            File file = DownloadManager.getFileFromServer(apk_url, pd);
            installApk(file);
            mHandler.sendEmptyMessage(3);//成功，传递消息给handler

        } catch (Exception e) {
            mHandler.sendEmptyMessage(2);//失败，传递消息给handler
            e.printStackTrace();
        }
    }

    //安装apk
    protected void installApk(File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }


    public void clickOK(View view) {
//        EditText etRoomNumber = (EditText) dialogView.findViewById(R.id.et_room_number);
//
//
//        String roomNumber = etRoomNumber.getText().toString();
//        if (roomNumber.length() < 5) {
//            return;
//        }

        if (MyApplication.setCurrentRoomNumber(String.valueOf(spinner.getSelectedItemPosition() + 10010))) {
            dialog.dismiss();
        } else {
            Toast.makeText(this, "编号有误，请重输！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        /**
         * 设置为横屏
         */
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //获取屏幕亮度锁
        mWakeLock.acquire();
        super.onResume();
    }

    @Override
    protected void onPause() {
       /* if(homebtn!=null&&mapbtn!=null) {
            hideFloatView(homebtn);
            hideFloatView(mapbtn);
        }*/
        //释放屏幕亮度锁
        mWakeLock.release();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      /*  hideFloatView(homebtn);
        hideFloatView(mapbtn);*/
    }

    private void initView(Bundle savedInstanceState) {

        /*
           layoutInflater = LayoutInflater.from(this);
        //实例化TabHost对象，得到TabHost
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        //得到fragment的个数
        int count = fragmentArray.length;

        for (int i = 0; i < count; i++) {
            //为每一个Tab按钮设置图标、文字和内容
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
            //将Tab按钮添加进Tab选项卡中
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            //设置Tab按钮的背景
            mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.select_tab_backgroud);
        }*/
        headerLayout = (HeaderLayout) findViewById(R.id.head_layout);
        homebtn = (ImageView)findViewById(R.id.iv_home);
        mapbtn = (ImageView) findViewById(R.id.iv_map);
        rlTitleHead = (RelativeLayout) findViewById(R.id.title_head);
        final RelativeLayout rlNavigate = (RelativeLayout) findViewById(R.id.title_rl_navigate);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if(savedInstanceState==null) {
            fragmentTransaction.add(R.id.realtabcontent, homeFragment, HomeFragment.TAG);
            fragmentTransaction.add(R.id.realtabcontent, containerFragment, containerFragment.TAG);
            fragmentTransaction.add(R.id.realtabcontent, scheduleDetailFragment, ScheduleDetailFragment.TAG);
            fragmentTransaction.hide(containerFragment);
            fragmentTransaction.hide(scheduleDetailFragment);
            fragmentTransaction.commit();
        }
        homebtn.setSelected(true);
        rlTitleHead. getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                headerHeight = rlTitleHead.getMeasuredHeight();
                return true;
            }
        });
        rlNavigate.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                rlNavigateWidth =rlNavigate.getMeasuredWidth();
                return true;
            }
        });
        mapbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mapbtn.isSelected()) {
                    mapbtn.setSelected(true);
                    homebtn.setSelected(false);
                    showContainerFragment();
                } else {
                    return;
                }
            }
        });
        homebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentCityNameViewListener!=null)
                {
                    City currentCity = MyApplication.getCurrentCity();
                    currentCityNameViewListener.ObitainCityName(currentCity.name);
                }
                if (!homebtn.isSelected()) {
                    homebtn.setSelected(true);
                    mapbtn.setSelected(false);
                    showHomeFragment();
                } else {
                    return;
                }
            }
        });
    }
    public void showHomeFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(containerFragment);
        fragmentTransaction.show(homeFragment);
        fragmentTransaction.hide(scheduleDetailFragment);
        fragmentTransaction.commit();
    }

    public void showContainerFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(homeFragment);
        fragmentTransaction.hide(scheduleDetailFragment);
        fragmentTransaction.show(containerFragment);
        containerFragment.initView(ContainerFragment.SELECT_FRAGMENT);
        fragmentTransaction.commit();
    }

    public void showScheduleDatailFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(containerFragment);
        fragmentTransaction.hide(homeFragment);
        fragmentTransaction.show(scheduleDetailFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void run() {

        mHandler.postDelayed(this,1000);
    }

   /* //设置tab图片
    private View getTabItemView(int index) {
        View view = layoutInflater.inflate(R.layout.item_tab, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
        imageView.setImageResource(mImageViewArray[index]);
        return view;
    }*/


    //时间线程
    class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Message msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                    Thread.sleep(1000);//隔一秒发一次，查看是否有更新。还有现在的时辰及距离每个会议的倒计时
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1://把监听到的时间转成年月日返回给callback
                    long sysTime = System.currentTimeMillis();
                    CharSequence sysTimeStr = DateFormat.format("yyyy-MM-dd HH:mm:ss", sysTime);
                    for (int i = 0; i < timeListeners.size(); i++) {
                        TimeListener timeListener = timeListeners.get(i);
                        timeListener.timeCallBack(sysTimeStr);
                    }
                    break;
                case 2://版本更新失败
                    Toast.makeText(MainActivity.this, "更新失败!", Toast.LENGTH_SHORT).show();
                case 3://版本更新完毕，关闭进度对话框
                    if (pd != null)
                        pd.dismiss();
                    break;
                case 9://更新版本
                    if (nowVersion != null) {
                        if (updateDialog == null) {
                            updateDialog = new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("有新版本，请更新")
                                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //下载新版本
                                            downloadApk(nowVersion.url);
                                        }
                                    })
                                    .setNegativeButton("取消", null)
                                    .create();
                        }

                        if (!updateDialog.isShowing()) {
                            updateDialog.show();
                        }

                    }

                    break;
                default:
                    break;
            }
        }
    };

    //时间监听器
    public interface TimeListener {
        public void timeCallBack(CharSequence sysTimeStr);
    }

    //监听器集合
    public static List<TimeListener> timeListeners = new ArrayList<TimeListener>();

    //添加监听器方法
    public static void setTimeLister(TimeListener timeLister) {
        timeListeners.add(timeLister);
    }

    //删除监听器方法
    public static void removeTimeLister(TimeListener timeListener) {
        if (timeListeners.contains(timeListener)) {
            timeListeners.remove(timeListener);
        }
    }

    //返回键设置
    /*@Override
    public void onBackPressed() {
        backHandle = (BaseFragment) getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
        if (backHandle == null || !backHandle.onBackPress()) {
            super.onBackPressed();
        }
    }*/
}
