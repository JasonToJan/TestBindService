package com.meizu.statsapp.v3.utils;

import android.content.Context;
import android.util.Log;

import com.meizu.statsapp.v3.utils.reflect.SystemProperties;

import java.io.Closeable;
import java.io.IOException;

public class CommonUtils {

    public static final String TAG = "UsageStatsUtils";

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * 是否处于手机端调试模式
     *
     * @return 是否处于调试模式
     */
    public static boolean isDebugMode(Context context) {
        Object o;
        try {
            o = SystemProperties.get("persist.meizu.usagestats.debug", "");
            if (null != o && (o.equals("all") || o.equals(context.getPackageName()))) {
                return true;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return false;
    }

    /**
     * 是否开启log打印
     * @return
     */
    public static boolean isPrintLog() {
        Object o1;
        try {
            o1 = SystemProperties.get("persist.meizu.usagestats.log", "false");
            if (null != o1 && o1.equals("true")) {
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Logger -->Failed to get system properites");
            //e.printStackTrace();
        }
        return false;
    }

}
