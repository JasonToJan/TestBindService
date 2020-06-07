package com.meizu.statsapp.v3.utils.log;

import android.util.Log;

import com.meizu.statsapp.v3.utils.CommonUtils;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by huchen on 15-11-4.
 */
public class EncryptLogger implements ILog {
    private static final String TAG = "EncryptLogger";
    private final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private final String FILE_NAME = "usage_logs_v2.txt";
    private final String OLD_FILE_NAME = "usage_logs_v2_old.txt";
    private String mPath;
    private File mLogFile;
    private EncryptBase64 mEncryptor;
    private SimpleDateFormat mDateformat;
    private int mMyPid;

    public EncryptLogger(String dir) {
        mPath = dir;
        File file = new File(dir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        mLogFile = new File(dir, FILE_NAME);
        mEncryptor = new EncryptBase64("lo");
        //mDateformat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        mDateformat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        mMyPid = android.os.Process.myPid();
    }

    @Override
    public void print(LogLevel level, String tag, String msg, long tid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(mDateformat.format(new Date()));
            sb.append("\t");
            sb.append(mMyPid);
            sb.append("-");
            sb.append(tid);
            sb.append("\t");
            sb.append(level == LogLevel.DEBUG ? "D" :
                    (level == LogLevel.INFO ? "I" :
                            (level == LogLevel.WARN ? "W" : "E")));
            sb.append("/");
            sb.append(tag);
            sb.append(": ");
            sb.append(msg);
            String encryptLog = mEncryptor.encode(sb.toString().getBytes(Charset.forName("UTF-8")));

            boolean append = true;
            if (mLogFile.exists()) {
                long fileSize = mLogFile.length();
                if (fileSize + encryptLog.getBytes().length > MAX_FILE_SIZE) {
                    String parent = mLogFile.getParent();
                    File oldFile = new File(parent, OLD_FILE_NAME);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                    mLogFile.renameTo(new File(parent, OLD_FILE_NAME));
                    mLogFile = new File(mPath, FILE_NAME);
                }
            }

            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(mLogFile, append);
                fileWriter.write(encryptLog);
                fileWriter.write("\r\n");
            } catch (Exception e) {
            } finally {
                CommonUtils.closeQuietly(fileWriter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
    }
}
