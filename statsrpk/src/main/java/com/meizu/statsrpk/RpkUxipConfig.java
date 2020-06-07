package com.meizu.statsrpk;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.lib.plugin.tracker.EventFilter;
import com.meizu.statsapp.v3.utils.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jinhui on 18-8-8.
 */

public class RpkUxipConfig {
    private static final String TAG = "RpkUxipConfig";

    /**
     * 从服务端返回的response中解析rpkinfo信息，并保存到statsrpk_config_***(rpkPkgName)中，因此该方法需要在异步线程中执行
     * @param context
     * @param response
     * @param rpkInfo
     * @throws JSONException
     */
    public static void parseAppInfo(Context context, String response, RpkInfo rpkInfo) throws JSONException {
        Logger.d(TAG, "parseAppInfo");
        JSONObject jsonObject = new JSONObject(response);

        SharedPreferences sp = context.getSharedPreferences(RpkConstants.SP_FILE_RPK_CONFIG_PREFIX + rpkInfo.rpkPkgName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (!TextUtils.isEmpty(jsonObject.getString("key"))) {
            rpkInfo.appKey = jsonObject.getString("key");
            editor.putString("appKey", rpkInfo.appKey);
        }
        if (!TextUtils.isEmpty(jsonObject.getString("primaryPackageName"))) {
            rpkInfo.apkPkgName = jsonObject.getString("primaryPackageName");
            editor.putString("apkPkgName", rpkInfo.apkPkgName);
        }

        editor.commit();
    }

    /**
     * 从从服务端返回的response中解析config信息，并保存到statsrpk_config_***(rpkPkgName)中，因此该方法需要在异步线程中执行
     * @param context
     * @param response
     * @param rpkInfo
     * @throws JSONException
     */
    public static void parseConfig(Context context, String response, RpkInfo rpkInfo) throws JSONException {
        Logger.d(TAG, "parseConfig");
        JSONObject jsonObject = new JSONObject(response);

        SharedPreferences sp = context.getSharedPreferences(RpkConstants.SP_FILE_RPK_CONFIG_PREFIX + rpkInfo.rpkPkgName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        //版本号
        int version = jsonObject.getInt(UxipConstants.RESPONSE_KEY_VERSION);
        editor.putInt("version", version);

        //上报策略
        boolean active = jsonObject.getBoolean(UxipConstants.RESPONSE_KEY_ACTIVE);
        editor.putBoolean("active", active);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String getTime = dateFormat.format(new Date());
        editor.putString(UxipConstants.PREFERENCES_KEY_GET_TIME, getTime);

        editor.commit();

        //事件过滤
        Map<String, EventFilter> eventFilterMap = new HashMap<>();
        JSONArray events = jsonObject.getJSONArray(UxipConstants.RESPONSE_KEY_EVENTS);
        String event_filters = "";
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.getJSONObject(i);
            String eventName = event.getString(UxipConstants.RESPONSE_KEY_EVENTS_NAME);
            boolean eventActive = event.getBoolean(UxipConstants.RESPONSE_KEY_EVENTS_ACTIVE);
            boolean eventRealtime = event.getBoolean(UxipConstants.RESPONSE_KEY_EVENTS_REALTIME);
            boolean eventNeartime = event.getBoolean(UxipConstants.RESPONSE_KEY_EVENTS_NEARTIME);
            eventFilterMap.put(eventName, new EventFilter(eventName, eventActive, eventRealtime, eventNeartime));
            int type = 0;
            if (!eventActive) {
                type = 0; //不发送
            } else if (!eventRealtime && !eventNeartime) {
                type = 1; //批量发送
            } else {
                type = 2; //实时发送
            }
            if (eventName.contains(":") || eventName.contains(",")) {
                continue;
            } else {
                event_filters += eventName + ":" + type;
                if (i < events.length() - 1) {
                    event_filters += ",";
                }
            }
        }
        editor.putString("event_filters", event_filters);

        editor.commit();
    }
}
