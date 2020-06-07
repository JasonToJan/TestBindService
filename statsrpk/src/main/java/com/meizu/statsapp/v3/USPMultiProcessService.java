package com.meizu.statsapp.v3;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.meizu.statsapp.v3.utils.log.Logger;

public class USPMultiProcessService extends Service {
    private String TAG = USPMultiProcessService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        Logger.d(TAG, "onBind intent: " + intent);
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "onStartCommand intent: " + intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy");
    }

}
