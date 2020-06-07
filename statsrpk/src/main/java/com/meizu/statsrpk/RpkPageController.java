package com.meizu.statsrpk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by huchen on 16-8-4.
 */
public class RpkPageController {
    private static String TAG = "RpkPageController";
    LinkedList<Page> pages = new LinkedList<>();
    private final long PAGE_TIME_OUT = 1000 * 60 * 60 * 12;
    private final int MAX_PAGE_COUNT = 100;

    private Context mContext;
    private RpkInstanceImpl mRpkInstance;
    private String sessionId;

    public RpkPageController(Context context) {
        this.mContext = context;
    }

    public void init(RpkInstanceImpl rpkInstance) { //init已经被全局executor包裹
        this.mRpkInstance = rpkInstance;
    }

    public String getOrGenerateSessionId() {
        if (sessionId == null) {
            synchronized (this) {
                sessionId = UUID.randomUUID().toString();
                Logger.d(TAG, "generate a sessionId: " + sessionId);
            }
        }
        return sessionId;
    }

    /**
     * 页面开始
     *
     * @param pageName
     */
    public synchronized void startPage(String pageName) {
        Logger.d(TAG, "startPage: " + pageName);
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.removeCallbacks(cleanSessionId);
        Page page = new Page(pageName, System.currentTimeMillis(), SystemClock.elapsedRealtime());
        pages.addFirst(page);
        int delCount = pages.size() - MAX_PAGE_COUNT;
        if (delCount > 0) {
            Logger.d(TAG, "ON_PAGE_STOP, too many pages in stack, delete pages " + delCount);
            for (int i = 0; i < delCount; i++) {
                pages.removeLast();
            }
        }
    }

    /**
     * 页面结束
     *
     * @param pageName
     */
    public synchronized void stopPage(String pageName) {
        Logger.d(TAG, "stopPage: " + pageName);
        Iterator<Page> iterator1 = pages.iterator();
        while (iterator1.hasNext()) {
            long currentTime = System.currentTimeMillis();
            long currentElapse = SystemClock.elapsedRealtime();
            Page page = iterator1.next();
            if (Math.abs(currentElapse - page.elapse) > PAGE_TIME_OUT) {
                iterator1.remove();
                Logger.d(TAG, "remove invalid page who's duration > 12 hours:" + page);
            }
        }
        Iterator<Page> iterator2 = pages.iterator();
        boolean found = false;
        while (iterator2.hasNext()) {
            long currentTime = System.currentTimeMillis();
            long currentElapse = SystemClock.elapsedRealtime();
            Page page = iterator2.next();
            if (pageName.equals(page.name)) {
                if (!found) {
                    long start = page.time;
                    long end = currentTime;
                    long duration2 = currentElapse - page.elapse;
                    mRpkInstance.getRpkTracker().trackPage(pageName, start, end, duration2);
                    Logger.d(TAG, "create a page event: " + page);
                    found = true;
                } else {
                    Logger.d(TAG, "found repeated page: " + page);
                }
                iterator2.remove();
            }
        }
        if (pages.size() <= 0) {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.postDelayed(cleanSessionId, 30 * 1000);
        }
    }

    private Runnable cleanSessionId = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "clean sessionId: " + sessionId);
            sessionId = null;
        }
    };

    private class Page {
        private String name;
        private long time; //页面开始的时间戳
        private long elapse; //页面开始的时间戳，离开机时间

        Page(String name, long time, long elapse) {
            this.name = name;
            this.time = time;
            this.elapse = elapse;
        }
    }

}
