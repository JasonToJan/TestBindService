/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package com.meizu.statsapp.v3.lib.plugin.utils;

import android.os.Build;

import com.meizu.statsapp.v3.utils.CommonUtils;
import com.meizu.statsapp.v3.utils.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * Provides basic Utilities for the Snowplow Tracker.
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    /**
     * 生成输入字节数组的MD5。
     *
     * @param source
     * @return MD5字符
     */
    public static String getMD5(byte[] source) {
        if (null == source || source.length < 1) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return bytesToHexString(md.digest(source));
        } catch (NoSuchAlgorithmException e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        return null;
    }

    private static final char[] DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'
    };

    private static String bytesToHexString(byte[] bytes) {
        if (null == bytes) {
            return "";
        }
        char[] buf = new char[bytes.length * 2];
        int c = 0;
        for (byte b : bytes) {
            buf[c++] = DIGITS[(b >> 4) & 0xf];
            buf[c++] = DIGITS[b & 0xf];
        }
        return new String(buf);
    }

    /**
     * Converts a Map to a JSONObject
     *
     * @param map The map to convert
     * @return The JSONObject
     */
    @SuppressWarnings("unchecked")
    public static JSONObject mapToJSONObject(Map map) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new JSONObject(map);
        } else {
            JSONObject retObject = new JSONObject();
            Set<Map.Entry> entries = map.entrySet();
            for (Map.Entry entry : entries) {
                String key = (String) entry.getKey();
                Object value = getJsonSafeObject(entry.getValue());
                try {
                    retObject.put(key, value);
                } catch (JSONException e) {
                    Logger.w(TAG, "Could not put key:" + key + " and value:" + value + " into new JSONObject:" + e);
                }
            }
            return retObject;
        }
    }

    /**
     * Returns a Json Safe object for situations
     * where the Build Version is too old.
     *
     * @param o The object to check and convert
     * @return the json safe object
     */
    private static Object getJsonSafeObject(Object o) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return o;
        } else if (o == null) {
            return JSONObject.NULL;
        } else if (o instanceof JSONObject || o instanceof JSONArray) {
            return o;
        } else if (o instanceof Collection) {
            JSONArray retArray = new JSONArray();
            for (Object entry : (Collection) o) {
                retArray.put(getJsonSafeObject(entry));
            }
            return retArray;
        } else if (o.getClass().isArray()) {
            JSONArray retArray = new JSONArray();
            int length = Array.getLength(o);
            for (int i = 0; i < length; i++) {
                retArray.put(getJsonSafeObject(Array.get(o, i)));
            }
            return retArray;
        } else if (o instanceof Map) {
            return mapToJSONObject((Map) o);
        } else if (o instanceof Boolean ||
                o instanceof Byte ||
                o instanceof Character ||
                o instanceof Double ||
                o instanceof Float ||
                o instanceof Integer ||
                o instanceof Long ||
                o instanceof Short ||
                o instanceof String) {
            return o;
        } else if (o.getClass().getPackage().getName().startsWith("java.")) {
            return o.toString();
        }
        return null;
    }

    public static Map<String, Object> jsonObjectToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<>();

        if (json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    private static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            //huchen add for null
            if (value == JSONObject.NULL) {
                value = null;
            } else if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    /**
     *
     * @param input
     * @return
     */
    public static byte[] compress(byte[] input) {
        if (input == null || input.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(input);
        } catch (IOException e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        } finally {
            CommonUtils.closeQuietly(gzip);
        }
        return out.toByteArray();
    }

}
