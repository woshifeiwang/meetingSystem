package com.hna.meetingsystem.request;

import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Jie on 2016-04-13.
 */
public class HttpRequest {//单例okHttp框架
    public final static int CONNECT_TIMEOUT =10;
    public final static int READ_TIMEOUT=15;
    public final static int WRITE_TIMEOUT=10;

    private static class MyRequestHolder {
        public static HttpRequest instance = new HttpRequest();
    }

    public static HttpRequest getInstance() {
        return MyRequestHolder.instance;
    }


    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;

    public HttpRequest() {
        client = new OkHttpClient()
                .newBuilder()
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS)
                .build();
    }

    public void post(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void get(String url, Callback callback) {

        Request request = new Request.Builder().addHeader("Content-type","application/json")
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void put(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();
        client.newCall(request).enqueue(callback);
    }


    public void delete(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        client.newCall(request).enqueue(callback);
    }
}
