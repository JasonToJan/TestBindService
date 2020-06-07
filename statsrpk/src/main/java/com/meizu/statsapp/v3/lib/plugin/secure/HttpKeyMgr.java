package com.meizu.statsapp.v3.lib.plugin.secure;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.utils.CommonUtils;
import com.meizu.statsapp.v3.utils.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by jinhui on 17-3-17.
 */

public class HttpKeyMgr {
    private final static String TAG = "HttpKeyMgr";

    private static HttpKeyMgr instance;
    private static final Object lock = new Object();

    private byte[] rKey, aKey;
    private byte[] rKey64, aKey64, sKey64;
    private X509Certificate cert;

    private SharedPreferences spKey;
    private SharedPreferences spCert;
    private final String PREFERENCE_HTTPKEY_FILE = "com.meizu.statsapp.v3.httpkey";
    private final String PREFERENCE_HTTPCERT_FILE = "com.meizu.statsapp.v3.httpcert";

    private long lastResetKeysTime = 0;
    private Context mContext;

    private HttpKeyMgr(Context context) {
        this.mContext = context;
        spKey = context.getSharedPreferences(PREFERENCE_HTTPKEY_FILE, Context.MODE_PRIVATE);
        spCert = context.getSharedPreferences(PREFERENCE_HTTPCERT_FILE, Context.MODE_PRIVATE);

        loadKeys();
        if (rKey == null || (rKey != null && rKey.length == 0)) { //如果本地没有rKey，生成随机rKey
            //从本地加载证书
            initCert(mContext);
            if (cert != null) {
                generateKeys();
            } else {
                spKey.edit().clear().apply();
                spCert.edit().clear().apply();
                //证书失效，需要从服务器下载新的
//                try {
//                    downloadAndInitCert();
//                    if (cert != null) {
//                        generateKeys();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        } else {
            if (aKey == null || (aKey != null && aKey.length == 0)) { //rKey存在，aKey不存在
                initCert(mContext);
                if (cert != null) {
                    generateAkey();
                }
            }
        }
    }

    public static void init(Context context) {
        if (instance == null) {
            synchronized (lock) {
                if (null == instance) {
                    instance = new HttpKeyMgr(context);
                }
            }
        }
    }

    public static HttpKeyMgr get() {
        if (instance == null) {
            throw new IllegalStateException(
                    "KeyMgr is not initialised - invoke " +
                            "at least once with parameterised init/get");
        }
        return instance;
    }

    //加载证书
    private void initCert(Context context) {
        //先加载preference里面保存的证书，如果存在
        Logger.d(TAG,"load certs from preference");
        String save_certs = spCert.getString("certificates", "");
        if (!TextUtils.isEmpty(save_certs)) {
            InputStream ims = new ByteArrayInputStream(save_certs.getBytes());
            if (ims != null) {
                try {
                    loadAvailableCertWithoutVerify(ims);
                } catch (CertificateException e) {
                    //e.printStackTrace();
                    sLogE("load Certificates from preference Exception, " + e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    //e.printStackTrace();
                    sLogE("load Certificates from preference Exception, " + e.getMessage());
                } catch (SignatureException e) {
                    //e.printStackTrace();
                    sLogE("load Certificates from preference Exception, " + e.getMessage());
                } catch (NoSuchProviderException e) {
                    //e.printStackTrace();
                    sLogE("load Certificates from preference Exception, " + e.getMessage());
                } catch (InvalidKeyException e) {
                    //e.printStackTrace();
                    sLogE("load Certificates from preference Exception, " + e.getMessage());
                }
            }
        }

        // 从常量中加载证书
        if (cert == null) {
            try {
                Logger.d(TAG,"load certs from uxipcerts.java");
                InputStream ims = new ByteArrayInputStream(UxipCerts.raw.getBytes());
                loadAvailableCertWithoutVerify(ims);
            } catch (CertificateException e) {
                //e.printStackTrace();
                sLogE("load Certificates from asset Exception, " + e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                //e.printStackTrace();
                sLogE("load Certificates from asset Exception, " + e.getMessage());
            } catch (SignatureException e) {
                //e.printStackTrace();
                sLogE("load Certificates from asset Exception, " + e.getMessage());
            } catch (NoSuchProviderException e) {
                //e.printStackTrace();
                sLogE("load Certificates from asset Exception, " + e.getMessage());
            } catch (InvalidKeyException e) {
                //e.printStackTrace();
                sLogE("load Certificates from asset Exception, " + e.getMessage());
            }
        }

//        // 从asset中加载证书
//        if (cert == null) {
//            try {
//                sLogD("load certs from asset");
//                AssetManager assetManager = context.getAssets();
//                InputStream ims = null;
//                try {
//                    ims = assetManager.open("uxip_cert");
//                } catch (IOException e) {
//                    //e.printStackTrace();
//                    sLogE("open asset file exception:" + e.toString());
//                }
//                loadAvailableCertWithoutVerify(ims);
//            } catch (CertificateException e) {
//                //e.printStackTrace();
//                sLogE("load Certificates from asset Exception, " + e.getMessage());
//            } catch (NoSuchAlgorithmException e) {
//                //e.printStackTrace();
//                sLogE("load Certificates from asset Exception, " + e.getMessage());
//            } catch (SignatureException e) {
//                //e.printStackTrace();
//                sLogE("load Certificates from asset Exception, " + e.getMessage());
//            } catch (NoSuchProviderException e) {
//                //e.printStackTrace();
//                sLogE("load Certificates from asset Exception, " + e.getMessage());
//            } catch (InvalidKeyException e) {
//                //e.printStackTrace();
//                sLogE("load Certificates from asset Exception, " + e.getMessage());
//            }
//        }

    }

    private void loadAvailableCert(InputStream ims) throws CertificateException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Logger.d(TAG,"loadAvailableCert");
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            Collection<X509Certificate> certs = (Collection<X509Certificate>) cf.generateCertificates(ims);
            X509Certificate[] certArray = certs.toArray(new X509Certificate[certs.size()]);
            int i = 0;
            while (i < certArray.length) {
                Logger.d(TAG,"* --------------------");
                Logger.d(TAG,"* Subject DN: " + certArray[i].getSubjectDN());
                Logger.d(TAG,"* Signature Algorithm: " + certArray[i].getSigAlgName());
                Logger.d(TAG,"* Valid from: " + certArray[i].getNotBefore());
                Logger.d(TAG,"* Valid until: " + certArray[i].getNotAfter());
                Logger.d(TAG,"* Issuer: " + certArray[i].getIssuerDN());
                Logger.d(TAG,"* PublicKey: " + certArray[i].getPublicKey());
                if (i + 1 < certArray.length) {
                    certArray[i].verify(certArray[i + 1].getPublicKey());
                } else {
                    //是否是合法证书
                    boolean valid = verify(certArray[i]);
                    if (valid) {
                        //是否还在有效期内
                        certArray[0].checkValidity();
                        cert = certArray[0];
                        Logger.d(TAG,"***** AVAILABLE CERTIFICATE:");
                        Logger.d(TAG,"***** --------------------");
                        Logger.d(TAG,"***** Subject DN: " + cert.getSubjectDN());
                        Logger.d(TAG,"***** Signature Algorithm: " + cert.getSigAlgName());
                        Logger.d(TAG,"***** Valid from: " + cert.getNotBefore());
                        Logger.d(TAG,"***** Valid until: " + cert.getNotAfter());
                        Logger.d(TAG,"***** Issuer: " + cert.getIssuerDN());
                        Logger.d(TAG,"***** PublicKey: " + cert.getPublicKey());
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_MONTH, 30);
                        if (calendar.getTime().after(cert.getNotAfter())) { //不到30天证书就要过期了，要提前去下载更新证书
                            try {
                                downloadAndInitCert();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }
                }
                i++;
            }
        } finally {
            CommonUtils.closeQuietly(ims);
        }
    }

    private void loadAvailableCertWithoutVerify(InputStream ims) throws CertificateException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Logger.d(TAG,"loadAvailableCertWithoutVerify");
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            Collection<X509Certificate> certs = (Collection<X509Certificate>) cf.generateCertificates(ims);
            X509Certificate[] certArray = certs.toArray(new X509Certificate[certs.size()]);
            cert = certArray[0];
            Logger.d(TAG,"***** AVAILABLE CERTIFICATE:");
            Logger.d(TAG,"***** --------------------");
            Logger.d(TAG,"***** Subject DN: " + cert.getSubjectDN());
            Logger.d(TAG,"***** Signature Algorithm: " + cert.getSigAlgName());
            Logger.d(TAG,"***** Valid from: " + cert.getNotBefore());
            Logger.d(TAG,"***** Valid until: " + cert.getNotAfter());
            Logger.d(TAG,"***** Issuer: " + cert.getIssuerDN());
            Logger.d(TAG,"***** PublicKey: " + cert.getPublicKey());
        } finally {
            CommonUtils.closeQuietly(ims);
        }
    }

    private boolean verify(X509Certificate cert) {
//        KeyStore keyStore = null;
//        try {
//            keyStore = KeyStore.getInstance("BKS");
//            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//            InputStream is = new FileInputStream("/etc/security/cacerts.bks");
//            keyStore.load(is, "changeit".toCharArray());
//            Enumeration enumeration = keyStore.aliases();
//            while (enumeration.hasMoreElements()) {
//                String alias = (String) enumeration.nextElement();
//                sLogD("alias name: " + alias);
//                java.security.cert.Certificate certificate = keyStore.getCertificate(alias);
//                sLogD(certificate.toString());
//
//            }
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        }

        TrustManagerFactory trustManagerFactory = null;
        TrustManager[] trustManagers = null;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        X509TrustManager xtm = (X509TrustManager) trustManagers[0];
        for (X509Certificate acceptedIssuer : xtm.getAcceptedIssuers()) {
//            String acceptedIssuerStr = "S:" + acceptedIssuer.getSubjectDN().getName() + "\nI:" + acceptedIssuer.getIssuerDN().getName();
//            sLogD("### " + acceptedIssuerStr);
            try {
                cert.verify(acceptedIssuer.getPublicKey());
                String acceptedIssuerStr = "S:" + acceptedIssuer.getSubjectDN().getName() + "\nI:" + acceptedIssuer.getIssuerDN().getName();
                Logger.d(TAG,"### root CA: " + acceptedIssuerStr);
                return true;
            } catch (CertificateException e) {
                //e.printStackTrace();
                sLogE(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                //e.printStackTrace();
                sLogE(e.getMessage());
            } catch (InvalidKeyException e) {
                //e.printStackTrace();
                sLogE(e.getMessage());
            } catch (NoSuchProviderException e) {
                //e.printStackTrace();
                sLogE(e.getMessage());
            } catch (SignatureException e) {
                //e.printStackTrace();
                sLogE(e.getMessage());
            }
        }
        return false;
    }

    private void downloadAndInitCert() throws IOException{
        HttpsURLConnection con;
        URL url = null;
        try {
            url = new URL("https://uxip.meizu.com/api/v3/certificate");
        } catch (MalformedURLException e) {
            //e.printStackTrace();
            return;
        }
        con = (HttpsURLConnection) url.openConnection();
        con.setDoInput(true);
        con.setUseCaches(false);
        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        try {
            con.setRequestProperty("Charset", "UTF-8");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        InputStream is = null;
        try {
            int code = con.getResponseCode();
            Logger.d(TAG,"code = " + code);
            String body = null;
            is = con.getInputStream();
            if (null != is) {
                body = getStringByInputStream(is);
                Logger.d(TAG,"body = " + body);
                try {
                    JSONObject jsonObject = new JSONObject(body);
                    int resultCode = jsonObject.getInt(UxipConstants.API_RESPONSE_CODE);
                    if (resultCode == 200) {
                        JSONObject value = jsonObject.getJSONObject(UxipConstants.API_RESPONSE_VALUE);
                        {
                            String certificates = value.getString("certificate");
                            InputStream ims = new ByteArrayInputStream(certificates.getBytes());
                            try {
                                loadAvailableCertWithoutVerify(ims);
                                String version = value.getString("version");
                                SharedPreferences.Editor editor = spCert.edit();
                                editor.putString("certificates", certificates);
                                editor.putString("version", version);
                                editor.apply();
                            } catch (CertificateException e) {
                                //e.printStackTrace();
                            } catch (NoSuchAlgorithmException e) {
                                //e.printStackTrace();
                            } catch (SignatureException e) {
                                //e.printStackTrace();
                            } catch (NoSuchProviderException e) {
                                //e.printStackTrace();
                            } catch (InvalidKeyException e) {
                                //e.printStackTrace();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            CommonUtils.closeQuietly(is);
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private void loadKeys() {
        Logger.d(TAG,"loadKeys");
        //检查sKey是否存在，如果存在，直接用它对称加密数据，初始化动作完成
        String save_Key = spKey.getString("sKey64", ""); //获取保存的sKey，以base64编码保存
        Logger.d(TAG,"saved sKey64: " + save_Key);
        if (!TextUtils.isEmpty(save_Key)) {
            sKey64 = save_Key.getBytes();
        }

        //检查aKey是否存在
        save_Key = spKey.getString("aKey64", ""); //获取保存的aKey
        Logger.d(TAG,"saved aKey64: " + save_Key);
        if (!TextUtils.isEmpty(save_Key)) {
            aKey64 = save_Key.getBytes();
            aKey = Base64.decode(aKey64, Base64.NO_WRAP);
        }

        //检查rKey是否存在
        save_Key = spKey.getString("rKey64", ""); //获取保存的rKey
        Logger.d(TAG,"saved rKey64: " + save_Key);
        if (!TextUtils.isEmpty(save_Key)) {
            rKey64 = save_Key.getBytes();
            rKey = Base64.decode(rKey64, Base64.NO_WRAP);
        }
    }

    //生成随机rKey, aKey
    private void generateKeys() {
        generateRkey();
        generateAkey();
    }

    private void generateRkey() {
//            try {
//                KeyGenerator kgen = KeyGenerator.getInstance("AES");
//                SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
//                long seed = System.currentTimeMillis();
//                sLogD("rKey seed: " + seed);
//                sr.setSeed(seed);
//                kgen.init(128, sr); // 192 and 256 bits may not be available
//                SecretKey secretKey = kgen.generateKey();
//                rKey = secretKey.getEncoded();
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            } catch (NoSuchProviderException e) {
//                e.printStackTrace();
//            }
        rKey = UUID.randomUUID().toString().substring(0, 16).getBytes();
        rKey64 = Base64.encode(rKey, Base64.NO_WRAP);
        Logger.d(TAG,"***** rKey64: " + new String(rKey64));
        SharedPreferences.Editor editor = spKey.edit();
        editor.putString("rKey64", new String(rKey64));
        editor.apply();
    }

    // 用公钥加密rKey生成aKey
    private void generateAkey() {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, cert.getPublicKey());
            aKey = cipher.doFinal(rKey);
            aKey64 = Base64.encode(aKey, Base64.NO_WRAP);
            Logger.d(TAG,"***** aKey64: " + new String(aKey64));
            SharedPreferences.Editor editor = spKey.edit();
            editor.putString("aKey64", new String(aKey64));
            editor.apply();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private String getStringByInputStream(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String result = null;
        try {
            int i = -1;
            while ((i = is.read()) != -1) {
                baos.write(i);
            }
            result = baos.toString();
        } catch (IOException e) {
        } finally {
           CommonUtils.closeQuietly(baos);
        }
        return result;
    }

    public byte[] encrypt(byte[] input) {
        if (rKey == null || (rKey != null && rKey.length == 0)) {
            sLogE("rKey null!");
            return null;
        }
        if (input == null || (input != null && input.length == 0)) {
            sLogE("input null!");
            return null;
        }
        Logger.d(TAG,">>>>>>>>>> encrypt input >>>>>>>>>>\n" + getBase64(input));
        Logger.d(TAG,"<<<<<<<<<< encrypt input <<<<<<<<<<");
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(rKey, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] output = cipher.doFinal(input);
            Logger.d(TAG,">>>>>>>>>> encrypt output >>>>>>>>>>\n" + getBase64(output));
            Logger.d(TAG,"<<<<<<<<<< encrypt output <<<<<<<<<<");
            return output;
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
        return null;
    }

    public byte[] decrypt(byte[] input) {
        if (rKey == null || (rKey != null && rKey.length == 0)) {
            sLogE("rKey null!");
            return null;
        }
        if (input == null || (input != null && input.length == 0)) {
            sLogE("input null!");
            return null;
        }
        Logger.d(TAG,">>>>>>>>>> decrypt input >>>>>>>>>>\n" + getBase64(input));
        Logger.d(TAG,"<<<<<<<<<< decrypt input <<<<<<<<<<");
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(rKey, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] output = cipher.doFinal(input);
            Logger.d(TAG,">>>>>>>>>> decrypt output >>>>>>>>>>\n" + getBase64(output));
            Logger.d(TAG,"<<<<<<<<<< decrypt output <<<<<<<<<<");
            return output;
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
        return null;
    }

    private String getBase64(byte[] data) {
        if (data != null && data.length > 0) {
            return new String(Base64.encode(data, Base64.DEFAULT));
        } else {
            return "";
        }
    }
    /**
     * @param x_s_key sKey的64编码
     */

    public void saveSKey(String x_s_key) {
        sKey64 = x_s_key.getBytes();
        SharedPreferences.Editor editor = spKey.edit();
        editor.putString("sKey64", new String(sKey64));
        editor.apply();
    }

    public synchronized void reInitKeys() {
        Logger.d(TAG, "reInitKeys");
        if ((lastResetKeysTime == 0) ||
                (System.currentTimeMillis() - lastResetKeysTime) > 3 * 60 * 1000) { //3分钟内不多次重置keys
            SharedPreferences.Editor editor = spKey.edit();
            editor.clear();
            editor.apply();
            lastResetKeysTime = System.currentTimeMillis();
            if (cert != null) {
                generateKeys();
            }
        }
    }

    public byte[] getaKey64() {
        return aKey64;
    }

    public byte[] getsKey64() {
        return sKey64;
    }


    private void sLogE(String msg) {
        Logger.e(TAG, msg);
    }
}
