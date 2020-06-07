package com.meizu.statsapp.v3.lib.plugin.net;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinhui on 16-11-29.
 */
public class NetResponse implements Parcelable {
    private int responseCode;
    private String responseBody;

    public String getResponseBody() {
        return responseBody;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public NetResponse(int responseCode, String responseBody) {
        this.responseCode = responseCode;
        this.responseBody = responseBody;
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        try {
            object.put("code", responseCode);
            object.put("body", responseBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "[NetResponse] " + object.toString();
    }

    protected NetResponse(Parcel in) {
        responseCode = in.readInt();
        responseBody = in.readString();
    }

    public static final Creator<NetResponse> CREATOR = new Creator<NetResponse>() {
        @Override
        public NetResponse createFromParcel(Parcel in) {
            return new NetResponse(in);
        }

        @Override
        public NetResponse[] newArray(int size) {
            return new NetResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(responseCode);
        dest.writeString(responseBody);
    }
}
