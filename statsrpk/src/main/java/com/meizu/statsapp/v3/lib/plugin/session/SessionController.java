package com.meizu.statsapp.v3.lib.plugin.session;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.meizu.statsapp.v3.lib.plugin.sdk.SDKInstanceImpl;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.UUID;

/**
 * Created by huchen on 16-9-29.
 */
public class SessionController {
    private static String TAG = "SessionController";
    private Context mContext;
    private String sessionId;
    private String source;
    protected SDKInstanceImpl sdkInstanceImpl;

    private Handler mHandler;
    private final static int MSG_SESSION_END = 0x1;
    private final static String WORK_THREAD_NAME = "com.meizu.statsapp.v3.SessionControllerWorker";
    private final int sessionTimeoutMillis = 30 * 1000;//默认30秒，范围[1秒-1天]
    private ActivityLifecycleCallback mActivityLifecycleCallback;

    public SessionController(Context context) {
        mContext = context;
        HandlerThread thread = new HandlerThread(WORK_THREAD_NAME, Thread.NORM_PRIORITY);
        thread.start();
        mHandler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Logger.d(TAG, "session timeout");
                endSessionId();
                //在用户退出应用不再使用后，触发一波批量事件上报
                Logger.d(TAG, "flush events when session end");
                if (sdkInstanceImpl != null) {
                    sdkInstanceImpl.getTracker().getEmitter().flush();
                }
            }
        };
        registerApplicationLifeCycle();
        Logger.d(TAG, "SessionController init");
    }

    public void attach(SDKInstanceImpl sdkInstanceImpl) { //init已经被全局executor包裹
        this.sdkInstanceImpl = sdkInstanceImpl;
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

    private void endSessionId() {
        if (sessionId != null) {
            synchronized (this) {
                Logger.d(TAG, "end a session id: " + sessionId);
                sessionId = null;
                source = null;
            }
        }
    }

    public void onForeground() {
        Logger.d(TAG, "onForeground");
        mHandler.removeCallbacksAndMessages(null);
    }

    public void onBackground() {
        Logger.d(TAG, "onBackground");
        //30秒后仍然没回到前端，认为此次使用session结束
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(MSG_SESSION_END, sessionTimeoutMillis);
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String newSource) {
        if (TextUtils.isEmpty(source)) {//source一旦被设置，本次会话内不让修改
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (TextUtils.isEmpty(source)) {
                        source = newSource;
                        Logger.d(TAG, "set source: " + source);
                        if (sessionId != null) {
                            if (sdkInstanceImpl != null) {
                                sdkInstanceImpl.getTracker().updateSessionSource(sessionId, newSource);
                            }
                        }
                    }
                }
            });
        } else {
            Logger.d(TAG, "source already exist: " + source + ", session: " + sessionId + ", not set again");
        }
    }

    private void registerApplicationLifeCycle() {
        if (mContext == null) {
            return;
        }
        Application application = (Application)mContext.getApplicationContext();
        if (mActivityLifecycleCallback != null) {
            application.unregisterActivityLifecycleCallbacks(mActivityLifecycleCallback);
            mActivityLifecycleCallback = null;
        }

        mActivityLifecycleCallback = new ActivityLifecycleCallback(this);
        application.registerActivityLifecycleCallbacks(mActivityLifecycleCallback);
        Logger.d(TAG, "registerApplicationLifeCycle");
    }
}
