package com.hna.meetingsystem.request;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hna.meetingsystem.base.common.Common;
import com.hna.meetingsystem.city.model.City;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by pactera on 2016/12/19.
 */

public class CityRequest {//获取会议室信息请求封装类
    private static final String TAG = CityRequest.class.getSimpleName();

    private static CityRequest request = null;
    public static CityRequest getInstance()
    {
        if(request==null)
            request = new CityRequest();
        return request;
    }
    public interface GetCityInfoCallBack {//会议室请求，对okhttp再封装
        void getSuccess(List<City> citys);//多status，判断是否繁忙，used 判断是否正在被使用
        void getFailed(String message);
    }
    public void cityInfoRequest(String url, final GetCityInfoCallBack callBack)
    {
        HttpRequest.getInstance().get(Common.CITY_QUERY, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.getFailed("获取城市信息失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try{
                    String result = response.body().string();
                    Log.d(TAG, "onResponse:********************************* City  Request"+result);
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<City>>() {
                    }.getType();
                    List<City> citys= gson.fromJson(result, type);
                    Log.d(TAG,"----------------获取的city号码" +citys.get(0).roomNumber);
                    callBack.getSuccess(citys);//把会议室list信息封装接口传出去。
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally {
                    response.body().close();
                }
            }
        });
    }
}
