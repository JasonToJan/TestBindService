package com.example.testexample;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.meizu.statsapp.v3.utils.log.Logger;
import com.meizu.statsrpk.IRpkStatsInterface;
import com.meizu.statsrpk.service.RpkUsageStatsService;

import static com.example.testexample.MainActivity.START_RPK_SERVICE;
import static com.example.testexample.MainActivity.STOP_RPK_SERVICE;

public class Process2Activity extends AppCompatActivity {

    private static final String TAG = "RpkUsageStatsService####Process2Activity";
    private IRpkStatsInterface rpkStatsInterface;
    private ServiceConn serviceConn;

    MyReceiver receiver = new MyReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process2);
        LogUtils.d(TAG, "onCreate");

        initReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }

    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(START_RPK_SERVICE);
        intentFilter.addAction(STOP_RPK_SERVICE);
        registerReceiver(receiver, intentFilter);
    }

    private void startMyService(View view) { //此方法是单线程调用的

        RpkExecutor.execute(new Runnable() {
            @Override
            public void run() {

            }
        });

        Intent intent = new Intent(Process2Activity.this, RpkUsageStatsService.class);
        serviceConn = new ServiceConn();
        boolean result = bindService(intent, serviceConn, Context.BIND_AUTO_CREATE);
        LogUtils.d(TAG, "bindService, " + serviceConn + " result: " + result);
        if (result) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Logger.d(TAG, "延迟消息开始了");
                    synchronized (this) {
                        if (serviceConn != null) {
                            Logger.d(TAG, "notifyAll");
                            serviceConn.notifyAll();
                        }
                    }
                }
            },10000);

            synchronized (serviceConn) {
                try {
                    Logger.d(TAG, "wait开始了");
                    serviceConn.wait();
                    Logger.d(TAG, "wait结束了");
                } catch (InterruptedException e) {
                    LogUtils.w(TAG, "Exception:" + e.toString() + " -Cause:" + e.getCause());
                }
            }
        }
        LogUtils.d(TAG, "finished wait bindService 2" + serviceConn + " result: " + result);



    }

    public void stopBtn(View view) {
        LogUtils.d(TAG, "开始停止服务");
        if (serviceConn != null) {
            unbindService(serviceConn);
        }
        Intent intent = new Intent(this, RpkUsageStatsService.class);
        stopService(intent);
    }

    public void sendBroadcastClose(View view){
        sendBroadcast(new Intent(STOP_RPK_SERVICE));
    }

    public void sendBroadcastOpen(View view){
        sendBroadcast(new Intent(START_RPK_SERVICE));
    }

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null || intent.getAction() == null) return;
            String action = intent.getAction();
            LogUtils.d(TAG, "接受到广播了Process2=" + action);

            if (action.equals(START_RPK_SERVICE)) {

                startMyService(null);

            } else if (action.equals(STOP_RPK_SERVICE)) {

                stopBtn(null);
            }
        }
    }

    private class ServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.d(TAG, "已经接受到onServiceConnected");
            try {
                LogUtils.d(TAG, "onServiceConnected, " + service);
                rpkStatsInterface = IRpkStatsInterface.Stub.asInterface(service);
            } catch (Exception e) {
                LogUtils.d(TAG, "Exception onServiceConnected:" + e.toString() + " -Cause:" + e.getCause());
            }
            synchronized (this) {
                this.notifyAll();
            }
            LogUtils.d(TAG, "go to finished onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.d(TAG, "onServiceDisconnected, " + name);
            rpkStatsInterface = null;
            unbindService(this);
        }
    }

}
