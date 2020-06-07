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

public class FailureRestrict {
    private final static String TAG = FailureRestrict.class.getSimpleName();

    private final static List<Pair<String, Long>> failRecords = new ArrayList<>();
    private final static Map<String, Long> forbids = new HashMap<>();

    /**
     * 是否可以调用
     *
     * @return
     */
    public static synchronized boolean check(String methodKey) {
        long now = System.currentTimeMillis();
        Logger.d(TAG, "check " + methodKey + ", now: " + now);
        if (forbids.containsKey(methodKey)) {
            long forbidTime = forbids.get(methodKey);
            Logger.d(TAG, "forbid " + methodKey + ", forbidTime: " + forbidTime);
            if (now - forbidTime >= 5 * 60 * 1000) { //被禁5分钟以上要解禁
                Logger.d(TAG, "un forbid " + methodKey);
                forbids.remove(methodKey);
            }
            return false;
        }

        long earliest = 0, latest = 0;
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < failRecords.size(); i++) {
            Pair<String, Long> e = failRecords.get(i);
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
        if (indexes.size() >= 2) {
            Logger.d(TAG, "accumulate 2 fail records, methodKey: " + methodKey);
            for (int i = indexes.size() - 1; i >= 0; i--) {
                failRecords.remove((int) indexes.get(i));
            }
            Logger.d(TAG, "latest call: " + latest + ", earliest call: " + earliest + ", methodKey: " + methodKey);
            if (latest - earliest > 0 && latest - earliest <= 10 * 1000) { //10秒内有2次失败，认为太频繁了
                forbids.put(methodKey, now);
                Logger.d(TAG, "add to forbid: " + methodKey + ", now: " + now);
                return false;
            }
        }

        return true;
    }

    /**
    失败一次
     */
    public static synchronized void addFail(String methodKey) {
        long now = System.currentTimeMillis();
        Logger.d(TAG, "addFail " + methodKey + ", now: " + now);
        Pair<String, Long> pair = new Pair(methodKey, now);
        failRecords.add(pair);
    }

}
