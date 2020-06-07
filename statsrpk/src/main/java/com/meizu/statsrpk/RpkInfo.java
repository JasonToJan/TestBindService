package com.meizu.statsrpk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jinhui on 18-7-31.
 */

public class RpkInfo implements Parcelable {
    public String rpkPkgName;
    public String rpkVer;
    public int rpkVerCode;
    public String apkPkgName;
    public String appKey;

    public RpkInfo() {}

    protected RpkInfo(Parcel in) {
        rpkPkgName = in.readString();
        rpkVer = in.readString();
        rpkVerCode = in.readInt();
        apkPkgName = in.readString();
        appKey = in.readString();
    }

    public static final Creator<RpkInfo> CREATOR = new Creator<RpkInfo>() {
        @Override
        public RpkInfo createFromParcel(Parcel in) {
            return new RpkInfo(in);
        }

        @Override
        public RpkInfo[] newArray(int size) {
            return new RpkInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(rpkPkgName);
        dest.writeString(rpkVer);
        dest.writeInt(rpkVerCode);
        dest.writeString(apkPkgName);
        dest.writeString(appKey);
    }

    @Override
    public String toString() {
        return "[" + rpkPkgName + "," + rpkVer + "," + rpkVerCode + "," + apkPkgName + "," + appKey + "]";
    }
}
