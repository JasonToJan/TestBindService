package com.meizu.statsapp.v3.lib.plugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.meizu.statsapp.v3.GlobalExecutor;
import com.meizu.statsapp.v3.lib.plugin.utils.IntervalTimer;
import com.meizu.statsapp.v3.lib.plugin.utils.NetInfoUtils;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huchen on 16-8-23.
 */
public class EnvironmentReceiver {
    private final static String TAG = "EnvironmentReceiver";
    public final static String CHANGE_NAME_NETWORKCONNECT = "CHANGE_NAME_NETWORKCONNECT";
    public final static String CHANGE_NAME_POWER = "CHANGE_NAME_POWER";
    private static final long NETWORK_JITTER_DELAY = 30 * 1000;
    private static final long POWER_JITTER_DELAY = 5 * 60 * 1000;

    private static EnvironmentReceiver mEnvironmentReceiver;
    private static final Object sLock = new Object();
    private IntervalTimer mNetworkChangeTimer;
    private IntervalTimer mPowerChangeTimer;

    private List<IEnvListener> mEnvListeners;

    private EnvironmentReceiver(Context context) {
        mEnvListeners = new ArrayList<>();

        this.mNetworkChangeTimer = new IntervalTimer(NETWORK_JITTER_DELAY) {
            @Override
            public void onTrigger() { //稳定地连上了NETWORK_JITTER_DELAY时间后才trigger
                for (IEnvListener listener : mEnvListeners) {
                    if (listener != null) {
                        listener.environmentChanged(CHANGE_NAME_NETWORKCONNECT);
                    }
                }
                mNetworkChangeTimer.cancel();
            }
        };
        this.mPowerChangeTimer = new IntervalTimer(POWER_JITTER_DELAY) {
            @Override
            public void onTrigger() { //稳定地充电了POWER_JITTER_DELAY时间后才trigger
                for (IEnvListener listener : mEnvListeners) {
                    if (listener != null) {
                        listener.environmentChanged(CHANGE_NAME_POWER);
                    }
                }
                mPowerChangeTimer.cancel();
            }
        };

       /* try {
            IntentFilter batteryFilter = new IntentFilter();
            batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            Intent result = context.registerReceiver(null, batteryFilter);
            if (null != result && 0 != result.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)) {
                charging = true;
            }
        } catch (Exception ex) {
            Logger.w(TAG, "Exception: " + ex.toString() + " - Cause: " + ex.getCause());
        }*/

        Receiver receiver = new Receiver();
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception e) {
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        try {
            context.registerReceiver(receiver, filter);
        } catch (Exception e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
    }

    public static EnvironmentReceiver getInstance(Context context) {
        if (mEnvironmentReceiver == null) {
            synchronized (sLock) {
                if (mEnvironmentReceiver == null) {
                    mEnvironmentReceiver = new EnvironmentReceiver(context);
                }
            }
        }
        return mEnvironmentReceiver;
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
                Logger.d(TAG, "ACTION_POWER_CONNECTED, charging = true");
                mPowerChangeTimer.start();
            } else if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
                Logger.d(TAG, "ACTION_POWER_DISCONNECTED, charging = false");
                mPowerChangeTimer.cancel();
            } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                GlobalExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        boolean isOnline = NetInfoUtils.isOnline(context);
                        Logger.d(TAG, "CONNECTIVITY_ACTION, isOnline = " + isOnline);
                        if (isOnline) {
                            mNetworkChangeTimer.start();
                        } else {
                            mNetworkChangeTimer.cancel();
                        }
                    }
                });
            }
        }
    }

    public void addEnvListener(IEnvListener envListener) {
        if (mEnvListeners != null && envListener != null) {
            mEnvListeners.add(envListener);
        }
    }

    public interface IEnvListener {
        void environmentChanged(final String changeName);
    }
}
