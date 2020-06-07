package com.meizu.statsapp.v3.lib.plugin.page;

import android.content.Context;
import android.os.SystemClock;

import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by huchen on 16-8-4.
 */
public class PageController {
    private final static String TAG = "PageController";
    private LinkedList<Page> pages = new LinkedList<>();
    private final long PAGE_TIME_OUT = 1000 * 60 * 60 * 12; //12小时的页面时长认为是异常
    private final int MAX_PAGE_COUNT = 100;

    private Context mContext;

    public PageController(Context context) {
        this.mContext = context;
        Logger.d(TAG, "PageController init");
    }

    /**
     * 页面开始
     *
     * @param pageName
     */
    public synchronized void startPage(String pageName) {
        Logger.d(TAG, "startPage: " + pageName);
        Page page = new Page(pageName, System.currentTimeMillis(), SystemClock.elapsedRealtime());
        pages.addFirst(page); //加入头部，使得生成事件时用最新的pageStart
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
    public synchronized Page stopPage(String pageName) {
        Logger.d(TAG, "stopPage: " + pageName);
        Iterator<Page> iterator1 = pages.iterator();
        while (iterator1.hasNext()) {
            long currentElapse = SystemClock.elapsedRealtime();
            Page page = iterator1.next();
            if (Math.abs(currentElapse - page.elapse) > PAGE_TIME_OUT) {
                iterator1.remove();
                Logger.d(TAG, "#2_remove invalid page who's duration > 12 hours:" + page);
            }
        }
        Iterator<Page> iterator2 = pages.iterator();
        Page fisrtFound = null;
        while (iterator2.hasNext()) {
            Page page = iterator2.next();
            if (pageName.equals(page.name)) {
                if (fisrtFound == null) {
                    fisrtFound = page;
                    Logger.d(TAG, "stopPage, first found page: " + page);
                } else {
                    Logger.d(TAG, "stopPage, found repeated page: " + page);
                }
                iterator2.remove();
            }
        }
        return fisrtFound;
    }

    public class Page {
        public String name;
        public long time; //页面开始的时间戳
        public long elapse; //页面开始的时间戳，离开机时间

        Page(String name, long time, long elapse) {
            this.name = name;
            this.time = time;
            this.elapse = elapse;
        }

        @Override
        public String toString() {
            return "[" + name + "," + time + "," + elapse + "]";
        }
    }

    public long getPageDuration(String pageName) {
        Iterator<Page> iterator1 = pages.iterator();
        long currentTime = System.currentTimeMillis();
        long currentElapse = SystemClock.elapsedRealtime();
        while (iterator1.hasNext()) {
            Page page = iterator1.next();
            if (pageName.equals(page.name)) {
                //long duration = currentTime - page.time;
                long duration = currentElapse - page.elapse;
                return duration > 0 ? duration : 0;
            }
        }

        return 0;
    }
}
