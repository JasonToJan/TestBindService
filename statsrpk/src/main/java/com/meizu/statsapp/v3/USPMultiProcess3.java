
package com.meizu.statsapp.v3;

import android.app.Application;

import java.util.Map;

/**
 * 多进程模式入口
 *
 * @author jhui
 */
@Deprecated
public class USPMultiProcess3 {
    private static String TAG = USPMultiProcess3.class.getSimpleName();
    private static USPMultiProcess3 sInstance;
    private static final Object lock = new Object();

    //-------------------- 初始化的api

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
        if (null == sInstance) {
            synchronized (lock) {
                if (null == sInstance) {
                    sInstance = new USPMultiProcess3();
                    UsageStatsProxy3.init(application, pkgType, pkgKey, initConfig);
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
        if (null == sInstance) {
            synchronized (lock) {
                if (null == sInstance) {
                    sInstance = new USPMultiProcess3();
                    UsageStatsProxy3.init(application, pkgType, pkgKey);
                }
            }
        }
    }

    /**
     * 获得事件统计代理对象。
     *
     * @return uspMultiProcess
     */
    public static USPMultiProcess3 getInstance() {
        return sInstance;
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
        if (eventName == null) {
            return;
        }
        UsageStatsProxy3.getInstance().onEvent(eventName, pageName, properties);
    }

    /**
     * 记录一个事件，并立即上报
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
    public void onEventRealtime(final String eventName, final String pageName, final Map<String, String> properties) {
        if (eventName == null) {
            return;
        }
        UsageStatsProxy3.getInstance().onEventRealtime(eventName, pageName, properties);
    }

    /**
     * 记录一个事件， 缓存几秒后立即上报
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
    public void onEventNeartime(final String eventName, final String pageName, final Map<String, String> properties) {
        if (eventName == null) {
            return;
        }
        UsageStatsProxy3.getInstance().onEventNeartime(eventName, pageName, properties);
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
        if (eventName == null) {
            return;
        }
        UsageStatsProxy3.getInstance().onEventLib(eventName, pageName, properties, libPkgName);
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
        if (eventName == null) {
            return;
        }
        UsageStatsProxy3.getInstance().onEventRealtimeLib(eventName, pageName, properties, libPkgName);
    }

    /**
     * 记录日志，2.5.x的老接口，给公共sdk库使用
     *
     * @param logName    日志名称
     * @param properties 日志属性
     */
    public void onLog(final String logName, final Map<String, String> properties) {
        if (logName == null) {
            return;
        }
        UsageStatsProxy3.getInstance().onLog(logName, properties);
    }

    /**
     * 实时记录日志，2.5.x的老接口，给公共sdk库使用
     *
     * @param logName    日志名称
     * @param properties 日志属性
     */
    public void onLogRealtime(final String logName, final Map<String, String> properties) {
        if (logName == null) {
            return;
        }
        UsageStatsProxy3.getInstance().onLogRealtime(logName, properties);
    }

    /**
     * 记录一个后台使用时长
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param duration  持续时长
     */
    @Deprecated
    public void onBackgroundUse(final long startTime, final long endTime, final long duration) {}

    /**
     * 跟踪页面启动。
     *
     * @param pageName 页面名称，不能为空
     */
    public void onPageStart(final String pageName) {
        UsageStatsProxy3.getInstance().onPageStart(pageName);
    }

    /**
     * 跟踪页面退出。
     *
     * @param pageName 页面名称，不能为空
     */
    public void onPageStop(final String pageName) {
        UsageStatsProxy3.getInstance().onPageStop(pageName);
    }

    /**
     * 设置会话开启的来源，当此会话结束后，source会被置空。
     * 用来标示应用被谁拉起。
     *
     * @param source 跳转来源
     */
    public void setSource(final String source) {
        UsageStatsProxy3.getInstance().setSource(source);
    }


    /**
     * 获取当前source
     *
     * @return 当前source
     */
    public String getSource() {
        return UsageStatsProxy3.getInstance().getSource();
    }

    /**
     * 增加公共字段event_attrib
     * 目前只有游戏在用
     *
     * @param attributes 共有属性
     */
    public void setAttributes(final Map<String, String> attributes) {
        UsageStatsProxy3.getInstance().setAttributes(attributes);
    }

    /**
     * 获取当前sessionid
     *
     * @return 当前sessionid
     */
    public String getSessionId() {
        return UsageStatsProxy3.getInstance().getSessionId();
    }

    /**
     * 获取某个页面的停留时长
     *
     * @param pageName 页面名称
     * @return 返回的页面时长，单位毫秒
     */
    public long getPageDuration(String pageName) {
        return 0;//TODO
    }


}
