
package com.meizu.statsapp;

import android.content.Context;

import com.meizu.statsapp.v3.UsageStatsProxy3;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * v3兼容v2的老接口，要逐步废弃
 *
 * @author jhui
 *
 * @deprecated in V3
 */
@Deprecated
public class UsageStatsProxy {

    private static final String TAG = "UsageStatsProxy";
    private static UsageStatsProxy sUsageStatsProxy;
    private static Object sLock = new Object();

    private UsageStatsProxy(Context context, boolean online, boolean upload) {

    }

    private static UsageStatsProxy getInstance(Context context, boolean online, boolean upload) {
        if (null == sUsageStatsProxy) {
            synchronized (sLock) {
                if (null == sUsageStatsProxy) {
                    sUsageStatsProxy = new UsageStatsProxy(context, online, upload);
                }
            }
        }
        return sUsageStatsProxy;
    }

    private static void _WARNING_() {
        Logger.w(TAG, "_WARNING_, DO NOT USE STATSAPP V2 INTERFACE IN V3!");
        //Log.w(TAG, "_WARNING_, DO NOT USE STATSAPP V2 INTERFACE IN V3!");
        //"333".split(",")[2].toString(); //cause crash
    }

    /**
     * 获得事件统计代理对象
     *
     * @param context 上下文对象
     * @param online  true为联网应用，false为非联网应用
     * @return 事件统计代理对象
     *
     */
    public static UsageStatsProxy getInstance(Context context, boolean online) {
        _WARNING_();
        if (null == sUsageStatsProxy) {
            synchronized (sLock) {
                if (null == sUsageStatsProxy) {
                    sUsageStatsProxy = new UsageStatsProxy(context, online, true);
                }
            }
        }
        return sUsageStatsProxy;
    }

    /**
     * 获得联网应用事件统计代理对象。
     *
     * @param context 上下文
     * @param upload  true上传数据，false不上传数据
     * @return 联网应用事件统计代理对象
     */
    public static UsageStatsProxy getOnlineInstance(Context context, boolean upload) {
        _WARNING_();
        return getInstance(context, true, upload);
    }

    /**
     * 获得非联网应用事件统计代理对象。
     *
     * @param context 上下文
     */
    public static UsageStatsProxy getOfflineInstance(Context context) {
        _WARNING_();
        return getInstance(context, false, true);
    }

    /**
     * 获得联网应用事件统计代理对象。
     *
     * @param context 上下文
     * @param upload true上传数据，false不上传数据
     * @param packageName 自定义指定的包名
     * @param hbInterval 心跳间隔
     * @param pkgType 应用类型
     * @return 联网应用事件统计代理对象
     */
    public static UsageStatsProxy getOnlineInstance(Context context, boolean upload, String packageName, long hbInterval, int pkgType) {
        _WARNING_();
        return getOnlineInstance(context, upload);
    }

    /**
     * 获得联网应用事件统计代理对象。
     *
     * @param context 上下文
     * @param packageName 自定义指定的包名
     * @param hbInterval 心跳间隔
     * @param pkgType 应用类型
     * @return 联网应用事件统计代理对象
     */
    public static UsageStatsProxy getOfflineInstance(Context context, String packageName, long hbInterval, int pkgType) {
        _WARNING_();
        return getOfflineInstance(context);
    }

    /**
     * 设置是否上传数据。只对联网应用有效。
     *
     * @param upload true上传数据，false不上传数据
     */
    public void setUploaded(boolean upload) {
        _WARNING_();
    }

    public void setOnline(boolean online) {
        _WARNING_();
    }

    public void setAttributes(Map<String, String> attributes) {
        _WARNING_();
        UsageStatsProxy3.getInstance().setAttributes(attributes);
    }

    /**
     * 跟踪页面启动。
     *
     * @param pageName 页面名称，不能为空
     */
    public void onPageStart(String pageName) {
        _WARNING_();
        UsageStatsProxy3.getInstance().onPageStart(pageName);
    }

    /**
     * 跟踪页面退出。
     *
     * @param pageName 页面名称，不能为空
     */
    public void onPageStop(String pageName) {
        _WARNING_();
        UsageStatsProxy3.getInstance().onPageStop(pageName);
    }

    /**
     * 记录一个事件。
     *
     * @param eventName 事件名称
     * @param pageName  事件发生的页面，可以为空
     * @param property  事件的属性，可以为空
     */
    public void onEvent(String eventName, String pageName, String property) {
        _WARNING_();
        Map<String, String> properties = new HashMap<>();
        properties.put("value", property);
        UsageStatsProxy3.getInstance().onEvent(eventName, pageName, properties);
    }

    /**
     * 记录一个事件。
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
    public void onEvent(String eventName, String pageName, Map<String, String> properties) {
        _WARNING_();
        UsageStatsProxy3.getInstance().onEvent(eventName, pageName, properties);
    }

    /**
     * 在联网应用中，记录一个事件并直接上报数据。
     * 在非联网应用中和onEvent(String eventName, String pageName, Map<String, String> properties)方法一样。
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
    public void onEventRealtime(String eventName, String pageName, Map<String, String> properties) {
        _WARNING_();
        UsageStatsProxy3.getInstance().onEventRealtime(eventName, pageName, properties);
    }

    /**
     * 记录日志。
     *
     * @param logName    日志名称
     * @param properties 日志属性
     */
    public void onLog(String logName, Map<String, String> properties) {
        _WARNING_();
        UsageStatsProxy3.getInstance().onLog(logName, properties);
    }

    /**
     * 记录日志。(实时上报)
     *
     * @param logName    日志名称
     * @param properties 日志属性
     */
    public void onLogRealtime(String logName, Map<String, String> properties) {
        _WARNING_();
        UsageStatsProxy3.getInstance().onLogRealtime(logName, properties);
    }

    public String getSessionId() {
        _WARNING_();
        return UsageStatsProxy3.getInstance().getSessionId();
    }

    public void setSource(String source) {
        _WARNING_();
        UsageStatsProxy3.getInstance().setSource(source);
    }

    public String getSource() {
        _WARNING_();
        return UsageStatsProxy3.getInstance().getSource();
    }

    public void setBulkLimit(int bulkLimit) {
        _WARNING_();
    }

    /**
     * get umid
     *
     */
    public String getUMID() {
        _WARNING_();
        return UsageStatsProxy3.getInstance().getUMID();
    }

}
