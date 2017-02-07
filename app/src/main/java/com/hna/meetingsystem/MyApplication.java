package com.hna.meetingsystem;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hna.meetingsystem.base.utils.DateFormat;
import com.hna.meetingsystem.city.SelectCityFragment;
import com.hna.meetingsystem.city.model.City;
import com.hna.meetingsystem.city.model.CityInfo;
import com.hna.meetingsystem.model.MeetingInfoS;
import com.hna.meetingsystem.request.MeetingRequest;
import com.hna.meetingsystem.request.MeetingRequestImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/4/16.
 */
/*初始化房间，并每5秒获取房间的会议信息，以开始时间的先后顺序存入list集合
并把本房间的信息存储进入sp
*/
public class MyApplication extends Application {
    public final static String TAG = "Application";
    //房间
    private static final String KEY_CITYS = "CITYS";
    //房间代号
    private static final String KEY_CITY_ID = "CITY_ID";
    //记录结果的查取到的会议信息的list
    public List<MeetingInfoS> list = new ArrayList<MeetingInfoS>();
    //当前会议集合
    public List<MeetingInfoS> curList = new ArrayList<MeetingInfoS>();

    public interface CurrentCityNameViewListener{
        void ObitainCityName(String cityName);
    }
    public static CurrentCityNameViewListener currentCityNameViewListener;//监听当前会议室名称，显示到自定义的headerLayout中。
    public static void setCurrentCityNameViewListener(CurrentCityNameViewListener listener)
    {
        MyApplication.currentCityNameViewListener = listener;
    }

    private static String currentRoomNumber;//当前房号
    private static int cityIndex;//当前 房间脚标

    private static Context context;//这个会内存有问题吧。。。。。擦

    private TimeThread timeThread;//时间线程
    private static SharedPreferences setting;//sp存储
    public boolean isWifiOn = false;

    private static List<City> citys ;//citys信息；

    public static boolean isSettingRoomNumber = false;//是否是初始化选择的房间

    //返回
  /*  public static Context getContextObject() {
        return context;这个静态引用会引起内存溢出。。。。。所以取消
    }*/
    protected static MyApplication instance;
    private Thread.UncaughtExceptionHandler restartHandler = new Thread.UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread,  Throwable throwable) {
            final String throwableMessage = throwable.getMessage().toString()+"错误信息走了";

            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Toast.makeText(getBaseContext(),"收集错误信息"+throwableMessage,Toast.LENGTH_LONG).show();
                    File throwableInfo = null;
                    FileOutputStream fos = null;
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        if(throwableInfo==null) {
                            throwableInfo = new File(Environment.getExternalStorageDirectory(), "throwableInfo.txt");
                            if (!throwableInfo.exists()) {
                                throwableInfo.mkdirs();                 }
                        }
                    }
                    try {
                        fos = new FileOutputStream(throwableInfo);
                        fos.write(throwableMessage.getBytes());
                        fos.flush();
                        Looper.loop();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
            restartApp();
        }

    };
    private void restartApp() {
        Intent intent = new Intent(instance,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        instance.startActivity(intent);
        Process.killProcess(Process.myPid());
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Thread.setDefaultUncaughtExceptionHandler(restartHandler);
        context = getApplicationContext();

        setting = context.getSharedPreferences("city_index", MODE_PRIVATE);
        //获取房间脚标
        cityIndex = setting.getInt(KEY_CITY_ID, -1);//默认是-1

        isSettingRoomNumber = cityIndex > -1;//如果cityIndex〉1证明已经初始化设置了一个房间了

        if (!isSettingRoomNumber) {
            cityIndex = 0;//如果没有手动设置，那么默认脚标为0的会议室为默认房间‘海口’
        }
        readCityList();
        SelectCityFragment.setCityChangeListener(new SelectCityFragment.CityChangeListener() {
            @Override
            public void getCityData(List<City> cities) {
                citys = cities;
            }
        });
    }

    public static String getCurrentRoomNumber() {
        return currentRoomNumber;
    }

    public static List<City> getAllCityRooms() {
        return citys;
    }//返回所有city集合
    public interface CurrentRoomIndexListener
    {
        void getCurrentRoomIndex(int index);

    }
    static CurrentRoomIndexListener currentRoomIndexListener;
    public static void setCurrentRoomIndexListener(CurrentRoomIndexListener listener)
    {
        currentRoomIndexListener = listener;
    }
    //设置当前房号的房子，保存到sp
    public static boolean setCurrentRoomNumber(String roomNumber) {
        if (roomNumber == null)
            return false;

        int i = 0;
        for (; i < citys.size(); i++) {
            if (citys.get(i).roomNumber.equals(roomNumber)) {
                cityIndex = i;
                currentRoomNumber = roomNumber;
                if(currentRoomIndexListener!=null)
                {
                    currentRoomIndexListener.getCurrentRoomIndex(cityIndex);
                }
                break;
            }
        }
        if (i == citys.size())
            return false;

        if(currentCityNameViewListener !=null)
        {
            City currentCity = getCurrentCity();
            currentCityNameViewListener.ObitainCityName(currentCity.name);
        }
        SharedPreferences.Editor editor = setting.edit();
        editor.putInt(KEY_CITY_ID, cityIndex);//
        editor.commit();

        return true;
    }//保存成功返回true；

    public static City getCityById(int id) {
        return citys.get(id);
    }

    public static City getCurrentCity() {
        if (citys != null && citys.size() > cityIndex)
            return citys.get(cityIndex);

        return new City();
    }

    public static int getCityIndex() {
        return cityIndex;
    }

    class TimeThread extends Thread {
        @Override
        public void run() {

            if (citys == null) {
                readCityList();
            }

            Message msg = null;
            do {
                try {
                    msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                    Thread.sleep(5*1000);//每5秒发一个消息
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
                case 0:
                    //返回一个bundle，bundle.get(String key);
                    CityInfo cityInfo = (CityInfo) msg.getData().get(KEY_CITYS);
                    citys = cityInfo.citys;//json里的房间信息
                    currentRoomNumber = citys.get(cityIndex).roomNumber;//获取到本房间的好吗（初始化房间完毕）
                    Log.d(TAG, "handleMessage: ------------ read city list finish: " + citys.size());
                    break;
                case 1:
                    //每5秒不断读取本房间里的会议信息
                    fetchData();
                    break;
                default:
                    break;

            }
        }
    };
    //日期监听器
    private DataListener dataListener;

    public interface DataListener {
        void getData(List<MeetingInfoS> data);
    }

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
        isWifiOn = isWiFiActive(context);
        if (!isWifiOn)//判断wifi是否连接
        {
            Toast.makeText(context,"wifi未连接，请检查", Toast.LENGTH_SHORT).show();
        }
        if (timeThread == null) {
            timeThread = new TimeThread();
            timeThread.start();
        }
        citys = getCityInfo().citys;
    }

    public void fetchData() {
        //网络获取今天currentRoomNumber的会议信息
        MeetingRequestImpl.getInstance().getMeetingStartToday(currentRoomNumber, new MeetingRequest.GetMeetingRoomCallBack() {
            @Override
            public void getSuccess(List<MeetingInfoS> meetings) {

                synchronized (this) {
                    if (!curList.equals(meetings)) {
                        list.clear();
                        curList = meetings;
                        for (int i = 0; i < curList.size(); i++) {
                            list.add(curList.get(i));
                        }
                        //从小到大排列会议
                        for (int i = 0; i < list.size(); i++) {
                            for (int j = i; j < list.size(); j++) {
                                //年月日时分秒格式
                                SimpleDateFormat sdf = DateFormat.getDateFormat();
                                try {
                                    MeetingInfoS meetingInfoS;
                                    Date ldate = sdf.parse(list.get(i).startTime);//报错角标越界异常。。。。
                                    Date rdate = sdf.parse(list.get(j).startTime);
                                    if (ldate.getTime() > rdate.getTime()) {
                                        meetingInfoS = list.get(i);
                                        list.set(i, list.get(j));
                                        list.set(j, meetingInfoS);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
//                       移除状态为－１，和１的会议．这两状态能取到么？。。。
                        for (int i = list.size() - 1; i >= 0; i--) {
                            if (list.get(i).status.equals("-1") || list.get(i).status.equals("1")) {
                                list.remove(list.get(i));
                            }
                        }
                        if (dataListener != null) {
                            //开启监听器,获取这个房间的会议信息
                            dataListener.getData(list);
                        }
                    }
                }
            }

            @Override
            public void getFailed(String message) {
                Log.e(TAG, message);
            }
        });

    }


    private void readCityList() {
        //读取该目录下的会议室json数据
        CityInfo cityInfo = getCityInfo();
        citys = cityInfo.citys;
        Message msg = new Message();
        Bundle bundle = new Bundle();
        //数据存储进bundle里
        bundle.putSerializable(KEY_CITYS, cityInfo);
        msg.setData(bundle);
        msg.what = 0;
        mHandler.sendMessage(msg);//发给Handler，获取到本房间的roomNumber
    }
    public static List<City> getCityList()
    {
        return citys;
    }

    private CityInfo getCityInfo() {
        InputStream ioCity = context.getResources().openRawResource(R.raw.citys);
        BufferedReader br = new BufferedReader(new InputStreamReader(ioCity));

        StringBuffer stringBuffer = new StringBuffer();
        String str;

        try {
            while ((str = br.readLine()) != null) {
                stringBuffer.append(str);
            }
        } catch (IOException e) {
            System.err.println("read room file error! " + e.toString());
        }

        Gson gson = new Gson();
        CityInfo citys = new CityInfo();
        citys = gson.fromJson(stringBuffer.toString(), CityInfo.class);
        return citys;
    }

    //判断wifi是否活跃状态
    public static boolean isWiFiActive(Context inContext) {
        Context context = inContext.getApplicationContext();
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
