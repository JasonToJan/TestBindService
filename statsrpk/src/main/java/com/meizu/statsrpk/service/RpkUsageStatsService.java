package com.meizu.statsrpk.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import com.meizu.LogUtils;
import com.meizu.statsapp.v3.UsageStatsProxy3;
import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.events.Event;
import com.meizu.statsapp.v3.lib.plugin.events.EventUtil;
import com.meizu.statsapp.v3.lib.plugin.events.PageEvent;
import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;
import com.meizu.statsapp.v3.lib.plugin.tracker.LocationFetcher;
import com.meizu.statsapp.v3.lib.plugin.tracker.subject.Subject;
import com.meizu.statsrpk.IRpkStatsInterface;
import com.meizu.statsrpk.RpkEvent;
import com.meizu.statsrpk.RpkInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RpkUsageStatsService extends Service {
    private String TAG = "TEST##"+RpkUsageStatsService.class.getSimpleName();
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

    class RpkStatsInterface extends IRpkStatsInterface.Stub {
        private RpkEmitterWorker rpkEmitterWorker;
        private ScheduledExecutorService executorService;
        private Subject subject;
        private final Context mContext;

        RpkStatsInterface(final Context context) {
            LogUtils.d(TAG, "begin rpkStatsInterface");
            mContext = context;
//            executorService = Executors.newScheduledThreadPool(1);
//            executorService.execute(new Runnable() {
//                @Override
//                public void run() {
//                    Subject.SubjectBuilder builder = new Subject.SubjectBuilder();
//                    builder.context(context);
//                    //相对耗时，放在异步线程中较好
//                    subject = builder.build();
//                    rpkEmitterWorker = new RpkEmitterWorker(mContext, 1);
//                    LogUtils.d(TAG, "finished RpkEmitterWorker");
//                }
//            });
            LogUtils.d(TAG, "finished rpkStatsInterface");
        }

        @Override
        public void track(final RpkEvent rpkEvent, final RpkInfo rpkInfo) throws RemoteException {
            LogUtils.d(TAG, "begin track");
//            executorService.execute(new Runnable() {
//                @Override
//                public void run() {
//                    if (rpkEmitterWorker != null) {
//                        TrackerPayload payload = null;
//                        if (rpkEvent.type.equals("action_x")) {
//                            Event event = EventUtil.buildActionXEvent(mContext, rpkEvent.eventName, rpkEvent.pageName, rpkEvent.properties);
//                            payload = event.generatePayload();
//                            payload.add(Parameters.SESSION_ID, rpkEvent.sessionId);
//                        } else if (rpkEvent.type.equals("page")) {
//                            PageEvent event = EventUtil.buildPageEvent(mContext, rpkEvent.eventName, (String) rpkEvent.properties.get("start"), (String) rpkEvent.properties.get("end"));
//                            Map<String, String> properties = new HashMap<>();
//                            properties.put("duration2", String.valueOf((String) rpkEvent.properties.get("duration2")));
//                            event.setProperties(properties);
//                            payload = event.generatePayload();
//                            payload.add(Parameters.SESSION_ID, rpkEvent.sessionId);
//                        }
//
//                        if (payload != null) {
//                            if (subject != null) {
//                                payload.addMap(subject.getDeviceInfo());
//                                payload.addMap(subject.getAppInfo());
//                                payload.addMap(subject.getSettingProperty());
//                                payload.addMap(subject.getVolatileProperty(mContext));
//                            }
//
//                            if (UsageStatsProxy3.getInstance() != null && UsageStatsProxy3.getInstance().getLocationFetcher() != null) {
//                                LocationFetcher locationFetcher = UsageStatsProxy3.getInstance().getLocationFetcher();
//                                if (locationFetcher != null) {
//                                    Location location = locationFetcher.getLocation();
//                                    if (location != null) {
//                                        payload.add(Parameters.LONGITUDE, location.getLongitude());
//                                        payload.add(Parameters.LATITUDE, location.getLatitude());
//                                        payload.add(Parameters.LOC_TIME, location.getTime());
//                                    } else {
//                                        payload.add(Parameters.LONGITUDE, 0);
//                                        payload.add(Parameters.LATITUDE, 0);
//                                        payload.add(Parameters.LOC_TIME, 0);
//                                    }
//                                }
//                            }
//                            appendRpkInfo(payload, rpkInfo);
//                            rpkEmitterWorker.add(rpkInfo.appKey, rpkInfo.rpkPkgName, payload);
//                        }
//                    }
//
//                    LogUtils.d(TAG, "finished track RpkEmitterWorker");
//                }
//            });
        }

        private void appendRpkInfo(final TrackerPayload payload, final RpkInfo rpkInfo) {
            payload.add(Parameters.PKG_NAME, rpkInfo.apkPkgName); //覆盖Subject中默认的包名
            payload.add(Parameters.PKG_VER, rpkInfo.rpkVer); //覆盖Subject中默认的包版本
            payload.add(Parameters.PKG_VER_CODE, rpkInfo.rpkVerCode); //覆盖Subject中默认的包版本号
            payload.add(Parameters.CHANNEL_ID, "102027"); //魅族快应用渠道
            Map<String, String> event_attrib = new HashMap<>();
            event_attrib.put("rpkPkgName", rpkInfo.rpkPkgName);
            payload.add(Parameters.EVENT_ATTRIB, event_attrib);
        }

    }

}
