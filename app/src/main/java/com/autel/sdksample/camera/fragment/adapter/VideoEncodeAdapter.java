package com.autel.sdksample.camera.fragment.adapter;

import android.content.Context;

import com.autel.common.camera.media.VideoEncodeFormat;
import com.autel.sdksample.adapter.SelectorAdapter;


public class VideoEncodeAdapter extends SelectorAdapter<VideoEncodeFormat> {

    public VideoEncodeAdapter(Context context) {
        super(context);
        elementList.add(VideoEncodeFormat.H264);
        elementList.add(VideoEncodeFormat.H265);
    }
}
