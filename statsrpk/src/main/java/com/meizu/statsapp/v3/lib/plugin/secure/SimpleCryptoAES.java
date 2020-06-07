package com.meizu.statsapp.v3.lib.plugin.secure;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by huchen on 16-10-17.
 * 为本机数据库中的事件加密解密
 */
public class SimpleCryptoAES {
    private static SimpleCryptoAES instance;
    private static final Object lock = new Object();

    private Context context;
    private byte[] salt;
    private byte[] iv;

    private SimpleCryptoAES(Context context) {
        this.context = context;
        salt = readFromPreferenceOrCreateRandom("salt", KEY_SIZE);
        iv = readFromPreferenceOrCreateRandom("iv", IV_SIZE);
    }

    public static void init(Context context) {
        if (instance == null) {
            synchronized (lock) {
                if (null == instance) {
                    instance = new SimpleCryptoAES(context);
                }
            }
        }
    }

    public static SimpleCryptoAES get() {
        if (instance == null) {
            throw new IllegalStateException(
                    "KeyMgr is not initialised - invoke " +
                            "at least once with parameterised init/get");
        }
        return instance;
    }

    /**
     * @param
     * @return AES算法加密
     * @throws Exception
     */
    public String encrypt(String seedText, String clearText, int encryptType)
            throws Exception {
        if (encryptType == 1) {
            SecretKey skey = derivesKeyWithSHA1PRNG(seedText.getBytes());
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            byte[] encrypted = cipher.doFinal(clearText.getBytes());
            return toHex(encrypted);
        } else if (encryptType == 2) {
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
            //Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey skey = deriveKeySecurely(seedText, KEY_SIZE);
            cipher.init(Cipher.ENCRYPT_MODE, skey, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(clearText.getBytes());
            return toHex(encrypted);
        }
        return null;
    }
    /**
     * @param
     * @return AES算法解密
     * @throws Exception
     */
    public String decrypt(String seedText, String encryptedText, int encryptType)
            throws Exception {
        if (encryptType == 1) {
            SecretKey skey = derivesKeyWithSHA1PRNG(seedText.getBytes());
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skey);
            byte[] encrypted = toByte(encryptedText);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        } else if (encryptType == 2) {
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
            //Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey skey = deriveKeySecurely(seedText, KEY_SIZE);
            cipher.init(Cipher.DECRYPT_MODE, skey, new IvParameterSpec(iv));
            byte[] encrypted = toByte(encryptedText);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        }
        return null;
    }

    private static String toHex(byte[] buf) {
        final String HEX = "0123456789ABCDEF";
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            result.append(HEX.charAt((buf[i] >> 4) & 0x0f)).append(
                    HEX.charAt(buf[i] & 0x0f));
        }
        return result.toString();
    }
    private static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    private static byte[] deriveRawKeyWithSHA1PRNG(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(seed);
        kgen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        return skey.getEncoded();
    }
    private static SecretKey derivesKeyWithSHA1PRNG(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(seed);
        kgen.init(128, sr); // 192 and 256 bits may not be available
        return kgen.generateKey();
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static SecretKey deriveKeyInsecurely(String password, int keySizeInBytes) {
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(
                InsecureSHA1PRNGKeyDerivator.deriveInsecureKey(passwordBytes, keySizeInBytes),
                "AES");
    }
    private SecretKey deriveKeySecurely(String password, int keySizeInBytes) {
        // Use this to derive the key from the password:
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
                100 /* iterationCount */, keySizeInBytes * 8 /* key size in bits */);
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Deal with exceptions properly!", e);
        }
    }

    private final String PREFERENCE_SIMPLE_CRYPTO_AES_KEY_FILE = "com.meizu.statsapp.v3.simple_crypto_AES";
    private static final int KEY_SIZE = 32;
    private static final int IV_SIZE = 16;
    private byte[] readFromPreferenceOrCreateRandom(String key, int size) {
        byte[] bytes;
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SIMPLE_CRYPTO_AES_KEY_FILE, Context.MODE_PRIVATE);
        String value = sp.getString(key, "");
        if (!TextUtils.isEmpty(value)) {
            bytes = Base64.decode(value, Base64.NO_WRAP);
            if (bytes.length == size) {
                return bytes;
            }
        }
        bytes = new byte[size];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(bytes);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, Base64.encodeToString(bytes, Base64.NO_WRAP));
        editor.commit();
        return bytes;
    }

}
