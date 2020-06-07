package com.meizu.statsapp.v3.lib.plugin.emitter.remote;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.RemoteException;

import com.meizu.statsapp.v3.lib.plugin.IVccOfflineStatsCallback;
import com.meizu.statsapp.v3.lib.plugin.IVccOfflineStatsInterface;
import com.meizu.statsapp.v3.lib.plugin.emitter.EmitterConfig;
import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.List;

/**
 * Created by huchen on 16-8-22.
 */
public class V3RemoteServiceRequester {
    private static final String TAG = V3RemoteServiceRequester.class.getSimpleName();
    private static final Object lock = new Object();
    private static V3RemoteServiceRequester sInstance;

    private Context context;
    private ServiceInfo vccOfflineStatsService;
    private IVccOfflineStatsInterface vccOfflineStatsInterface;
    private final ServiceConn serviceConn = new ServiceConn();
    private IRemoteConnCallback remoteConnCallback;
    private static final long BINDING_TIMEOUT = 3 * 1000;

    private V3RemoteServiceRequester(Context context) {
        this.context = context;
        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(ACTION_VCC_OFFLINE_STATS);
        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(mainIntent, PackageManager.GET_RESOLVED_FILTER);
        Logger.d(TAG, "queryIntentServices for ACTION_VCC_OFFLINE_STATS: " + resolveInfos);
        if (resolveInfos != null) {
            for (ResolveInfo resolveInfo : resolveInfos) {
                String pkgName = resolveInfo.serviceInfo.packageName; // 获得应用程序的包名
                String serviceName = resolveInfo.serviceInfo.name; // 获得该应用程序的启动Activity的name
                if (DATASERVICE_PACKAGENAME.equals(pkgName)) {
                    Logger.d(TAG, "choose serviceName---" + serviceName + " pkgName---" + pkgName);
                    vccOfflineStatsService = resolveInfo.serviceInfo;
                    break;
                }
            }
        }
    }

    public static V3RemoteServiceRequester getInstance(Context context) {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    sInstance = new V3RemoteServiceRequester(context);
                }
            }
        }
        return sInstance;
    }

    void setRemoteConnCallback(IRemoteConnCallback remoteConnCallback) {
        this.remoteConnCallback = remoteConnCallback;
    }

    /**
     * @param packageName
     * @param eventId
     * @param payload
     * @return 返回true只是代表远程调用成功，并不一定真正成功写入到了远端db
     */
    boolean emitterAddEvent(String packageName, final long eventId, final TrackerPayload payload) {
        if (vccOfflineStatsService != null) {
            if (vccOfflineStatsInterface != null) {
                boolean result = internalAdd(packageName, eventId, payload);
                if (result) {
                    return true;
                }
            }
            Logger.w(TAG, "not get remote interface.");
            bindRemoteService();
        }
        return false;
    }

    private boolean internalAdd(String packageName, final long eventId, final TrackerPayload payload) {
        try {
            vccOfflineStatsInterface.emitterAddEvent(packageName, eventId, payload);
            return true;
        } catch (RemoteException e) {
            Logger.w(TAG, "Exception:" + e.toString() + " - Cause:" + e.getCause());
        }
        return false;
    }

    @Deprecated
    public boolean emitterAddEventRealtime(String packageName, final long eventId, TrackerPayload payload) {
        return false;
    }

    /**
     * @param packageName
     * @param eventIds
     * @param payloads
     * @return 返回true只是代表远程调用成功，并不一定真正成功写入到了远端db
     */
    public boolean emitterBulkAddEvents(String packageName, final List<Long> eventIds, final List<TrackerPayload> payloads) {
        if (vccOfflineStatsService != null) {
            if (vccOfflineStatsInterface != null) {
                boolean result = internalBulkAdd(packageName, eventIds, payloads);
                if (result) {
                    return true;
                }
            }
            Logger.w(TAG, "not get remote interface.");
            bindRemoteService();
        }
        return false;
    }

    private boolean internalBulkAdd(String packageName, final List<Long> eventIds, final List<TrackerPayload> payloads) {
        try {
            vccOfflineStatsInterface.emitterBulkAddEvents(packageName, eventIds, payloads);
            return true;
        } catch (RemoteException e) {
            Logger.w(TAG, "Exception:" + e.toString() + " - Cause:" + e.getCause());
        }
        return false;
    }

    /**
     * @param packageName
     * @param value
     * @return
     */
    public boolean emitterUpdateConfig(String packageName, EmitterConfig value) {
        if (vccOfflineStatsService != null) {
            if (vccOfflineStatsInterface != null) {
                boolean result = internalUpdateConfig(packageName, value);
                if (result) {
                    return true;
                }
            }
            Logger.w(TAG, "not get remote interface.");
            bindRemoteService();
        }
        return false;
    }

    private boolean internalUpdateConfig(String packageName, EmitterConfig value) {
        try {
            vccOfflineStatsInterface.emitterUpdateConfig(packageName, value);
            return true;
        } catch (RemoteException e) {
            Logger.w(TAG, "Exception:" + e.toString() + " - Cause:" + e.getCause());
        }
        return false;
    }

    /**
     * @param packageName
     * @return
     */
    public String emitterGetUmid(String packageName) {
        if (vccOfflineStatsService != null) {
            if (vccOfflineStatsInterface != null) {
                boolean requestResult;
                String umid = null;
                try {
                    umid = vccOfflineStatsInterface.emitterGetUmid(packageName);
                    requestResult = true;
                } catch (RemoteException e) {
                    Logger.w(TAG, "Exception:" + e.toString() + " - Cause:" + e.getCause());
                    requestResult = false;
                }
                if (requestResult) {
                    return umid;
                }
            }
            Logger.w(TAG, "not get remote interface.");
        }
        return null;
    }

    /**
     * @param sessionId
     * @param eventSource
     * @return
     */
    public boolean emitterUpdateEventSource(String sessionId, String eventSource) {
        if (vccOfflineStatsService != null) {
            if (vccOfflineStatsInterface != null) {
                boolean result;
                try {
                    vccOfflineStatsInterface.emitterUpdateEventSource(sessionId, eventSource);
                    return true;
                } catch (RemoteException e) {
                    Logger.w(TAG, "Exception:" + e.toString() + " - Cause:" + e.getCause());
                }
            }
            Logger.w(TAG, "not get remote interface.");
        }
        return false;
    }

    /**
     * @param packageName
     * @param callback
     */
    public void setCallback(String packageName, IVccOfflineStatsCallback callback) {
        if (vccOfflineStatsService != null) {
            if (vccOfflineStatsInterface != null) {
                try {
                    vccOfflineStatsInterface.setCallback(packageName, callback);
                } catch (RemoteException e) {
                    Logger.w(TAG, "Exception:" + e.toString() + " - Cause:" + e.getCause());
                }
            }
            Logger.w(TAG, "not get remote interface.");
        }
    }

    //remote
    private static final String DATASERVICE_PACKAGENAME = "com.meizu.dataservice";
    private static final String ACTION_VCC_OFFLINE_STATS = "com.meizu.dataservice.action.vccOfflineStats";

    //////////内部方法
    private void bindRemoteService() {
        if (vccOfflineStatsService != null) {
            synchronized (serviceConn) {
                Intent launchIntent = new Intent();
                launchIntent.setAction(ACTION_VCC_OFFLINE_STATS);
                launchIntent.setPackage(vccOfflineStatsService.packageName);
                ComponentName component = new ComponentName(vccOfflineStatsService.packageName, vccOfflineStatsService.name);
                launchIntent.setComponent(component);
                boolean result = context.bindService(launchIntent, serviceConn, Context.BIND_AUTO_CREATE);
                Logger.d(TAG, "bindService, " + serviceConn + " result: " + result);
                if (result) {
                    try {
                        serviceConn.wait(BINDING_TIMEOUT);
                        Logger.d(TAG, "serviceConn wait END");
                    } catch (InterruptedException e) {
                        Logger.w(TAG, "Exception:" + e.toString() + " - Cause:" + e.getCause());
                    }
                }
            }
        }
    }

    private class ServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                Logger.d(TAG, "onServiceConnected, " + service);
                //Logger.d(TAG, "unbindService, " + serviceConn);
                //context.unbindService(serviceConn);
                vccOfflineStatsInterface = IVccOfflineStatsInterface.Stub.asInterface(service);
                if (remoteConnCallback != null) {
                    remoteConnCallback.onServiceConnected();
                }
            } catch (Exception e) {
                Logger.e(TAG, "Exception onServiceConnected:" + e.toString() + " - Cause:" + e.getCause());
            }
            synchronized (serviceConn) {
                serviceConn.notifyAll();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(TAG, "onServiceDisconnected, " + name);
            vccOfflineStatsInterface = null;
            if (remoteConnCallback != null) {
                remoteConnCallback.onServiceDisconnected();
            }
            context.unbindService(this);
        }
    }

    interface IRemoteConnCallback {
        void onServiceConnected();

        void onServiceDisconnected();
    }
}
