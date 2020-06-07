package com.meizu.statsapp.v3.lib.plugin.utils;

import android.util.Pair;

import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jinhui on 17-3-30.
 */

public class InvokerRestrict {
    private final static String TAG = InvokerRestrict.class.getSimpleName();

    private final static List<Pair<String, Long>> invokeRecords = new ArrayList<>();
    private final static Map<String, Long> forbids = new HashMap<>();

    /**
     * 是否可以调用
     *
     * @return
     */
    public static synchronized boolean check() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length >= 3) {
            String className = stackTraceElements[3].getClassName();
            String methodName = stackTraceElements[3].getMethodName();
            if (className.contains("com.meizu.statsapp.v3")) {
                String methodKey = className + "." + methodName;
                long now = System.currentTimeMillis();
                Logger.d(TAG, "check for methodKey: " + methodKey + ", now: " + now);
                if (forbids.containsKey(methodKey)) {
                    long forbidTime = forbids.get(methodKey);
                    Logger.d(TAG, "forbidTime: " + forbidTime);
                    if (now - forbidTime >= 1 * 60 * 1000) { //被禁1分钟以上要解禁
                        Logger.d(TAG, "un forbid");
                        forbids.remove(methodKey);
                    }
                    return false;
                }
                Pair<String, Long> pair = new Pair(methodKey, now);
                 invokeRecords.add(pair);
                long earliest = 0, latest = 0;
                List<Integer> indexes = new ArrayList<>();
                for (int i = 0; i <  invokeRecords.size(); i++) {
                    Pair<String, Long> e =  invokeRecords.get(i);
                    if (e.first.equals(methodKey)) {
                        indexes.add(i);
                        if (earliest == 0 || e.second < earliest) {
                            earliest = e.second;
                        }
                        if (latest == 0 || e.second > latest) {
                            latest = e.second;
                        }
                    }
                }
                if (indexes.size() >= 10) {
                    Logger.d(TAG, "accumulate 10 calls, methodKey: " + methodKey);
                    for (int i = indexes.size() - 1; i >= 0; i--) {
                         invokeRecords.remove((int)indexes.get(i));
                    }
                    Logger.d(TAG, "latest call: " + latest + ", earliest call: " + earliest);
                    if (latest - earliest > 0 && latest - earliest <= 10 * 1000) { //10秒内有10次调用，认为太频繁了
                        forbids.put(methodKey, now);
                        Logger.d(TAG, "add to forbid: " + methodKey + ", " + now);
                        return false;
                    }
                }
            }
        }

        return true;
    }

}
