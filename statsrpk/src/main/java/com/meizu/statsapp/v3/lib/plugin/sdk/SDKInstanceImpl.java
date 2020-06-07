package com.meizu.statsapp.v3.lib.plugin.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.meizu.statsapp.v3.InitConfig;
import com.meizu.statsapp.v3.SdkVer;
import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.lib.plugin.emitter.Emitter;
import com.meizu.statsapp.v3.lib.plugin.emitter.local.LocalEmitter;
import com.meizu.statsapp.v3.lib.plugin.emitter.remote.V3OfflineEmitter;
import com.meizu.statsapp.v3.lib.plugin.events.EventUtil;
import com.meizu.statsapp.v3.lib.plugin.events.LogEvent;
import com.meizu.statsapp.v3.lib.plugin.session.SessionController;
import com.meizu.statsapp.v3.lib.plugin.tracker.LocationFetcher;
import com.meizu.statsapp.v3.lib.plugin.tracker.Tracker;
import com.meizu.statsapp.v3.lib.plugin.tracker.subject.Subject;
import com.meizu.statsapp.v3.lib.plugin.utils.PermissionUtils;
import com.meizu.statsapp.v3.utils.CommonUtils;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huchen on 16-8-29.
 */
public class SDKInstanceImpl {
    private static final String TAG = "SDKInstanceImpl";
    private Context applicationContext;

    private String pkgKey;
    private Emitter emitter;
    private Tracker tracker;
    private ConfigController configController;
    private LocationFetcher locationFetcher;
    private SessionController sessionController;

    public SDKInstanceImpl(Context context,
                           int pkgType,
                           String pkgKey) {
        long t1 = System.currentTimeMillis();
        if (null == context) {
            throw new IllegalArgumentException("The applicationContext is null!");
        }
        applicationContext = context.getApplicationContext();
        Logger.d(TAG, "##### pkgKey: " + pkgKey + ", pkgType: " + pkgType
                + ", initConfig: " + (new InitConfig()).toString() + ", sdkVersion: " + SdkVer.verName);
        this.pkgKey = pkgKey;
        this.configController = new ConfigController(applicationContext, pkgKey);
        Logger.d(TAG, "##### SDKInstanceImpl 1, " + (System.currentTimeMillis() - t1));
        locationFetcher = new LocationFetcher(applicationContext);
        emitter = buildEmitter(applicationContext, pkgKey);
        Logger.d(TAG, "##### SDKInstanceImpl 2, " + (System.currentTimeMillis() - t1));
        Subject subject = buildSubject(applicationContext, pkgKey, pkgType, SdkVer.verName);
        Logger.d(TAG, "##### SDKInstanceImpl 3, " + (System.currentTimeMillis() - t1));
        tracker = buildTrack(emitter, subject, applicationContext);
        Logger.d(TAG, "##### SDKInstanceImpl 4, " + (System.currentTimeMillis() - t1));
        init();
        Logger.d(TAG, "##### SDKInstanceImpl 5, " + (System.currentTimeMillis() - t1));

        if (InitConfig.useInternationalDomain) {
            Logger.d(TAG,"Switch international domain.");
            UxipConstants.UPLOAD_URL = "http://uxip.in.meizu.com/api/v3/event/";
            UxipConstants.GET_UMID_URL = "http://uxip-config.in.meizu.com/api/v3/umid";
            UxipConstants.GET_CONFIG_URL = "http://uxip-res.in.meizu.com/resource/v3/config/";
        }
    }

    private void init() {
        locationFetcher.setEnable(InitConfig.reportLocation);
        configController.init(this);
        emitter.init();
        tracker.init(this);
        //是否生成_bootup_
        if (!InitConfig.noBootUp) {
            Map<String, String> properties = new HashMap<>();
            properties.put("daily_actived", String.valueOf(getDailyActived(applicationContext)));
            properties.put("global_actived", String.valueOf(getGlobalActived(applicationContext)));
            tracker.track(EventUtil.buildActionXEvent(applicationContext, "_bootup_", null, properties), 0);
        }
    }

    public String getPkgKey() {
        return pkgKey;
    }

    public Emitter getEmitter() {
        return emitter;
    }

    public Tracker getTracker() {
        return tracker;
    }

    public LocationFetcher getLocationFetcher() {
        return locationFetcher;
    }

    public void attach(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public String getUMID() {
        if (emitter != null) {
            return emitter.getUMID();
        }
        return "";
    }

    /**
     * Returns a Tracker
     *
     * @param emitter a Classic emitter
     * @param subject the tracker subject
     * @return a new Classic Tracker
     */
    private Tracker buildTrack(Emitter emitter, Subject subject, Context context) {
        boolean debug = CommonUtils.isDebugMode(context);
        return new Tracker.TrackerBuilder(emitter, context)
                .subject(subject)
                .debug(debug)
                .build();
    }

    /**
     * Returns a Local Emitter
     *
     * @param context the application applicationContext
     * @return a new Classic Emitter
     */
    private Emitter buildEmitter(Context context, String pkgKey) {
        boolean offline = InitConfig.offline;
        boolean noEncrypt = InitConfig.noEncrypt;
        if (CommonUtils.isDebugMode(context)) {
            noEncrypt = true;
        }
        Emitter emitter;
        if (InitConfig.useInternationalDomain) {
            PermissionUtils.checkInternetPermission(context);
            emitter = new LocalEmitter(applicationContext, pkgKey);
            emitter.setEncrypt(!noEncrypt);
            return emitter;
        }
        if (offline) {
            if (findDataService()) {
                emitter = new V3OfflineEmitter(applicationContext, pkgKey);
                emitter.setEncrypt(!noEncrypt);
                return emitter;
            }
            /*else if (findExperienceDataSync()) {
                emitter = new V2ProviderEmitter(applicationContext, pkgKey);
                return emitter;
            }*/
        }
        //其他默认情况下工作在独立模式
        PermissionUtils.checkInternetPermission(context);
        emitter = new LocalEmitter(applicationContext, pkgKey);
        emitter.setEncrypt(!noEncrypt);
        return emitter;
    }

    /**
     * Returns a Subject Object
     *
     * @param context the application applicationContext
     * @return a new subject
     */
    private Subject buildSubject(Context context, String pkgKey, int pkgType, String sdkVersion) {
        String replacePackage = InitConfig.replacePackage;
        if (TextUtils.isEmpty(replacePackage)) {
            return new Subject.SubjectBuilder()
                    .context(context)
                    .pkgKey(pkgKey)
                    .pkgType(pkgType)
                    .sdkVersion(sdkVersion).build();

        } else {
            return new Subject.SubjectBuilder()
                    .context(context)
                    .pkgKey(pkgKey)
                    .pkgType(pkgType)
                    .sdkVersion(sdkVersion)
                    .replacePackage(replacePackage)
                    .build();
        }
    }

    /**
     * 记录一个事件。
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
    public void onEvent(final String eventName, final String pageName, final Map<String, String> properties) {
        Logger.d(TAG, "onEvent eventName: " + eventName + ", pageName: " + pageName + ", properties: " + properties);
        if (null == tracker || TextUtils.isEmpty(eventName)) {
            return;
        }
        tracker.track(EventUtil.buildActionXEvent(applicationContext, eventName, pageName, properties));
    }

    /**
     * 记录一个事件，并立即上报
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
    public void onEventRealtime(final String eventName, final String pageName, final Map<String, String> properties) {
        Logger.d(TAG, "onEventRealtime eventName: " + eventName + ", pageName: " + pageName + ", properties: " + properties);
        if (null == tracker || TextUtils.isEmpty(eventName)) {
            return;
        }
        tracker.track(EventUtil.buildActionXEvent(applicationContext, eventName, pageName, properties), UxipConstants.SEND_REALTIME);
    }

    public void onEventNeartime(final String eventName, final String pageName, final Map<String, String> properties) {
        Logger.d(TAG, "onEventNeartime eventName: " + eventName + ", pageName: " + pageName + ", properties: " + properties);
        if (null == tracker || TextUtils.isEmpty(eventName)) {
            return;
        }
        tracker.track(EventUtil.buildActionXEvent(applicationContext, eventName, pageName, properties), UxipConstants.SEND_NEARTIME);
    }

    /**
     * 记录日志。
     *
     * @param logName    日志名称
     * @param properties 日志属性
     */
    public void onLog(final String logName, final Map<String, String> properties) {
        Logger.d(TAG, "onLog logName: " + logName + ", properties: " + properties);
        if (null == tracker || TextUtils.isEmpty(logName)) {
            return;
        }
        Map<String, Object> replaceMap = new HashMap<>();
        replaceMap.put(Parameters.PKG_NAME, LogEvent.LOG_PACKAGE);
        tracker.trackX(EventUtil.buildLogEvent(applicationContext, logName, properties), UxipConstants.SEND_NORMA, replaceMap);
    }

    /**
     * 记录日志。
     *
     * @param logName    日志名称
     * @param properties 日志属性
     */
    public void onLogRealtime(final String logName, final Map<String, String> properties) {
        Logger.d(TAG, "onLogRealtime logName: " + logName + ", properties: " + properties);
        if (null == tracker || TextUtils.isEmpty(logName)) {
            return;
        }
        Map<String, Object> replaceMap = new HashMap<>();
        replaceMap.put(Parameters.PKG_NAME, LogEvent.LOG_PACKAGE);
        tracker.trackX(EventUtil.buildLogEvent(applicationContext, logName, properties), UxipConstants.SEND_REALTIME, replaceMap);
    }

    /**
     * 特殊事件，覆盖某几个顶级参数
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     * @param replaceMap 要覆盖的参数，可以为空
     */
    public void onEventX(final String eventName, final String pageName, final Map<String, String> properties, Map<String, Object> replaceMap) {
        Logger.d(TAG, "onEventX eventName: " + eventName + ", pageName: " + pageName + ", properties: " + properties);
        if (null == tracker || TextUtils.isEmpty(eventName)) {
            return;
        }
        tracker.trackX(EventUtil.buildActionXEvent(applicationContext, eventName, pageName, properties), UxipConstants.SEND_NORMA, replaceMap);
    }

    /**
     * 实时特殊事件，覆盖某几个顶级参数
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     * @param replaceMap 要覆盖的参数，可以为空
     */
    public void onEventRealtimeX(final String eventName, final String pageName, final Map<String, String> properties, Map<String, Object> replaceMap) {
        Logger.d(TAG, "onEventX eventName: " + eventName + ", pageName: " + pageName + ", properties: " + properties);
        if (null == tracker || TextUtils.isEmpty(eventName)) {
            return;
        }
        tracker.trackX(EventUtil.buildActionXEvent(applicationContext, eventName, pageName, properties), UxipConstants.SEND_REALTIME, replaceMap);
    }

    /**
     * 增加公共字段event_attrib
     *
     * @param attributes 共有属性
     */
    public void setEventAttributes(Map<String, String> attributes) {
        Logger.d(TAG, "setEventAttributes attributes: " + attributes);
        if (null == tracker || null == tracker.getSubject()) {
            return;
        }
        if (attributes != null) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                tracker.getSubject().addEventAttributePairs(entry.getKey(), entry.getValue());
            }
        } else {
            tracker.getSubject().clearEventAttributePairs();
        }

    }


    /**
     * offline模式在过渡阶段的新老固件兼容
     */
    private boolean findDataService() {
        PackageManager pm = applicationContext.getPackageManager();
        Intent mainIntent = new Intent("com.meizu.dataservice.action.vccOfflineStats");
        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(mainIntent, PackageManager.GET_RESOLVED_FILTER);
        Logger.d(TAG, "queryIntentServices: " + resolveInfos);
        if (resolveInfos != null) {
            for (ResolveInfo resolveInfo : resolveInfos) {
                String pkgName = resolveInfo.serviceInfo.packageName; // 获得应用程序的包名
                String serviceName = resolveInfo.serviceInfo.name; // 获得该应用程序的启动Activity的name
                if ("com.meizu.dataservice".equals(pkgName)) {
                    Logger.d(TAG, "choose serviceName---" + serviceName + " pkgName---" + pkgName);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean findExperienceDataSync() {
        for (PackageInfo pack : applicationContext.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS)) {
            ProviderInfo[] providers = pack.providers;
            if (providers != null) {
                for (ProviderInfo provider : providers) {
                    if (provider != null && provider.authority != null && provider.authority.equals("com.meizu.usagestats")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int getDailyActived(Context context) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        String today = dateFormat.format(date);
        SharedPreferences sp = context.getSharedPreferences(UxipConstants.PREFERENCES_COMMON_NAME, Context.MODE_PRIVATE);
        String last = sp.getString(UxipConstants.PREFERENCES_KEY_DAILY_ACTIVED_LAST, "");
        Logger.d(TAG, "beforeGetDailyActived ------------------ current states: today:" + today + ", last:" + last);
        if (!today.equals(last)) {
            Logger.i(TAG, "a new day");
            sp.edit().putString(UxipConstants.PREFERENCES_KEY_DAILY_ACTIVED_LAST, today).apply();
            return 1;
        }
        return 0;
    }

    private int getGlobalActived(Context context) {
        SharedPreferences sp = context.getSharedPreferences(UxipConstants.PREFERENCES_COMMON_NAME, Context.MODE_PRIVATE);
        boolean actived = sp.getBoolean(UxipConstants.PREFERENCES_KEY_GLOBAL_ACTIVED, true);
        if (actived) {
            sp.edit().putBoolean(UxipConstants.PREFERENCES_KEY_GLOBAL_ACTIVED, false).apply();
        }
        return actived ? 1 : 0;
    }
}
