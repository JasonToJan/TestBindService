package com.meizu.statsrpk;

import android.content.Context;
import android.text.TextUtils;

import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.Map;

/**
 * Created by huchen on 16-8-29.
 */
public class RpkInstanceImpl {
    private static final String TAG = RpkInstanceImpl.class.getSimpleName();
    private Context applicationContext;

    private RpkInfo rpkInfo;
    private RpkEmitter rpkEmitter;
    private RpkTracker rpkTracker;
    private RpkPageController rpkPageController;
    private RpkConfigController rpkConfigController;

    public RpkInstanceImpl(Context context, RpkInfo rpkInfo) {
        long t1 = System.currentTimeMillis();
        if (null == context) {
            throw new IllegalArgumentException("The applicationContext is null!");
        }
        applicationContext = context.getApplicationContext();
        this.rpkInfo = rpkInfo;

        rpkConfigController = new RpkConfigController(applicationContext, rpkInfo);
        Logger.d(TAG, "##### RpkInstanceImpl 1, " + (System.currentTimeMillis() - t1));
        Logger.d(TAG, "##### RpkInstanceImpl 2, " + (System.currentTimeMillis() - t1));
        Logger.d(TAG, "##### RpkInstanceImpl 3, " + (System.currentTimeMillis() - t1));
        rpkTracker = buildTrack(applicationContext, buildEmitter(applicationContext, rpkInfo));
        Logger.d(TAG, "##### RpkInstanceImpl 4, " + (System.currentTimeMillis() - t1));
        rpkPageController = new RpkPageController(applicationContext);
        Logger.d(TAG, "##### RpkInstanceImpl 5, " + (System.currentTimeMillis() - t1));
        init();
        Logger.d(TAG, "##### RpkInstanceImpl 6, " + (System.currentTimeMillis() - t1));
    }

    private void init() {
        rpkConfigController.init(this);
        rpkPageController.init(this);
        rpkTracker.init(this);
    }

    RpkPageController getRpkPageController() {
        return rpkPageController;
    }

    RpkTracker getRpkTracker() {
        return rpkTracker;
    }

    /**
     * Returns a Tracker
     *
     * @param emitter a Classic emitter
     * @return a new Classic Tracker
     */
    private RpkTracker buildTrack(Context context, RpkEmitter emitter) {
        return new RpkTracker(context, emitter, rpkInfo);
    }

    private RpkEmitter buildEmitter(Context context, RpkInfo rpkInfo) {
        rpkEmitter = new RpkEmitter(context, rpkInfo);
        return rpkEmitter;
    }

    public void onEvent(final String eventName, final String pageName, final Map<String, String> properties) {
        Logger.d(TAG, "onEvent eventName: " + eventName + ", pageName: " + pageName + ", properties: " + properties);
        if (null == rpkTracker || TextUtils.isEmpty(eventName)) {
            return;
        }
        rpkTracker.trackEvent(eventName, pageName, properties);
    }

    /**
     * 跟踪页面启动。
     *
     * @param pageName 页面名称，不能为空
     */
    public void onPageStart(final String pageName) {
        Logger.d(TAG, "onPageStart pageName: " + pageName);
        if (null == rpkPageController) {
            return;
        }
        rpkPageController.startPage(pageName);
    }

    /**
     * 跟踪页面退出。
     *
     * @param pageName 页面名称，不能为空
     */
    public void onPageStop(final String pageName) {
        Logger.d(TAG, "onPageStop pageName: " + pageName);
        if (null == rpkPageController) {
            return;
        }
        rpkPageController.stopPage(pageName);
    }

}
