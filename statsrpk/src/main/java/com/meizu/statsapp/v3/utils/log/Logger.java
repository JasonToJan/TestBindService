package com.meizu.statsapp.v3.utils.log;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;

import com.meizu.statsapp.v3.InitConfig;
import com.meizu.statsapp.v3.utils.CommonUtils;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by huchen on 15-10-29.
 */
public class Logger {
    private static final String workThreadName = "UsageStats_Logger";
    private static ILog sHook;
    private static LogLevel sConsoleLogLevel = LogLevel.DEBUG;
    private static Handler workHandler;
    private static final String TAG = "UsageStats_";

    public static boolean sDebug = true;

    static {
        sDebug = CommonUtils.isPrintLog() | InitConfig.printLog;
        HandlerThread workThread = new HandlerThread(workThreadName);
        workThread.start();
        workHandler = new Handler(workThread.getLooper());
    }

    public static void setHook(ILog hook) {
        sHook = hook;
    }

    public static void setLevel(LogLevel logLevel) {
        sConsoleLogLevel = logLevel;
    }

    public static void v(String tag, String msg) {
        if (sDebug)
            workHandler.post(new LogInfo(LogLevel.VERBOSE, getTag(tag), msg, getThread(), getThreadName()));
    }

    public static void d(String tag, String msg) {
//        if (sDebug)
//            workHandler.post(new LogInfo(LogLevel.DEBUG, getTag(tag), msg, getThread(), getThreadName()));
        Log.d(buildTag(tag),msg);
    }

    private static String APP_TAG = "TEST##CardSDK";

    private static HashMap<String, String> sCachedTag = new HashMap<>();

    private static String buildTag(@NonNull String tag) {
        String key = String.format(Locale.US, "%s@%s", tag, Thread.currentThread().getName());

        if (!sCachedTag.containsKey(key)) {
            if (APP_TAG.equals(tag)) {
                sCachedTag.put(key, String.format(Locale.US, "|%s|%s|",
                        tag,
                        Thread.currentThread().getName()
                ));
            } else {
                sCachedTag.put(key, String.format(Locale.US, "|%s_%s|%s|",
                        APP_TAG,
                        tag,
                        Thread.currentThread().getName()
                ));
            }
        }

        return sCachedTag.get(key);
    }

    public static void i(String tag, String msg) {
        if (sDebug)
            workHandler.post(new LogInfo(LogLevel.INFO, getTag(tag), msg, getThread(), getThreadName()));
    }

    public static void w(String tag, String msg) {
        if (sDebug)
            workHandler.post(new LogInfo(LogLevel.WARN, getTag(tag), msg, getThread(), getThreadName()));
    }

    public static void e(String tag, String msg) {
        if (sDebug)
            workHandler.post(new LogInfo(LogLevel.ERROR, getTag(tag), msg, getThread(), getThreadName()));
    }

    private static String getTag(String tag) {
        //return TAG + tag;
        return tag;
    }

    private static long getThread() {
        return Thread.currentThread().getId();
    }

    private static String getThreadName() {
        return Thread.currentThread().getName();
    }

    private static class LogInfo implements Runnable {
        private LogLevel mLogLevel;
        private String mTag, mMsg, mThreadName;
        private long mTid;

        public LogInfo(LogLevel logLevel, String tag, String msg, long tid, String threadName) {
            mLogLevel = logLevel;
            mTag = tag;
            mMsg = msg;
            mTid = tid;
            mThreadName = threadName;
        }

        private String getMessage() {
            return mThreadName + "|" + mMsg;
        }

        @Override
        public void run() {
            if (mLogLevel.ordinal() >= sConsoleLogLevel.ordinal()) {
                if (mLogLevel == LogLevel.DEBUG) {
                    Log.d(mTag, getMessage());
                } else if (mLogLevel == LogLevel.INFO) {
                    Log.i(mTag, getMessage());
                } else if (mLogLevel == LogLevel.WARN) {
                    Log.w(mTag, getMessage());
                } else if (mLogLevel == LogLevel.ERROR) {
                    Log.e(mTag, getMessage());
                }
                if (sHook != null) {
                    sHook.print(mLogLevel, mTag, getMessage(), mTid);
                }
            }
        }
    }
}
