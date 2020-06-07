package com.meizu.statsapp.v3.lib.plugin.emitter;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by huchen on 16-9-7.
 */
public class EmitterConfig implements Parcelable {

    boolean active = true;
    boolean sampling; //已废弃
    boolean flushOnStart = true;
    boolean flushOnCharge = true;
    boolean flushOnReconnect = true;
    long flushDelayInterval = 30 * 60 * 1000; //单位毫秒
    int flushCacheLimit = 50; //默认50条一批次
    long flushMobileTrafficLimit = 2 * 1024 * 1024; //默认2兆的数据流量
    int neartimeInterval = 10; //单位秒

    private String pkgKey;

    //默认值应该从configController中取，这里只是防止异常
    //pkgKey是必须的，所以这里放在构造参数
    public EmitterConfig(String pkgKey) {
        this.pkgKey = pkgKey;
    }

    public String getPkgKey() {
        return pkgKey;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isFlushOnStart() {
        return flushOnStart;
    }

    public boolean isFlushOnCharge() {
        return flushOnCharge;
    }

    public boolean isFlushOnReconnect() {
        return flushOnReconnect;
    }

    public long getFlushDelayInterval() {
        return flushDelayInterval;
    }

    public int getFlushCacheLimit() {
        return flushCacheLimit;
    }

    public long getFlushMobileTrafficLimit() {
        return flushMobileTrafficLimit;
    }

    public int getNeartimeInterval() {
        return neartimeInterval;
    }

    @Override
    public String toString() {
        return "EmitterConfig{" +
                "active=" + active +
                ", flushOnStart=" + flushOnStart +
                ", flushOnCharge=" + flushOnCharge +
                ", flushOnReconnect=" + flushOnReconnect +
                ", flushDelayInterval=" + flushDelayInterval +
                ", flushCacheLimit=" + flushCacheLimit +
                ", flushMobileTrafficLimit=" + flushMobileTrafficLimit +
                ", neartimeInterval=" + neartimeInterval +
                ", pkgKey='" + pkgKey + '\'' +
                '}';
    }

    protected EmitterConfig(Parcel in) {
        sampling = in.readByte() != 0;
        active = in.readByte() != 0;
        flushOnStart = in.readByte() != 0;
        flushOnCharge = in.readByte() != 0;
        flushOnReconnect = in.readByte() != 0;
        flushDelayInterval = in.readLong();
        flushCacheLimit = in.readInt();
        flushMobileTrafficLimit = in.readLong();
        pkgKey = in.readString();
    }

    public static final Creator<EmitterConfig> CREATOR = new Creator<EmitterConfig>() {
        @Override
        public EmitterConfig createFromParcel(Parcel in) {
            return new EmitterConfig(in);
        }

        @Override
        public EmitterConfig[] newArray(int size) {
            return new EmitterConfig[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (sampling ? 1 : 0));
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeByte((byte) (flushOnStart ? 1 : 0));
        dest.writeByte((byte) (flushOnCharge ? 1 : 0));
        dest.writeByte((byte) (flushOnReconnect ? 1 : 0));
        dest.writeLong(flushDelayInterval);
        dest.writeInt(flushCacheLimit);
        dest.writeLong(flushMobileTrafficLimit);
        dest.writeString(pkgKey);
    }
}
