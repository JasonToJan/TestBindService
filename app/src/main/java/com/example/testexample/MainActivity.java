package com.example.testexample;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.meizu.statsapp.v3.utils.log.Logger;
import com.meizu.statsrpk.IRpkStatsInterface;
import com.meizu.statsrpk.service.RpkUsageStatsService;

public class MainActivity extends AppCompatActivity {

    public static final String START_RPK_SERVICE = "start_rpk_service";
    public static final String STOP_RPK_SERVICE = "stop_rpk_service";
    private static final String TAG = "RpkUsageStatsService##MainActivity";
    //private IRpkStatsInterface rpkStatsInterface;
    private ServiceConn serviceConn;

    MyReceiver receiver = new MyReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtils.d(TAG, "onCreate");

        initView();
        initReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }

    private void initView(){
        final Button startBtn = findViewById(R.id.am_start_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMyService(v);
            }
        });
    }

    private void initReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(START_RPK_SERVICE);
        intentFilter.addAction(STOP_RPK_SERVICE);
        registerReceiver(receiver, intentFilter);
    }

    private void startMyService(View view) { //此方法是单线程调用的

        Intent intent = new Intent(getApplicationContext(), MyService.class);
        serviceConn = new ServiceConn();
        boolean result = getApplicationContext().bindService(intent, serviceConn, Context.BIND_AUTO_CREATE);
        LogUtils.d(TAG, "bindService, " + serviceConn + " result: " + result);
        if (result) {
            synchronized (serviceConn) {
                try {
                    serviceConn.wait();
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
            getApplicationContext().unbindService(serviceConn);
        }
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);
    }

    public void jumpToProcess(View view) {
        startActivity(new Intent(this,Process2Activity.class));
    }

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            String action = intent.getAction();
            LogUtils.d(TAG, "接受到广播了Main=" + action);

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
//            try {
//                LogUtils.d(TAG, "onServiceConnected, " + service);
//                rpkStatsInterface = IRpkStatsInterface.Stub.asInterface(service);
//            } catch (Exception e) {
//                LogUtils.d(TAG, "Exception onServiceConnected:" + e.toString() + " -Cause:" + e.getCause());
//            }
            synchronized (this) {
                this.notifyAll();
            }
            LogUtils.d(TAG, "go to finished onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(TAG, "onServiceDisconnected, " + name);
            //rpkStatsInterface = null;
            getApplicationContext().unbindService(this);
        }
    }
}
