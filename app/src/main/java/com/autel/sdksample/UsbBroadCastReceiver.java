package com.autel.sdksample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.autel.sdksample.mission.MapActivity;

public class UsbBroadCastReceiver extends BroadcastReceiver {
    final String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "action " + intent.getAction());
        Intent i = new Intent();
        i.setClass(context, MapActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}

