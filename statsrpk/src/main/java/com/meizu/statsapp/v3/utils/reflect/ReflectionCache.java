package com.meizu.statsapp.v3.utils.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by tanfujun on 15-10-19.
 */
public class ReflectionCache {
    private final String TAG = "ReflectionCache";
    private static HashMap<String, ClassInfo> classInfoMap;

    private ReflectionCache() {
        classInfoMap = new HashMap<>();
    }

    private static class SingletonHolder {
        private static final ReflectionCache INSTANCE = new ReflectionCache();
    }

    private void putClassInfoToCache(String key, ClassInfo classInfo) {
        classInfoMap.put(key, classInfo);
    }

    //单例模式创建
    public static ReflectionCache build() {
        return SingletonHolder.INSTANCE;
    }

    public Class<?> forName(String className) throws ClassNotFoundException {
        return forName(className, true);
    }

    public Class<?> forName(String className, Boolean isCached) throws ClassNotFoundException {
        if (isCached) {
            ClassInfo classInfoFromCache = classInfoMap.get(className);
            if (classInfoFromCache != null) {
                return classInfoFromCache.mClass;
            } else {
                Class c = Class.forName(className);
                ClassInfo classInfo = new ClassInfo(c, className);
                putClassInfoToCache(className, classInfo);
                return c;
            }
        } else {
            return Class.forName(className);
        }
    }

    public Method getMethod(Class<?> objClass, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        ClassInfo classInfoFromCache = classInfoMap.get(objClass.getName());
        if (classInfoFromCache == null) {
            try {
                forName(objClass.getName(), true);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        classInfoFromCache = classInfoMap.get(objClass.getName());
        if (classInfoFromCache == null) {
            return null;
        }
        String methodKey = methodName;
        for (Class<?> c : parameterTypes) {
            methodKey = methodKey + c.toString();
        }
        Method methodFromCache = classInfoFromCache.getCachedMethod(methodKey);
        if (methodFromCache != null) {
            return methodFromCache;
        } else {
            Method method = objClass.getMethod(methodName, parameterTypes);
            classInfoFromCache.addCachedMethod(methodKey, method);
            return method;
        }
    }

    public Method getDeclaredMethod(Class<?> objClass, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        ClassInfo classInfoFromCache = classInfoMap.get(objClass.getName());
        if (classInfoFromCache == null) {
            try {
                forName(objClass.getName(), true);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        classInfoFromCache = classInfoMap.get(objClass.getName());
        if (classInfoFromCache == null) {
            return null;
        }
        String methodKey = methodName;
        for (Class<?> c : parameterTypes) {
            methodKey = methodKey + c.toString();
        }
        Method methodFromCache = classInfoFromCache.getCachedMethod(methodKey);
        if (methodFromCache != null) {
            return methodFromCache;
        } else {
            Method method = objClass.getDeclaredMethod(methodName, parameterTypes);
            classInfoFromCache.addCachedMethod(methodKey, method);
            return method;
        }
    }

    public Field getField(Class<?> objClass, String fieldName) throws NoSuchFieldException {
        ClassInfo classInfoFromCache = classInfoMap.get(objClass.getName());
        if (classInfoFromCache == null) {
            try {
                forName(objClass.getName(), true);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        classInfoFromCache = classInfoMap.get(objClass.getName());
        if (classInfoFromCache == null) {
            return null;
        }
        Field fieldFromCache = classInfoFromCache.getCachedField(fieldName);
        if (fieldFromCache != null) {
            return fieldFromCache;
        } else {
            Field field = objClass.getField(fieldName);
            classInfoFromCache.addCachedField(fieldName, field);
            return field;
        }
    }

    public Field getDeclaredField(Class<?> objClass, String fieldName) throws NoSuchFieldException {
        ClassInfo classInfoFromCache = classInfoMap.get(objClass.getName());
        if (classInfoFromCache == null) {
            try {
                forName(objClass.getName(), true);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        classInfoFromCache = classInfoMap.get(objClass.getName());
        if (classInfoFromCache == null) {
            return null;
        }
        Field fieldFromCache = classInfoFromCache.getCachedField(fieldName);
        if (fieldFromCache != null) {
            return fieldFromCache;
        } else {
            Field field = objClass.getDeclaredField(fieldName);
            classInfoFromCache.addCachedField(fieldName, field);
            return field;
        }
    }

}
