package com.hna.meetingsystem;

import android.app.Application;
import android.content.Intent;
import android.os.Process;

/**
 * Created by pactera on 2017/1/23.
 */

public class RestartApplication extends Application {
    protected static RestartApplication instance;
    private Thread.UncaughtExceptionHandler restartHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
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
    }
}
