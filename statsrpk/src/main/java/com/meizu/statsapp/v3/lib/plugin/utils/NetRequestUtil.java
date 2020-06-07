package com.meizu.statsapp.v3.lib.plugin.utils;

import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huchen on 16-10-12.
 */

public class NetRequestUtil {
    public static String TAG = NetRequestUtil.class.getSimpleName();
    public static String HEADER_If_None_Match = "If-None-Match";
    public static String HEADER_If_Modified_Since = "If-Modified-Since";

    public static String sign(String requestType, String uri, Map<String, String> uriParam, Map<String, String> header) {
        Map<String, String> parameterMap = new HashMap<>(uriParam);
        String uriPath = "";
        try {
            URI requestUri = new URI(uri);
            uriPath = requestUri.getPath();
        } catch (URISyntaxException e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }

        parameterMap.put("key", UxipConstants.UMID_SECRET_KEY);
        if (header != null && header.containsKey(HEADER_If_None_Match)) {
            parameterMap.put(HEADER_If_None_Match, header.get(HEADER_If_None_Match));
        }
        if (header != null && header.containsKey(HEADER_If_Modified_Since)) {
            parameterMap.put(HEADER_If_Modified_Since, header.get(HEADER_If_Modified_Since));
        }

        List<String> keys = new ArrayList<String>(parameterMap.keySet());
        Collections.sort(keys);

        StringBuilder canonicalStringBuilder = new StringBuilder();
        for (String key : keys) {
            canonicalStringBuilder.append("&").append(urlEncode(key)).append("=")
                    .append(urlEncode(parameterMap.get(key)));
        }
        String canonicalString = canonicalStringBuilder.toString().substring(1);

        StringBuilder stringToSign = new StringBuilder().append(requestType).append("\n")
                .append(uriPath).append("\n").append(canonicalString);

        return Utils.getMD5(stringToSign.toString().getBytes());
    }

    private static Map<String, String> flatMap(Map<String, String[]> map) {
        Map<String, String> flatMap = new HashMap<String, String>();

        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            flatMap.put(entry.getKey(), toString(entry.getValue()));
        }

        return flatMap;
    }

    private static String toString(String[] a) {
        if (a == null)
            return "";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.toString();
            b.append(",  ");
        }
    }

    private static String urlEncode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return URLEncoder.encode(value, "UTF-8").replace("+", "%20").replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
        }
        return null;
    }
}
