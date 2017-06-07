package com.autel.sdksample.camera.fragment.adapter;

import android.content.Context;

import com.autel.common.camera.media.PhotoStyleType;
import com.autel.sdksample.adapter.SelectorAdapter;


public class PhotoStyleAdapter extends SelectorAdapter<PhotoStyleType> {

    public PhotoStyleAdapter(Context context) {
        super(context);
        elementList.add(PhotoStyleType.Standard);
        elementList.add(PhotoStyleType.Soft);
        elementList.add(PhotoStyleType.Landscape);
        elementList.add(PhotoStyleType.Custom);
    }
}
