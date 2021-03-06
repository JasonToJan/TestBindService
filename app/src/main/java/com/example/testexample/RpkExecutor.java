package com.example.testexample;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

//入口api的工作线程
class RpkExecutor {

    private static String TAG = RpkExecutor.class.getSimpleName();
    private static Handler sHandler;
    private static final int KILL_WORKER = 5; // Hard-kill the worker thread

    /**
     * If the executor is null creates a
     * new executor.
     *
     * @return the executor
     */
    private static Handler getExecutor() {
        if (sHandler == null) {
            synchronized (RpkExecutor.class) {
                final HandlerThread thread = new HandlerThread("com.meizu.statsrpk.apiWorker", Thread.NORM_PRIORITY);
                thread.start();
                sHandler = new MessageHandler(thread.getLooper());
            }
        }
        return sHandler;
    }

    static class MessageHandler extends Handler {
        public MessageHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == KILL_WORKER) {
                Log.w(TAG, "Worker received a hard kill. Thread id " + Thread.currentThread().getId());
                synchronized(RpkExecutor.class) {
                    sHandler = null;
                    try {
                        Looper.myLooper().quit();
                    } catch (NullPointerException e) {
                        Log.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
                    }
                }
            }
        }
    }

    /**
     * Sends a runnable to the executor service.
     *
     * @param runnable the runnable to be queued
     */
    public static void execute(Runnable runnable) {
        getExecutor().post(runnable);
    }

    /**
     * Sends a runnable to the executor service.
     *
     * @param runnable the runnable to be queued
     * @param interval the interval in milliseconds
     */
    public static void schedule(Runnable runnable, long interval) {
        getExecutor().postDelayed(runnable, interval);
    }

    /**
     * Sends a runnable to the executor service.
     *
     * @param message the runnable to be queued
     */
    private static void sendMessage(Message message) {
        getExecutor().sendMessage(message);
    }

    /**
     * Sends a callable to the executor service and
     * returns a Future.
     *
     * @param runnable the callable to be queued
     */
    public static void cancel(Runnable runnable) {
        getExecutor().removeCallbacks(runnable);
    }


    /**
     * Shuts the executor service down and resets
     * the executor to a null state.
     */
    public static void shutdown() {
        final Message m = Message.obtain();
        m.what = KILL_WORKER;
        getExecutor().sendMessage(m);
    }

    private static boolean isDead() {
        synchronized(RpkExecutor.class) {
            return sHandler == null;
        }
    }

    public static Looper getLooper() {
       return getExecutor().getLooper();
    }

    /**
     * Returns the status of the executor.
     *
     * @return executor is alive or not
     */
    public static boolean status() {
        return isDead();
    }
}