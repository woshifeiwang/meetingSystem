package com.hna.meetingsystem.base.utils;


import android.app.ProgressDialog;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//自定义下载器
public class DownloadManager {

    public static File getFileFromServer(String path, ProgressDialog pd) throws Exception {
        //判断SD卡是否安装
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);//5秒超时
            pd.setMax(conn.getContentLength());
            InputStream is = conn.getInputStream();

            File file = new File(Environment.getExternalStorageDirectory(), "update.apk");
            FileOutputStream fos = new FileOutputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[1024];
            int len;
            int total = 0;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                total += len;
                pd.setProgress(total);
            }
            fos.close();
            bis.close();
            is.close();
            return file;
        } else {
            return null;
        }
    }
}