package com.hna.meetingsystem.request;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hna.meetingsystem.base.utils.DateFormat;
import com.hna.meetingsystem.base.common.Common;
import com.hna.meetingsystem.city.model.Version;
import com.hna.meetingsystem.model.MeetingInfo;
import com.hna.meetingsystem.model.MeetingInfoS;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Jie on 2016-04-13.
 */

public class MeetingRequestImpl implements MeetingRequest {//实现类

    private static final String TAG = MeetingRequestImpl.class.getSimpleName();

    private static MeetingRequest request = null;
    int count = 0;
    public static MeetingRequest getInstance() {
        if (request == null)
            request = new MeetingRequestImpl();
        return request;

    }

    private int sum = 0;
    private List<Integer> failedRequest = new ArrayList<>();

    Gson gson = new Gson();

    @Override
    public void request(MeetingInfo info, Callback callback) {
        count++;
        Log.d(TAG+"request",count+"??????????");
        HttpRequest.getInstance().put(Common.MEETING_SUBSCRIBE, gson.toJson(info), callback);//提交数据。。。
    }

    @Override
    public void requestAll(final List<MeetingInfo> meetings, final AllCallBack callback) {
        if (meetings == null)
            return;
        failedRequest.clear();
        sum = 0;
//        failedRequest = null;
        for (int i = 0; i < meetings.size(); i++) {
            final int j = i;
            HttpRequest.getInstance().put(Common.MEETING_SUBSCRIBE, gson.toJson(meetings.get(i)), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    sum++;
                    failedRequest.add(j);//失败的会议数量
                    if (sum == meetings.size()) {
                        callback.call(failedRequest);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    sum++;
                    if (sum == meetings.size()) {
                        callback.call(failedRequest);//返回所有会议？
                    }
                }
            });
        }
    }

    @Override
    public void getMeetingByTime(String roomNumber, String startTime, String endTime, GetMeetingRoomCallBack callBack) {
        Map<String, String> condition = new HashMap<>();
        condition.put(Common.KEY_ROOM_NUMBER, roomNumber);
        condition.put(Common.KEY_START_TIME, startTime);
        condition.put(Common.KEY_END_TIME, endTime);
        queryMeetings(condition, callBack);//请求查询符合条件数据
    }

    @Override
    public void getMeetingStartToday(String roomNumber, GetMeetingRoomCallBack callBack) {
        Map<String, String> condition = new HashMap<>();
        condition.put(Common.KEY_ROOM_NUMBER, roomNumber);
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        condition.put(Common.KEY_START_TIME, DateFormat.getDateFormat().format(calendar.getTime()));
        queryMeetings(condition, callBack);//需要判断
    }


    @Override
    public void registedsIndex(Map<String, String> condition, final GetMeetingRegistedCallBack callback) {
        String url = Common.MEETING_REJISTEDS;
        String roomTag ="";
        boolean firstParams = true;
        if (condition != null && condition.size() > 0) {
            Set<String> keys = condition.keySet();
            for (String key :
                    keys) {
                Log.d(TAG, "queryMeetings: key" + key + " , " + condition.get(key));
                roomTag = condition.get(key);
                url += (firstParams ? "?" : "&") + key + "=" + condition.get(key);
                firstParams = false;
            }
        }

        HttpRequest.getInstance().get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.getFailed("获取会议时刻坐标失败!请检查服务器");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                Log.d(TAG,"==========================+++++++++++++++++++++++++++++++++====================================code :"+code);
                if(code==101)
                {
                    Log.d(TAG,"请求超时");
                }
                Gson gson = new Gson();
                Type type = new TypeToken<HashSet<Integer>>() {
                }.getType();
                String result = response.body().string();
                HashSet<Integer> list = gson.fromJson(result, type);
                callback.getSuccess(list);
            }
        });
    }

    @Override
    public void getMeetingStart7day(String roomNumber, int selectId,GetMeetingRegistedCallBack callBack) {
        Map<String, String> condition = new HashMap<>();
        condition.put(Common.KEY_ROOM_NUMBER, roomNumber);
        Date date = new Date();
        Calendar startCalender = Calendar.getInstance();
        startCalender.setTime(date);
        startCalender.set(Calendar.DAY_OF_MONTH,startCalender.get(Calendar.DAY_OF_MONTH)+selectId);
        startCalender.set(Calendar.HOUR_OF_DAY, 0);
        startCalender.set(Calendar.MINUTE, 0);
        startCalender.set(Calendar.SECOND, 0);
        condition.put(Common.KEY_START_TIME, DateFormat.getDateFormat().format(startCalender.getTime()));

         Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.get(Calendar.DAY_OF_MONTH) +selectId+ 1);
        endCalendar.set(Calendar.HOUR_OF_DAY, 0);
        endCalendar.set(Calendar.MINUTE, 0);
        endCalendar.set(Calendar.SECOND, 0);
        condition.put(Common.KEY_END_TIME, DateFormat.getDateFormat().format(endCalendar.getTime()));

        Log.d(TAG,"取7天的时间范围~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+condition.get(Common.KEY_START_TIME)+"--"+condition.get(Common.KEY_END_TIME));
        registedsIndex(condition, callBack);//需要判断
    }
    Map<String, String> condition = new HashMap<>();

    public void getMeetingsByRoom(String roomNumber, final GetMeetingRoomCallBack callback) {        condition.put(Common.KEY_ROOM_NUMBER, roomNumber);
        queryMeetings(condition, callback);//--

    }

    @Override
    public void getTodayMeetings(String roomNumber, GetMeetingRegistedCallBack callback) {
        Map<String, String> condition = new HashMap<>();
        condition.put(Common.KEY_ROOM_NUMBER, roomNumber);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        endCalendar.set(Calendar.HOUR_OF_DAY, 0);
        endCalendar.set(Calendar.MINUTE, 0);
        endCalendar.set(Calendar.SECOND, 0);

        condition.put(Common.KEY_START_TIME, DateFormat.getDateFormat().format(calendar.getTime()));
        condition.put(Common.KEY_END_TIME, DateFormat.getDateFormat().format(endCalendar.getTime()));
        Log.d(TAG,"取今天的时间范围~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+condition.get(Common.KEY_START_TIME)+"--"+condition.get(Common.KEY_END_TIME));

        registedsIndex(condition, callback);
    }

    @Override
    public void queryMeetings(final Map<String, String> condition, final GetMeetingRoomCallBack callBack) {

        String url = Common.MEETING_SUBSCRIBE;
        String roomTag ="";
        boolean firstParams = true;
        if (condition != null && condition.size() > 0) {
            Set<String> keys = condition.keySet();
            for (String key :
                    keys) {
                Log.d(TAG, "queryMeetings: key" + key + " , " + condition.get(key));
                    roomTag = condition.get(key);
                url += (firstParams ? "?" : "&") + key + "=" + condition.get(key);
                firstParams = false;
            }
        }

        Log.d(TAG, "------queryMeetings :"+roomTag+url + "!!!!!!!!!!!");//url变了

        getMeetings(url, callBack);

    }

    @Override
    public void delete(String id, Callback callback) {
        String encode = null;
        try {
           encode = URLEncoder.encode(id, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpRequest.getInstance().delete(Common.MEETING_DELETE + encode, callback);
    }


    private void getMeetings(final String url, final GetMeetingRoomCallBack callback) {//。。。
        final String url1 = url;
        HttpRequest.getInstance().get(url, new Callback() {//请求返回数据
            @Override
            public void onFailure(Call call, IOException e) {
                callback.getFailed("获取会议列表失败!请检查服务器");
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<MeetingInfoS>>() {
                    }.getType();
                    String result = response.body().string();
                    Log.d(TAG,result+"?????????");
                    if (result.startsWith("<")) {//wifi不通时会返回html数据，这里不处理，返回
                        callback.getFailed("wifi不稳，获取会议列表失败!");
                        return;
                    }
                   /* if(result.contains("500")||result.contains("503")&&result.length()<=60)//有时候会返回500,503状态码
                    {
                        callback.getFailed("返回码： 50");
                        Log.d(TAG,"网络繁忙" );
                        return;
                    }*/
                    List<MeetingInfoS> list = gson.fromJson(result, type);

                    for (MeetingInfoS m :
                            list) {
                        if (m.personnel == null) {
                            m.personnel = "海航生态科技";
                        }
                    }

                    Log.d(TAG, "onResponse: --------- get Meeting list success" + list.size());
                    callback.getSuccess(list);

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    response.body().close();
                }
            }
        });
    }

    public void getVersion(final VersionCallBack callBack) {
        HttpRequest.getInstance().get(Common.VERSION_INFO, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                try {
                    JSONObject jsonObject =
                            new JSONObject(response.body().string());
                    Version version = gson.fromJson(jsonObject.get("android").toString(), Version.class);
                    callBack.call(version);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
