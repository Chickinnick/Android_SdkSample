package com.autel.aidl;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class WIFiScanResult implements Parcelable {

    /**
     * The network name.
     */
    public String SSID;

    public WIFiScanResult(String ssid){
        this.SSID = ssid;
    }

    protected WIFiScanResult(Parcel in) {
        SSID = in.readString();
    }

    public static final Creator<WIFiScanResult> CREATOR = new Creator<WIFiScanResult>() {
        @Override
        public WIFiScanResult createFromParcel(Parcel in) {
            return new WIFiScanResult(in);
        }

        @Override
        public WIFiScanResult[] newArray(int size) {
            return new WIFiScanResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SSID);
    }
}
