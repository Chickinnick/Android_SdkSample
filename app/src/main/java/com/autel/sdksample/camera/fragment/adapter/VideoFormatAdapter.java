package com.autel.sdksample.camera.fragment.adapter;

import android.content.Context;

import com.autel.common.camera.media.VideoFormat;
import com.autel.sdksample.adapter.SelectorAdapter;


public class VideoFormatAdapter extends SelectorAdapter<VideoFormat> {

    public VideoFormatAdapter(Context context) {
        super(context);
        elementList.add(VideoFormat.MOV);
        elementList.add(VideoFormat.MP4);
    }
}
