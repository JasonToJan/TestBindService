package com.example.testexample;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.meizu.LogUtils;
import com.meizu.statsrpk.service.RpkUsageStatsService;

/**
 * desc:
 * *
 * update record:
 * *
 * created: Jason Jan
 * time:    2020/6/7 20:13
 * contact: jason1211241203@gmail.com
 **/
public class MyService extends Service {

    private String TAG = "TEST##"+ RpkUsageStatsService.class.getSimpleName();
//    private IInterface rpkStatsInterface;

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, "onBind intent: " + intent);
//        synchronized (RpkUsageStatsService.class) {
//            LogUtils.d(TAG, "go in rpkUsageStatsService inner");
//
//        }

//        if (rpkStatsInterface == null) {
//            rpkStatsInterface = new RpkStatsInterface(this);
//        }
//
//        IBinder binder = rpkStatsInterface.asBinder();
//        LogUtils.d(TAG, "onBind return binder: " + binder);
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "begin onCreate");

        LogUtils.d(TAG, "end onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "begin onStartCommand intent: " + intent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "end onDestroy");
    }
}
