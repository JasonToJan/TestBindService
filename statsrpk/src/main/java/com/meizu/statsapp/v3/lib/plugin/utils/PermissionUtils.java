package com.meizu.statsapp.v3.lib.plugin.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import com.meizu.statsapp.v3.utils.log.Logger;

/**
 * Created by huchen on 16-8-16.
 */
public class PermissionUtils {
    private final static String TAG = PermissionUtils.class.getSimpleName();


    public static boolean checkInternetPermission(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        final String packageName = context.getPackageName();

        if (packageManager == null || packageName == null) {
            Logger.e(TAG, "Can't check configuration when using a Context with null packageManager or packageName");
            return false;
        }
        if (PackageManager.PERMISSION_GRANTED != packageManager.checkPermission("android.permission.INTERNET", packageName)) {
            Logger.e(TAG, "Package does not have permission android.permission.INTERNET - usage will not work at all!");
            Logger.e(TAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n" +
                    "<uses-permission android:name=\"android.permission.INTERNET\" />");
            return false;
        }
        return true;
    }
}
