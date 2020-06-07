
package com.meizu.statsapp.v3;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;

import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.events.EventUtil;
import com.meizu.statsapp.v3.lib.plugin.events.PageEvent;
import com.meizu.statsapp.v3.lib.plugin.page.PageController;
import com.meizu.statsapp.v3.lib.plugin.sdk.SDKInstanceImpl;
import com.meizu.statsapp.v3.lib.plugin.session.SessionController;
import com.meizu.statsapp.v3.lib.plugin.tracker.LocationFetcher;
import com.meizu.statsapp.v3.lib.plugin.utils.FlymeOSUtils;
import com.meizu.statsapp.v3.utils.log.EncryptLogger;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件统计代理类。事件统计SDK入口，提供事件统计API。
 * 单进程中使用UsageStatsProxy3，多进程请使用USPMultiProcess3
 *
 * @author jhui
 */
public class UsageStatsProxy3 {
    private static String TAG = "UsageStatsProxy3";
    private static UsageStatsProxy3 sUsageStatsProxy;
    private static final Object sLock = new Object();
    private Context mContext;
    private String mPkgKey;
    private int mPkgType;
    private SDKInstanceImpl mSDKInstanceImpl;
    private Application mApplication;
    private SessionController mSessionController;
    private PageController mPageController;

    //-------------------- 初始化的api

    private UsageStatsProxy3(Application application,
                             final int pkgType,
                             final String pkgKey,
                             final InitConfig initConfig
    ) {
        if (null == application) {
            throw new IllegalArgumentException("The context is null!");
        }
        if (TextUtils.isEmpty(pkgKey)) {
            throw new IllegalArgumentException("pkgKey is null!");
        }
        if (null == initConfig) {
            throw new IllegalArgumentException("initConfig is null!");
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            throw new IllegalArgumentException("android OS version too low!");
        }
        mApplication = application;
        mContext = mApplication.getBaseContext();
        this.mPkgKey = pkgKey;
        this.mPkgType = pkgType;

        //log to file
        if (Logger.sDebug) {
            File externalFilesDir = mContext.getExternalFilesDir(null);
            if (externalFilesDir != null) {
                Logger.setHook(new EncryptLogger(externalFilesDir.getAbsolutePath()));
            }
        }

        long t1 = System.currentTimeMillis();
        Logger.d(TAG, "##### UsageStatsProxy3 init");

        mSessionController = new SessionController(mApplication.getApplicationContext());
        mPageController = new PageController(mApplication.getApplicationContext());

        if (initConfig.mainThreadInit) {
            realInit();
        } /*else {
            GlobalExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    realInit();
                }
            });
        }*/

        Logger.d(TAG, "##### UsageStatsProxy3 init complete, " + (System.currentTimeMillis() - t1));
    }

    private void realInit() {
        long t1 = System.currentTimeMillis();
        Logger.d(TAG, "##### UsageStatsProxy3 realInit 1, " + (System.currentTimeMillis() - t1));
        mSDKInstanceImpl = new SDKInstanceImpl(mContext, mPkgType, mPkgKey);
        try {
            deleteDirectory(mContext.getDir("mz_statsapp_v3_base", Context.MODE_PRIVATE));
            deleteDirectory(mContext.getDir("mz_statsapp_v3_dex", Context.MODE_PRIVATE));
            deleteDirectory(mContext.getDir("mz_statsapp_v3_patch", Context.MODE_PRIVATE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSessionController.attach(mSDKInstanceImpl);
        mSDKInstanceImpl.attach(mSessionController);
        Logger.d(TAG, "##### UsageStatsProxy3 realInit 2, " + (System.currentTimeMillis() - t1));

    }

    private static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    /**
     * 初始化埋点sdk。
     *
     * @param application app实例
     * @param pkgType     0/NULL:默认值，表示APP ；1:代理游戏； 2：flymeTV； 3:PAD
     * @param pkgKey      appKey，从统一上报平台获取
     * @param initConfig  初始化配置
     */
    public static void init(Application application,
                            PkgType pkgType,
                            String pkgKey,
                            InitConfig initConfig) {
        if (null == sUsageStatsProxy) {
            synchronized (sLock) {
                if (null == sUsageStatsProxy) {
                    sUsageStatsProxy = new UsageStatsProxy3(application, pkgType.value(), pkgKey, initConfig);
                }
            }
        }
    }

    /**
     * 初始化埋点sdk，使用默认配置。
     *
     * @param application app实例
     * @param pkgType     0/NULL:默认值，表示APP ；1:代理游戏； 2：flymeTV； 3:PAD
     * @param pkgKey      appKey，从统一上报平台获取
     */
    public static void init(Application application,
                            PkgType pkgType,
                            String pkgKey) {
        if (null == sUsageStatsProxy) {
            synchronized (sLock) {
                if (null == sUsageStatsProxy) {
                    InitConfig initConfig = new InitConfig(); //使用默认配置
                    sUsageStatsProxy = new UsageStatsProxy3(application, pkgType.value(), pkgKey, initConfig);
                }
            }
        }
    }

    /**
     * 获得事件统计代理对象。
     *
     * @return UsageStatsProxy
     */
    public static UsageStatsProxy3 getInstance() {
        if (sUsageStatsProxy == null) {
            throw new IllegalStateException("UsageStatsProxy3 is not initialised - invoke at least once with parameterised init");
        }
        return sUsageStatsProxy;
    }

    //-------------------- 对外提供的api

    /**
     * 记录一个事件
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
    public void onEvent(final String eventName, final String pageName, final Map<String, String> properties) {
        if (TextUtils.isEmpty(eventName)) {
            return;
        }
        GlobalExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mSDKInstanceImpl == null) {
                    Logger.w(TAG, "onEvent, sdkInstanceImpl is NULL!");
                    realInit();
                }
                mSDKInstanceImpl.onEvent(eventName, pageName, properties);
            }
        });
    }

    /**
     * 记录一个事件，并立即上报
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
    public void onEventRealtime(final String eventName, final String pageName, final Map<String, String> properties) {
        if (TextUtils.isEmpty(eventName)) {
            return;
        }
        GlobalExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mSDKInstanceImpl == null) {
                    Logger.w(TAG, "onEventRealtime, sdkInstanceImpl is NULL!");
                    realInit();
                }
                mSDKInstanceImpl.onEventRealtime(eventName, pageName, properties);
            }
        });
    }

    /**
     * 记录一个事件， 缓存几秒后立即上报
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
    public void onEventNeartime(final String eventName, final String pageName, final Map<String, String> properties) {
        if (TextUtils.isEmpty(eventName)) {
            return;
        }
        GlobalExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mSDKInstanceImpl == null) {
                    Logger.w(TAG, "onEventNeartime, sdkInstanceImpl is NULL!");
                    realInit();
                }
                mSDKInstanceImpl.onEventNeartime(eventName, pageName, properties);
            }
        });
    }

    /**
     * 记录日志，2.5.x的老接口，给公共sdk库, 系统框架等使用
     *
     * @param logName    日志名称
     * @param properties 日志属性
     */
    public void onLog(final String logName, final Map<String, String> properties) {
        if (TextUtils.isEmpty(logName)) {
            return;
        }
        GlobalExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mSDKInstanceImpl == null) {
                    Logger.w(TAG, "onLog, sdkInstanceImpl is NULL!");
                    realInit();
                }
                mSDKInstanceImpl.onLog(logName, properties);
            }
        });
    }

    /**
     * 实时记录日志，2.5.x的老接口，给公共sdk库使用
     *
     * @param logName    日志名称
     * @param properties 日志属性
     */
    public void onLogRealtime(final String logName, final Map<String, String> properties) {
        if (TextUtils.isEmpty(logName)) {
            return;
        }
        GlobalExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mSDKInstanceImpl == null) {
                    Logger.w(TAG, "onLogRealtime, sdkInstanceImpl is NULL!");
                    realInit();
                }
                mSDKInstanceImpl.onLogRealtime(logName, properties);
            }
        });
    }

    /**
     * 记录一个公共sdk库事件
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     * @param libPkgName 公共sdk库的包名
     */
    public void onEventLib(final String eventName, final String pageName, final Map<String, String> properties, final String libPkgName) {
        if (TextUtils.isEmpty(eventName)) {
            return;
        }
        GlobalExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mSDKInstanceImpl == null) {
                    Logger.w(TAG, "onEventLib, sdkInstanceImpl is NULL!");
                    realInit();
                }
                Map<String, Object> replaceMap = new HashMap<>();
                replaceMap.put(Parameters.PKG_NAME, libPkgName);
                mSDKInstanceImpl.onEventX(eventName, pageName, properties, replaceMap);
            }
        });
    }

    /**
     * 记录一个公共sdk库事件
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     * @param libPkgName 公共sdk库的包名
     */
    public void onEventRealtimeLib(final String eventName, final String pageName, final Map<String, String> properties, final String libPkgName) {
        if (TextUtils.isEmpty(eventName)) {
            return;
        }
        GlobalExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mSDKInstanceImpl == null) {
                    Logger.w(TAG, "onEventRealtimeLib, sdkInstanceImpl is NULL!");
                    realInit();
                }
                Map<String, Object> replaceMap = new HashMap<>();
                replaceMap.put(Parameters.PKG_NAME, libPkgName);
                mSDKInstanceImpl.onEventRealtimeX(eventName, pageName, properties, replaceMap);
            }
        });
    }

    /**
     * 记录一个框架事件
     *
     * @param eventName      事件名称
     * @param pageName       事件发生的页面，可以为空
     * @param properties     事件的属性，可以为空
     * @param virtualPkgName 业务的虚拟包名
     */
    public void onEventFramework(final String eventName, final String pageName, final Map<String, String> properties,
                                 final String virtualPkgName, final String virtualPkgVer, final int virtualPkgVerCode) {
        if (TextUtils.isEmpty(eventName)) {
            return;
        }
        GlobalExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mSDKInstanceImpl == null) {
                    Logger.w(TAG, "onEventFramework, sdkInstanceImpl is NULL!");
                    realInit();
                }
                Map<String, Object> replaceMap = new HashMap<>();
                replaceMap.put(Parameters.PKG_NAME, virtualPkgName);
                replaceMap.put(Parameters.PKG_VER, virtualPkgVer);
                replaceMap.put(Parameters.PKG_VER_CODE, virtualPkgVerCode);
                mSDKInstanceImpl.onEventX(eventName, pageName, properties, replaceMap);
            }
        });
    }

    /**
     * 记录一个实时框架事件
     *
     * @param eventName      事件名称
     * @param pageName       事件发生的页面，可以为空
     * @param properties     事件的属性，可以为空
     * @param virtualPkgName 业务的虚拟包名
     */
    public void onEventRealtimeFramework(final String eventName, final String pageName, final Map<String, String> properties,
                                         final String virtualPkgName, final String virtualPkgVer, final int virtualPkgVerCode) {
        if (TextUtils.isEmpty(eventName)) {
            return;
        }
        GlobalExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mSDKInstanceImpl == null) {
                    Logger.w(TAG, "onEventRealtimeFramework, sdkInstanceImpl is NULL!");
                    realInit();
                }
                Map<String, Object> replaceMap = new HashMap<>();
                replaceMap.put(Parameters.PKG_NAME, virtualPkgName);
                replaceMap.put(Parameters.PKG_VER, virtualPkgVer);
                replaceMap.put(Parameters.PKG_VER_CODE, virtualPkgVerCode);
                mSDKInstanceImpl.onEventRealtimeX(eventName, pageName, properties, replaceMap);
            }
        });
    }

    /**
     * 记录一个后台使用时长
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param duration  持续时长
     */
    @Deprecated
    public void onBackgroundUse(final long startTime, final long endTime, final long duration) {
    }

    /**
     * 跟踪页面启动。
     *
     * @param pageName 页面名称，不能为空
     */
    public void onPageStart(final String pageName) {
        if (TextUtils.isEmpty(pageName)) {
            return;
        }
        mPageController.startPage(pageName);
    }

    /**
     * 跟踪页面退出。
     *
     * @param pageName 页面名称，不能为空
     */
    public void onPageStop(final String pageName) {
        onPageStop(pageName, null);
    }

    /**
     * 跟踪页面退出。
     *
     * @param pageName 页面名称，不能为空
     * @param properties 事件的属性，可以为空,默认使用单个参数的onPageStop。如若使用，不能在properties中添加key为duration2的数据，因为该字段已经被使用
     */
    public void onPageStop(final String pageName, final Map<String, String> properties) {
        if (TextUtils.isEmpty(pageName)) {
            return;
        }
        final PageController.Page page = mPageController.stopPage(pageName);
        if (page != null) {
            GlobalExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (mSDKInstanceImpl == null) {
                        Logger.w(TAG, "onPageStop, sdkInstanceImpl is NULL!");
                        realInit();
                    }
                    long currentTime = System.currentTimeMillis();
                    long currentElapse = SystemClock.elapsedRealtime();
                    final PageEvent pageEvent = EventUtil.buildPageEvent(mContext,
                            pageName,
                            String.valueOf(page.time),
                            String.valueOf(currentTime));
                    Map<String, String> prop;
                    if (properties != null) {
                        prop = properties;
                    } else {
                        prop = new HashMap<>();
                    }
                    prop.put("duration2", String.valueOf(currentElapse - page.elapse));
                    pageEvent.setProperties(prop);
                    mSDKInstanceImpl.getTracker().track(pageEvent);
                }
            });

        }
    }

    /**
     * 设置会话开启的来源，当此会话结束后，source会被置空。
     * 用来标示应用被谁拉起。
     *
     * @param source 跳转来源
     */
    public void setSource(final String source) {
        GlobalExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mSDKInstanceImpl == null) {
                    Logger.w(TAG, "setSource, sdkInstanceImpl is NULL!");
                    realInit();
                }
                mSessionController.setSource(source);
            }
        });
    }

    /**
     * 获取当前source
     *
     * @return 当前source
     */
    public String getSource() {
        return mSessionController.getSource();
    }

    /**
     * 增加公共字段event_attrib
     *
     * @param attributes 共有属性
     */
    public void setAttributes(final Map<String, String> attributes) { //attributes为空表示清除
        GlobalExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mSDKInstanceImpl == null) {
                    Logger.w(TAG, "setAttributes, sdkInstanceImpl is NULL!");
                    realInit();
                }
                mSDKInstanceImpl.setEventAttributes(attributes);
            }
        });
    }

    /**
     * 获取当前sessionid
     *
     * @return 当前sessionid
     */
    public String getSessionId() {
        return mSessionController.getSessionId();
    }

    /**
     * get umid
     */
    public String getUMID() {
        if (mSDKInstanceImpl == null) {
            Logger.w(TAG, "getUMID, sdkInstanceImpl is NULL!");
            realInit();
        }
        return mSDKInstanceImpl.getUMID();
    }

    /**
     * get uid via JS
     */
    public String getFlymeUID() {
        if (FlymeOSUtils.isBrandMeizu()) {
            return FlymeOSUtils.getFlymeUid(mApplication);
        }
        return "";
    }

    /**
     * 获取某个页面的停留时长
     *
     * @param pageName 页面名称
     * @return 返回的页面时长，单位毫秒
     */
    public long getPageDuration(String pageName) {
        return mPageController.getPageDuration(pageName);
    }

    /**
     * 获取经纬度读取器
     */
    public LocationFetcher getLocationFetcher() {
        if (mSDKInstanceImpl == null) {
            Logger.w(TAG, "getLocationFetcher, sdkInstanceImpl is NULL!");
            return null;
        }
        return mSDKInstanceImpl.getLocationFetcher();
    }

    /**
     * 获取SDKInstanceImpl实例
     */
    public SDKInstanceImpl getSdkInstanceImpl() {
        return mSDKInstanceImpl;
    }

}
