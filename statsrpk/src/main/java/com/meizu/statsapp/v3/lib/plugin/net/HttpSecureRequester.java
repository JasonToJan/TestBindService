package com.meizu.statsapp.v3.lib.plugin.net;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.meizu.statsapp.v3.lib.plugin.net.multipart.DataPart;
import com.meizu.statsapp.v3.lib.plugin.net.multipart.Part;
import com.meizu.statsapp.v3.lib.plugin.secure.HttpKeyMgr;
import com.meizu.statsapp.v3.lib.plugin.utils.FailureRestrict;
import com.meizu.statsapp.v3.lib.plugin.utils.Utils;
import com.meizu.statsapp.v3.utils.CommonUtils;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jhui on 15-12-29.
 */
public class HttpSecureRequester {
    private static final String TAG = "HttpSecureRequester";
    private static final Object lock = new Object();
    private static final String APPLICATION_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String MULTIPART_FORM_CONTENT_TYPE = "multipart/form-data";
    private static HttpSecureRequester sInstance;
    private static String MULTI_BOUNDARY = "******--212x89--";

    private Context context;
    private GslbWrapper gslbWrapper;

    private HttpSecureRequester(Context context) {
        this.context = context;
        try {
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        } catch (Exception e) {
            e.printStackTrace();
        }
        gslbWrapper = GslbWrapper.getsInstance(context);
        HttpKeyMgr.init(context);
    }

    public static HttpSecureRequester getInstance(Context context) {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null)
                    sInstance = new HttpSecureRequester(context);
            }
        }

        return sInstance;
    }

    /**
     * @param actionUrl
     * @param headers
     * @param uploadData
     * @return
     */
    public NetResponse postMultipart(String actionUrl, Map<String, String> headers, byte[] uploadData) {
        if (!FailureRestrict.check("HttpSecureRequester.postMultipart")) {
            return null;
        }
        URL originURL;
        try {
            originURL = new URL(actionUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        URL efURL = getEfURL(originURL);
        URL gslbURL = gslbConvert(efURL);
        headers = attachKeyHeader(headers);
        NetResponse response = null;
        if (gslbURL.getHost().equals(originURL.getHost())) { //gslb转换不成功
            Logger.d(TAG, "gslb conversion failure.");
            try {
                response = realMultipartRequest(efURL, originURL.getHost(), headers, uploadData);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } else {
            try {
                response = realMultipartRequest(gslbURL, originURL.getHost(), headers, uploadData);
            } catch (IOException e) {
                e.printStackTrace();
                gslbWrapper.onResponse(gslbURL.getHost(), -1);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            if (response == null) { //使用原始url再请求一次
                try {
                    response = realMultipartRequest(efURL, originURL.getHost(), headers, uploadData);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
        if (response != null && response.getResponseCode() > 400 && response.getResponseCode() != 495) {
            FailureRestrict.addFail("HttpSecureRequester.postMultipart");
        }
        return response;
    }

    private NetResponse realMultipartRequest(URL url, String host, Map<String, String> headers, byte[] uploadData) throws IOException, RuntimeException {
        if (url == null) {
            Logger.e(TAG, "url is null");
            return null;
        }
        HttpURLConnection con = null;
        con = (HttpURLConnection) url.openConnection();
        con.setDoInput(true);
        con.setUseCaches(false);
        try {
            con.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        con.setConnectTimeout(30 * 1000);
        con.setReadTimeout(30 * 1000);
        try {
            con.setRequestProperty("Host", host);
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestProperty("Content-Type", MULTIPART_FORM_CONTENT_TYPE + "; boundary=" + MULTI_BOUNDARY);
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    con.setRequestProperty(header.getKey(), header.getValue());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        DataOutputStream ds = null;
        InputStream is = null;
        try {
            if (uploadData != null && uploadData.length != 0) {
                con.setDoOutput(true);
                ds = new DataOutputStream(con.getOutputStream());
                DataPart dataPart = new DataPart("data", Utils.getMD5(uploadData) + "-gzip",
                        uploadData);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Part.sendParts(bos, new Part[]{dataPart}, MULTI_BOUNDARY.getBytes());
                byte[] encrypted = HttpKeyMgr.get().encrypt(bos.toByteArray());
                if (encrypted != null) {
                    ds.write(encrypted);
                    ds.flush();
                }
            }
            int code = con.getResponseCode();
            Logger.d(TAG, "code = " + code);
            byte[] body = null;
            is = con.getInputStream();
            if (null != is) {
                body = getByteArrayByInputStream(is);
                if (body != null) {
                    Logger.d(TAG, "body = " + new String(body));
                    body = HttpKeyMgr.get().decrypt(body);
                    if (body != null) {
                        Logger.d(TAG, "decrypt body = " + new String(body));
                    }
                }
            }
            if (code != 200) {
                if (code == 495) {
                    HttpKeyMgr.get().reInitKeys();
                }
                return null;
            }
            return new NetResponse(code, body != null ? new String(body) : null);
        } finally {
            CommonUtils.closeQuietly(ds);
            CommonUtils.closeQuietly(is);
            if (con != null) {
                con.disconnect();
            }
        }
    }

    /**
     * @param actionUrl
     * @param headers
     * @param content
     * @return
     */
    public NetResponse stringPartRequest(String actionUrl, String method, Map<String, String> headers, String content) {
        if (!FailureRestrict.check("HttpSecureRequester.stringPartRequest")) {
            return null;
        }
        URL originURL;
        try {
            originURL = new URL(actionUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        URL efURL = getEfURL(originURL);
        URL gslbURL = gslbConvert(efURL);
        headers = attachKeyHeader(headers);
        NetResponse response = null;
        if (gslbURL.getHost().equals(originURL.getHost())) { //gslb转换不成功
            try {
                response = realStringPartRequest(efURL, originURL.getHost(), method, headers, content);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e){
                e.printStackTrace();
            }
        } else {
            try {
                response = realStringPartRequest(gslbURL, originURL.getHost(), method, headers, content);
            } catch (IOException e) {
                e.printStackTrace();
                gslbWrapper.onResponse(gslbURL.getHost(), -1);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            if (response == null) { //使用原始url再请求一次
                try {
                    response = realStringPartRequest(efURL, originURL.getHost(), method, headers, content);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }

            }
        }
        if (response != null && response.getResponseCode() > 400 && response.getResponseCode() != 495) {
            FailureRestrict.addFail("HttpSecureRequester.stringPartRequest");
        }
        return response;
    }

    private NetResponse realStringPartRequest(URL url, String host, String method, Map<String, String> headers, String content) throws IOException, RuntimeException {
        if (url == null) {
            Logger.e(TAG, "url is null");
            return null;
        }
        HttpURLConnection con = null;
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setDoInput(true);
        con.setUseCaches(false);
        con.setConnectTimeout(30 * 1000);
        con.setReadTimeout(30 * 1000);
        try {
            con.setRequestProperty("Host", host);
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestProperty("Content-Type", APPLICATION_CONTENT_TYPE);
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    con.setRequestProperty(header.getKey(), header.getValue());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        DataOutputStream ds = null;
        InputStream is = null;
        try {
            if (content != null) {
                Logger.d(TAG, "content:\n" + content);
                con.setDoOutput(true);
                ds = new DataOutputStream(con.getOutputStream());
                byte[] encrypted = HttpKeyMgr.get().encrypt(content.getBytes());
                if (encrypted != null) {
                    ds.write(encrypted);
                    ds.flush();
                }
            }
            getsKey(con);
            int code = con.getResponseCode();
            Logger.d(TAG, "code = " + code);
            byte[] body = null;
            is = con.getInputStream();
            if (null != is) {
                body = getByteArrayByInputStream(is);
                if (body != null) {
                    Logger.d(TAG, "body = " + new String(body));
                    body = HttpKeyMgr.get().decrypt(body);
                    if (body != null) {
                        Logger.d(TAG, "decrypt body = " + new String(body));
                    }
                }
            }
            if (code != 200 && code != 304) {
                if (code == 495) {
                    HttpKeyMgr.get().reInitKeys();
                }
                return null;
            }
            if (body != null) {
                return new NetResponse(code, new String(body));
            } else {
                return new NetResponse(code, null);
            }
        } finally {
            CommonUtils.closeQuietly(ds);
            CommonUtils.closeQuietly(is);
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private String generateEf(URL originURL) {
        String urlFile = originURL.getFile();
        byte[] encrypted = HttpKeyMgr.get().encrypt(urlFile.getBytes());
        if (encrypted == null) {
            return null;
        }
        String ef = Base64.encodeToString(encrypted, Base64.NO_WRAP);
        try {
            ef = URLEncoder.encode(ef, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
        }
        Logger.d(TAG, "generated ef: " + ef);
        return ef;
    }

    private URL getEfURL(URL url) {
        URL newUrl = null;
        try {
            String ef = generateEf(url);
            newUrl = new URL(url.getProtocol(), url.getHost(), "lighttps?ef=" + ef);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (newUrl == null) {
            newUrl = url;
        }
        return newUrl;
    }

    private URL gslbConvert(URL url) {
        Logger.d(TAG, "### before gslb convert");
        URL newUrl = null;
        String host = url.getHost();
        String ip = gslbWrapper.convert(host);
        try {
            newUrl = new URL(url.getProtocol(), ip, url.getFile());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Logger.d(TAG, "### after gslb convert, ip: " + ip);
        if (newUrl == null) {
            newUrl = url;
        }
        return newUrl;
    }

    private Map<String, String> attachKeyHeader(Map<String, String> headers) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        byte[] sKey64 = HttpKeyMgr.get().getsKey64();
        if (sKey64 != null && sKey64.length > 0) {
            String x_s_key = new String(sKey64);
            Logger.d(TAG, "attach x_s_key: " + x_s_key);
            headers.put("X-S-Key", x_s_key); //有sKey带sKey
        } else {
            byte[] aKey64 = HttpKeyMgr.get().getaKey64();
            if (aKey64 != null && aKey64.length > 0) {
                String x_a_key = new String(HttpKeyMgr.get().getaKey64());
                Logger.d(TAG, "attach x_a_key: " + x_a_key);
                headers.put("X-A-Key", "V1:" + x_a_key); //没有sKey带aKey
            }
        }
        return headers;
    }

    private void getsKey(URLConnection con) {
        try {
            String x_s_key = con.getHeaderField("X-S-Key");
            Logger.d(TAG, "get x_s_key = " + x_s_key);
            if (!TextUtils.isEmpty(x_s_key)) {
                HttpKeyMgr.get().saveSKey(x_s_key);
            }
        } catch (Throwable t) {
        }
    }

    private byte[] getByteArrayByInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] result = null;
        try {
            int i = -1;
            while ((i = is.read()) != -1) {
                baos.write(i);
            }
            result = baos.toByteArray();
        } finally {
            CommonUtils.closeQuietly(baos);
        }
        return result;
    }
}
