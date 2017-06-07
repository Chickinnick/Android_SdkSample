package com.autel.sdksample.camera.fragment.adapter;

import android.content.Context;

import com.autel.common.camera.media.CameraExposureCompensation;
import com.autel.sdksample.adapter.SelectorAdapter;

public class ExposureValueAdapter extends SelectorAdapter<CameraExposureCompensation> {


    public ExposureValueAdapter(Context context) {
        super(context);
        elementList.add(CameraExposureCompensation.POSITIVE_3p0);
        elementList.add(CameraExposureCompensation.POSITIVE_2p7);
        elementList.add(CameraExposureCompensation.POSITIVE_2p3);
        elementList.add(CameraExposureCompensation.POSITIVE_2p0);
        elementList.add(CameraExposureCompensation.POSITIVE_1p7);
        elementList.add(CameraExposureCompensation.POSITIVE_1p3);
        elementList.add(CameraExposureCompensation.POSITIVE_1p0);
        elementList.add(CameraExposureCompensation.POSITIVE_0p7);
        elementList.add(CameraExposureCompensation.POSITIVE_0p3);
        elementList.add(CameraExposureCompensation.POSITIVE_0);
        elementList.add(CameraExposureCompensation.NEGATIVE_0p3);
        elementList.add(CameraExposureCompensation.NEGATIVE_0p7);
        elementList.add(CameraExposureCompensation.NEGATIVE_1p0);
        elementList.add(CameraExposureCompensation.NEGATIVE_1p3);
        elementList.add(CameraExposureCompensation.NEGATIVE_1p7);
        elementList.add(CameraExposureCompensation.NEGATIVE_2p0);
        elementList.add(CameraExposureCompensation.NEGATIVE_2p3);
        elementList.add(CameraExposureCompensation.NEGATIVE_2p7);
        elementList.add(CameraExposureCompensation.NEGATIVE_3p0);
    }
}
