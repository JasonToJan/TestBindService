package com.meizu.statsapp.v3.lib.plugin.session;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.meizu.statsapp.v3.GlobalExecutor;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jinhui on 18-10-9.
 */

class ActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {
    private final static String TAG = "ActivityLifecycleCallback";
    private long initTime, initElapse, startTime, endTime, startElapse, endElapse;
    private Handler mainHandler;
    private final int ONCE_USE = 0x1;
    private SessionController sessionController;

    ActivityLifecycleCallback(SessionController controller) {
        this.sessionController = controller;
        startTime = endTime = startElapse = endElapse = 0;
        initTime = System.currentTimeMillis();
        initElapse = SystemClock.elapsedRealtime();
        mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == ONCE_USE) {
                    Logger.d(TAG, "msg.what: ONCE_USE");
                    if (startTime == 0) {
                        sessionController.onForeground();
                        startTime = initTime;
                        startElapse = initElapse;
                    }
                    sessionController.onBackground();
                    onceUse();
                    startTime = endTime = startElapse = endElapse = 0;
                }
            }
        };
    }

    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    public void onActivityDestroyed(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
        Logger.d(TAG, "onActivityResumed, process:" + android.os.Process.myPid());
        if (startTime == 0) {
            sessionController.onForeground();
            startTime = System.currentTimeMillis();
            startElapse = SystemClock.elapsedRealtime();
        }
        mainHandler.removeMessages(ONCE_USE);
    }

    public void onActivityPaused(Activity activity) {
        Logger.d(TAG, "onActivityPaused, process:" + android.os.Process.myPid());
        endTime = System.currentTimeMillis();
        endElapse = SystemClock.elapsedRealtime();
        mainHandler.removeMessages(ONCE_USE);
        mainHandler.sendEmptyMessageDelayed(ONCE_USE, 1000);
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityStarted(Activity activity) {
    }

    public void onActivityStopped(Activity activity) {
    }

    private void onceUse() {
        long duration = endTime - startTime;
        long duration2 = endElapse - startElapse;
        Logger.d(TAG, "onceUse, startTime:" + startTime + ", endTime:" + endTime + ", duration:" + duration);
        if (startTime > 0 && endTime > 0 && duration > 0) {
            final Map<String, String> properties = new HashMap<>();
            properties.put("startTime", String.valueOf(startTime));
            properties.put("endTime", String.valueOf(endTime));
            properties.put("duration", String.valueOf(duration));
            properties.put("duration2", String.valueOf(duration2));
            GlobalExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (sessionController.sdkInstanceImpl != null) { //这个时间不强制创建sdk实例，没有就算了不上报了
                        sessionController.sdkInstanceImpl.onEvent("_onceuse_", null, properties);
                    }
                }
            });
        }
    }
}