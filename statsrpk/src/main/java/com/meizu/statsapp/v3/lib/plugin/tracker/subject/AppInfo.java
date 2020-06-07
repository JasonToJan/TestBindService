package com.meizu.statsapp.v3.lib.plugin.tracker.subject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huchen on 16-8-1.
 */
class AppInfo {
    private final static String TAG = "AppInfo";
    private HashMap<String, Object> appPairs = new HashMap<>();

    private AppInfo(AppInfoBuilder builder) {
        setChannelId(builder.context);

        if (builder.context != null) {
            setContextualParams(builder.context);
        }

        Logger.v(TAG, "AppInfo created successfully.");
    }

    /**
     * Builder for the DeviceInfo
     */
    public static class AppInfoBuilder {
        private Context context = null; // Optional

        /**
         * @param context The android context to pass to the subject
         * @return itself
         */
        public AppInfoBuilder context(Context context) {
            this.context = context;
            return this;
        }

        /**
         * Creates a new Subject
         *
         * @return a new Subject object
         */
        public AppInfo build() {
            return new AppInfo(this);
        }
    }

    /**
     * Sets the contextually based parameters.
     *
     * @param context the android context
     */
    public void setContextualParams(Context context) {
    }

    private void setChannelId(Context context) {
        add(Parameters.CHANNEL_ID, getChannelId(context));
    }

    /**
     * @param context 解析manifest中配置的需要统计的渠道号
     */
    private String getChannelId(Context context) {
        PackageManager pm = context.getPackageManager();
        if (null != pm) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                if (null != appInfo && null != appInfo.metaData) {
                    return null == appInfo.metaData.get("uxip_channel_num")
                            ? "0" : String.valueOf(appInfo.metaData.get("uxip_channel_num"));
                }
            } catch (PackageManager.NameNotFoundException e) {
                Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
            }
        }
        return "0";
    }

    /**
     * Inserts a value into the mobilePairs
     * subject storage.
     * <p>
     * NOTE: Avoid putting null or empty
     * values in the map
     *
     * @param key   a key value
     * @param value the value associated with
     *              the key
     */
    private void add(String key, String value) {
        if (key != null && !key.isEmpty() && value != null) {
            this.appPairs.put(key, value);
        }
    }

    /**
     * Inserts a value into the devicesPairs
     * subject storage.
     * <p>
     * NOTE: Avoid putting null or empty
     * values in the map
     *
     * @param key   a key value
     * @param value the value associated with
     *              the key
     */
    private void add(String key, Object value) {
        if (key != null && !key.isEmpty() && value != null) {
            this.appPairs.put(key, value);
        }
    }

    public Map getMap() {
        return appPairs;
    }
}
