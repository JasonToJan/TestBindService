package com.meizu;

import android.util.Log;


import androidx.annotation.NonNull;

import com.meizu.startsrpk.BuildConfig;

import java.util.HashMap;
import java.util.Locale;

/**
 * Title: LogUtility
 * Description: Log装饰类
 */
public class LogUtils {

    private static StringBuilder sStringBuilder = new StringBuilder();

    private static synchronized String buildString(Object[] msg) {
        if (msg == null || msg.length == 0) {
            return "";
        }
        sStringBuilder.setLength(0);
        for (Object s : msg) {
            sStringBuilder.append(s);
        }
        return sStringBuilder.toString();
    }

    private static boolean isDebug = BuildConfig.DEBUG;

    private static String APP_TAG = "TEST##CardSDK";

    private static HashMap<String, String> sCachedTag = new HashMap<>();
    private static JsonFormatter sJsonFormatter = new GsonFormatter();

    private LogUtils() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " cannot be instantiated");
    }

    public static void Init(@NonNull String appTag, @NonNull JsonFormatter formatter) {
        APP_TAG = appTag;
        sJsonFormatter = formatter;
    }

    public static void i(String message) {
        Log.i(buildTag(APP_TAG), buildMessage(message));
    }

    public static void d(String message) {
        if (isDebug) {
            Log.d(buildTag(APP_TAG), buildMessage(message));
        }
    }

    public static void w(String message) {
        if (isDebug) {
            Log.w(buildTag(APP_TAG), buildMessage(message));

        }
    }

    public static void e(String message) {
        if (isDebug) {
            Log.e(buildTag(APP_TAG), buildMessage(message));
        }
    }

    public static void v(String message) {
        if (isDebug) {
            Log.v(buildTag(APP_TAG), buildMessage(message));
        }
    }

    public static void wtf(String message) {
        Log.wtf(buildTag(APP_TAG), buildMessage(message));
    }

    public static void json(String message) {
        Log.v(buildTag(APP_TAG), buildMessage(formatJson(message)));
    }

    public static void i(@NonNull String tag, String message) {
        if (isDebug) {
            Log.i(buildTag(tag), buildMessage(message));
        }
    }

    public static void d(@NonNull String tag, String message) {
        if (isDebug) {
            Log.d(tag, message);
        }
    }

    public static void w(@NonNull String tag, String message) {
        if (isDebug) {
            Log.w(buildTag(tag), buildMessage(message));
        }
    }

    public static void e(@NonNull String tag, String message) {
        if (isDebug) {
            Log.e(buildTag(tag), buildMessage(message));
        }
    }

    public static void v(@NonNull String tag, String message) {
        if (isDebug) {
            Log.v(buildTag(tag), buildMessage(message));
        }
    }

    public static void wtf(@NonNull String tag, String message) {
        Log.wtf(buildTag(tag), buildMessage(message));
    }

    public static void json(@NonNull String tag, String content) {
        Log.v(buildTag(tag), buildMessage(formatJson(content)));
    }

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

    private static String buildMessage(String message) {
        StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();

        if (traceElements == null || traceElements.length < 4) {
            return message;
        }
        StackTraceElement traceElement = traceElements[4];

        return String.format(Locale.US, "%s.%s(%s:%d) %s",
                traceElement.getClassName().substring(traceElement.getClassName().lastIndexOf(".") + 1),
                traceElement.getMethodName(),
                traceElement.getFileName(),
                traceElement.getLineNumber(),
                message
        );
    }

    private static String formatJson(String content) {
        return String.format(Locale.US, "\n%s", sJsonFormatter.formatJson(content));
    }

    public interface JsonFormatter {
        String formatJson(String content);
    }

    public static class DefaultFormatter implements JsonFormatter {
        @Override
        public String formatJson(String content) {
            return content;
        }
    }

    /**
     * 需要Gson依赖支持
     */
    public static class GsonFormatter implements LogUtils.JsonFormatter {
        @Override
        public String formatJson(String content) {
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            JsonParser parser = new JsonParser();
//            JsonElement element = parser.parse(content);
//            return gson.toJson(element);

            return content;
        }
    }
}
