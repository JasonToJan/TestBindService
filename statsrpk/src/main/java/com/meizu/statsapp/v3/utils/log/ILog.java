package com.meizu.statsapp.v3.utils.log;

/**
 * Created by zbin on 15-11-4.
 */
public interface ILog {
    void print(LogLevel logLevel, String tag, String msg, long tid);
}
