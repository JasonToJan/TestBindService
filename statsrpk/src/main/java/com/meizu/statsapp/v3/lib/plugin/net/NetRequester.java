package com.meizu.statsapp.v3.lib.plugin.net;

import android.content.Context;
import android.content.SharedPreferences;

import com.meizu.statsapp.v3.utils.CommonUtils;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by jhui on 15-12-29.
 */
public class NetRequester {
    private static final String TAG = NetRequester.class.getSimpleName();
    private static final Object lock = new Object();
    private static final String APPLICATION_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String MULTIPART_FORM_CONTENT_TYPE = "multipart/form-data";
    private static NetRequester sInstance;
    private static String MULTI_BOUNDARY = "******--212x89--";

    private Context context;

    private NetRequester(Context context) {
        this.context = context;
    }

    public static NetRequester getInstance(Context context) {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null)
                    sInstance = new NetRequester(context);
            }
        }

        return sInstance;
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

    /**
     * 只给获取uxip配置请求用
     * @param actionUrl
     * @param headers
     * @return
     * @throws IOException
     */
    public NetResponse postNoGslb(String actionUrl, Map<String, String> headers) throws IOException, RuntimeException {
        return realNoGslbRequest(actionUrl, "GET", headers, null);
    }

    /**
     * @param actionUrl
     * @param method
     * @param headers
     * @param content
     * @return
     * @throws IOException
     */
    private NetResponse realNoGslbRequest(String actionUrl, String method, Map<String, String> headers, String content) throws IOException, RuntimeException {
        HttpURLConnection con = null;
        URL url = new URL(actionUrl);
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setDoInput(true);
        con.setUseCaches(false);
        try {
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
        OutputStream os = null;
        try {
            if (content != null) {
                con.setDoOutput(true);
                os = con.getOutputStream();
                ds = new DataOutputStream(os);
                StringBuffer sb = new StringBuffer();
                ds.writeBytes(content);
                Logger.d(TAG, "content:\n" + sb.toString());
                ds.flush();
            }
            int code = con.getResponseCode();
            Logger.d(TAG, "code = " + code);
            if (code == 200) {
                try {
                    String lastModified = con.getHeaderField("Last-Modified");
                    Logger.d(TAG, "get lastModified = " + lastModified);
                    String eTag = con.getHeaderField("ETag");
                    Logger.d(TAG, "get ETag = " + lastModified);
                    SharedPreferences sp = context.getSharedPreferences("com.meizu.statsapp.v3.serverconfig", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("lastModified", lastModified);
                    editor.putString("ETag", eTag);
                    editor.apply();
                } catch (NullPointerException e) {}
            }
            byte[] body = null;
            is = con.getInputStream();
            if (null != is) {
                body = getByteArrayByInputStream(is);
                if (body != null) {
                    try {
                        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                        SecretKeySpec secretKeySpec = new SecretKeySpec(("E21B5316F7B0813C").getBytes("UTF-8"), "AES");
                        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
                        body = cipher.doFinal(body);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }
                }
            }
            Logger.d(TAG, "body = " + body);
            if (body != null) {
                return new NetResponse(code, new String(body));
            } else {
                return new NetResponse(code, null);
            }
        } finally {
            CommonUtils.closeQuietly(ds);
            CommonUtils.closeQuietly(os);
            CommonUtils.closeQuietly(is);
            if (con != null) {
                con.disconnect();
            }
        }
    }

}
