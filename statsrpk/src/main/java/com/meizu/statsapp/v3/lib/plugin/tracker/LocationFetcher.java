package com.meizu.statsapp.v3.lib.plugin.tracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.utils.log.Logger;

/**
 * Created by huchen on 16-8-4.
 */
public class LocationFetcher {
    private static String TAG = "LocationFetcher";
    private boolean enable;
    private long fetchTime;
    private Context context;
    SharedPreferences sp;

    public LocationFetcher(Context context) {
        this.context = context;
        sp = context.getSharedPreferences(UxipConstants.PREFERENCES_COMMON_NAME, Context.MODE_PRIVATE);
    }

    public void setInterval(long intervalInMills) {
        Logger.d(TAG, "setInterval intervalInMills: " + intervalInMills);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(UxipConstants.PREFERENCES_KEY_POSITION_INTERVAL, intervalInMills);
        editor.apply();
    }

    public void setEnable(boolean enable) {
        Logger.d(TAG, "setReportLocation enable: " + enable);
        this.enable = enable;
    }

    public Location getLocation() {
        if (enable) {
            long now = System.currentTimeMillis();
            //if (fetchTime == 0 || Math.abs(now - fetchTime) > sp.getLong(UxipConstants.PREFERENCES_KEY_POSITION_INTERVAL, 4 * 3600 * 1000)) { //默认4小时间隔
                //fetchTime = now;
                return getLocation(context);
            //}
        }
        return null;
    }

    /**
     * Returns the location of the android
     * device.
     *
     * @param context the android context
     * @return the phones Location
     */
    private Location getLocation(Context context) {
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                Logger.d(TAG, "Location found:" + location);
                return location;
            } catch (SecurityException ex) {
                Logger.e(TAG, "Security exception:" + ex.toString());
            } catch (ClassCastException e) { //OUC, java.lang.ClassCastException: Couldn't convert result of type java.lang.String to android.location.Location
                Logger.e(TAG, "ClassCastException:" + e.toString());
            } catch (NullPointerException e){
                Logger.e(TAG, "NullPointerException:" + e.toString());
            }
        } else {
            Logger.e(TAG, "Location Manager provider is null.");
        }
        return null;
    }

}
