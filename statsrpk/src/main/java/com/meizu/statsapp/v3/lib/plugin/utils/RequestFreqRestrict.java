package com.meizu.statsapp.v3.lib.plugin.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.meizu.statsapp.v3.utils.log.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jinhui on 17-8-14.
 */

public class RequestFreqRestrict {
    private final static String TAG = "RequestFreqRestrict";

    private final static String PREFERENCES_MOBILE_TRAFFIC_NAME = "com.meizu.statsapp.v3.request_feq_restrict";
    private final static String PREFERENCES_KEY_DATE = "date";
    private final static String PREFERENCES_KEY_CURRENT_REQUEST = "current_request";

    //限定除了事件上报外，其他请求的次数一天不超过30次
    public static boolean isAllow(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_MOBILE_TRAFFIC_NAME, Context.MODE_PRIVATE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String date = sharedPreferences.getString(PREFERENCES_KEY_DATE, "");
        String today = dateFormat.format(new Date());
        if (date.equals(today)) {
            long current_request = sharedPreferences.getLong(PREFERENCES_KEY_CURRENT_REQUEST, 0);
            if (current_request > 30) {
                Logger.d(TAG, "isAllow false");
                return false;
            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(PREFERENCES_KEY_CURRENT_REQUEST, current_request + 1);
                editor.commit();
                Logger.d(TAG, "isAllow true");
                return true;
            }
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PREFERENCES_KEY_DATE, today);
            editor.putLong(PREFERENCES_KEY_CURRENT_REQUEST, 1);
            editor.commit();
            Logger.d(TAG, "isAllow true");
            return true;
        }
    }

}
