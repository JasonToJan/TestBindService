package com.meizu.nonvccstats;

import android.content.Context;

import com.meizu.statsapp.v3.utils.reflect.ReflectHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by jinhui on 17-8-10.
 */

public class NonVccStatsHelper {
    private static String TAG = NonVccStatsHelper.class.getSimpleName();
    private static NonVccStatsHelper nonVccStatsHelper;
    private static final Object lock = new Object();
    private Context mContext;
    private Object instanceImpl;

    private NonVccStatsHelper(Context context) {
        if (null == context) {
            throw new IllegalArgumentException("The context is null!");
        }
        mContext = context;
        Class<?> clazz = null;
        try {
            clazz = context.getClassLoader().loadClass("android.nonvccUsageStats.UsageStatsNonVccProxy3");
            Constructor constructor = clazz.getDeclaredConstructor(Context.class);
            constructor.setAccessible(true);
            instanceImpl = constructor.newInstance(context);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得helper对象。
     *
     * @return NonVccStatsHelper
     */
    public static NonVccStatsHelper getInstance(Context context) {
        if (nonVccStatsHelper == null) {
            synchronized (lock) {
                if (null == nonVccStatsHelper) {
                    nonVccStatsHelper = new NonVccStatsHelper(context);
                }
            }
        }
        return nonVccStatsHelper;
    }

    /**
     * 记录一个Os事件
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
    public void onOsEvent(final String eventName, final String pageName, final Map<String, String> properties) {
        try {
            ReflectHelper.invoke(instanceImpl, "onOsEvent",
                    new Class[]{String.class, String.class, Map.class},
                    new Object[]{eventName, pageName, properties});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 记录一个Os事件，并立即上报
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
    public void onOsEventRealtime(final String eventName, final String pageName, final Map<String, String> properties) {
        try {
            ReflectHelper.invoke(instanceImpl, "onOsEventRealtime",
                    new Class[]{String.class, String.class, Map.class},
                    new Object[]{eventName, pageName, properties});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 记录一个事件
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     * @param customPackageName 自定义的包名，不能为空
     */
    public void onAppEvent(final String eventName, final String pageName, final Map<String, String> properties, String customPackageName) {
        try {
            ReflectHelper.invoke(instanceImpl, "onAppEvent",
                    new Class[]{String.class, String.class, Map.class, String.class},
                    new Object[]{eventName, pageName, properties, customPackageName});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 记录一个事件，并立即上报
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     * @param customPackageName 自定义的包名，不能为空
     */
    public void onAppEventRealtime(final String eventName, final String pageName, final Map<String, String> properties, String customPackageName) {
        try {
            ReflectHelper.invoke(instanceImpl, "onAppEventRealtime",
                    new Class[]{String.class, String.class, Map.class, String.class},
                    new Object[]{eventName, pageName, properties, customPackageName});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
