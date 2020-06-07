package com.meizu.statsrpk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.meizu.LogUtils;
import com.meizu.statsapp.v3.utils.log.Logger;
import com.meizu.statsrpk.service.RpkUsageStatsService;

/**
 * Created by huchen on 16-8-22.
 * 仅仅是远端的代理，不具体做事
 */
public class RpkEmitter {
    private final static String TAG = "RpkUsageStatsService" + RpkEmitter.class.getSimpleName();
    private Context context;
    private RpkInfo rpkInfo;
    private IRpkStatsInterface rpkStatsInterface;
    ServiceConn serviceConn;

    public RpkEmitter(final Context context, RpkInfo rpkInfo) {
        this.context = context;
        this.rpkInfo = rpkInfo;

        LogUtils.d(TAG, "begin RpkEmiter构造函数");

        bindService();

        new Thread(new Runnable() {
            @Override
            public void run() {
                bindService();
            }
        }).start();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    LogUtils.d(TAG, "模拟服务被杀死了再开起");
                    context.unbindService(serviceConn);
                    Intent intent = new Intent(context, RpkUsageStatsService.class);
                    context.stopService(intent);
                    bindService();
                }

            }
        }, 5000);

        LogUtils.d(TAG, "end RpkEmiter构造函数");

//        LogUtils.d(TAG, "end RpkEmiter构造函数");
//

//

//
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                LogUtils.d(TAG, "模拟服务被杀死了再开起");
//                context.unbindService(serviceConn);
//                Intent intent = new Intent(context, RpkUsageStatsService.class);
//                context.stopService(intent);
//                bindService();
//
//            }
//        }, 30000);
//
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                LogUtils.d(TAG, "模拟服务被杀死了再开起");
//                context.unbindService(serviceConn);
//                Intent intent = new Intent(context, RpkUsageStatsService.class);
//                context.stopService(intent);
//                bindService();
//            }
//        }, 40000);
//
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                LogUtils.d(TAG, "模拟服务被杀死了再开起");
//                context.unbindService(serviceConn);
//                Intent intent = new Intent(context, RpkUsageStatsService.class);
//                context.stopService(intent);
//                bindService();
//            }
//        }, 50000);
//
//        for (int i = 0; i < 100; i++) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    bindService();
//                }
//            }).start();
//        }

    }

    public void track(final RpkEvent rpkEvent, final RpkInfo rpkInfo) {
        Logger.d(TAG, "rpk track: " + rpkEvent + "," + rpkInfo);
        if (rpkStatsInterface != null) {
            try {
                rpkStatsInterface.track(rpkEvent, rpkInfo);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void bindService() { //此方法是单线程调用的
        Log.e(TAG, "绑定了服务~！！！");
        Intent intent = new Intent(context, RpkUsageStatsService.class);
        serviceConn = new ServiceConn();
        boolean result = context.bindService(intent, serviceConn, Context.BIND_AUTO_CREATE);
        Logger.d(TAG, "bindService, " + serviceConn + " result: " + result);
        LogUtils.d(TAG, "begin wait bindService 1" + serviceConn + " result: " + result);
        if (result) {
            synchronized (serviceConn) {
                try {
                    serviceConn.wait();
                } catch (InterruptedException e) {
                    Logger.w(TAG, "Exception:" + e.toString() + " -Cause:" + e.getCause());
                }
            }
        }
        LogUtils.d(TAG, "finished wait bindService 2" + serviceConn + " result: " + result);
    }

    private class ServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.d(TAG, "已经接受到onServiceConnected");
            try {
                Logger.d(TAG, "onServiceConnected, " + service);
                LogUtils.d(TAG, "onServiceConnected, " + service);
                rpkStatsInterface = IRpkStatsInterface.Stub.asInterface(service);
            } catch (Exception e) {
                Logger.e(TAG, "Exception onServiceConnected:" + e.toString() + " -Cause:" + e.getCause());
                LogUtils.d(TAG, "Exception onServiceConnected:" + e.toString() + " -Cause:" + e.getCause());
            }
            synchronized (this) {
                this.notifyAll();
            }
            LogUtils.d(TAG, "go to finished onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(TAG, "onServiceDisconnected, " + name);
            rpkStatsInterface = null;
            context.unbindService(this);
        }
    }

}
