package com.meizu.statsapp.v3.lib.plugin.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

/**
 * Created by huchen on 16-11-7.
 */

public abstract class IntervalTimer {
    private final long mCountdownInterval;
    private long mTriggerTimeInFuture;
    private boolean mCancelled;
    private static final int MSG = 1;
    private static final int SKIP = -1;
    private Handler mHandler;

    public IntervalTimer(long countDownInterval) {
        this(null, countDownInterval);
    }

    public IntervalTimer(Looper looper, long countDownInterval) {
        this.mCancelled = false;
        this.mCountdownInterval = countDownInterval;

        this.mHandler = new Handler(looper == null ? Looper.getMainLooper() : looper) {
            public void handleMessage(Message msg) {
                synchronized (IntervalTimer.this) {
                    if (!mCancelled) {
                        switch (msg.what) {
                            case SKIP:
                                mTriggerTimeInFuture = SystemClock.elapsedRealtime() + mCountdownInterval;
                                break;
                            case MSG:
                                long millisLeft = mTriggerTimeInFuture - SystemClock.elapsedRealtime();
                                if (millisLeft <= 0L) {
                                    onTrigger();
                                    mTriggerTimeInFuture = mTriggerTimeInFuture + mCountdownInterval - millisLeft;
                                    this.sendMessageDelayed(this.obtainMessage(MSG), mCountdownInterval);
                                } else if (millisLeft <= mCountdownInterval) {
                                    this.sendMessageDelayed(this.obtainMessage(MSG), millisLeft);
                                }
                        }

                    }
                }
            }
        };
    }

    public synchronized void cancel() {
        this.mCancelled = true;
        this.mHandler.removeMessages(MSG);
        this.mHandler.removeMessages(SKIP);
    }

    public void skip() {
        this.mHandler.sendEmptyMessage(SKIP);
    }

    public abstract void onTrigger();

    public synchronized IntervalTimer start() {
        this.mCancelled = false;
        this.mTriggerTimeInFuture = SystemClock.elapsedRealtime() + mCountdownInterval;
        this.mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }
}
