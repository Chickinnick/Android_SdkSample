package com.autel.sdksample.camera.fragment.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.autel.common.camera.CameraProduct;
import com.autel.common.camera.media.CameraWhiteBalanceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class WhiteBalanceTypeAdapter extends BaseAdapter {
    private List<CameraWhiteBalanceType> whiteBalanceTypes = new ArrayList<>();
    private Context mContext;
    private CameraProduct cameraProduct;

    public WhiteBalanceTypeAdapter(Context context, CameraProduct cameraProduct) {
        mContext = context;
        this.cameraProduct = cameraProduct;
        whiteBalanceTypes.addAll(Arrays.asList(cameraProduct.supportedWhiteBalanceType()));
    }

    @Override
    public int getCount() {
        return null == whiteBalanceTypes ? 0 : whiteBalanceTypes.size();
    }

    @Override
    public Object getItem(int position) {
        return whiteBalanceTypes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = null;
        if (null == convertView) {
            textView = new TextView(mContext);
            convertView = textView;
        }else{
            textView = (TextView)convertView;
        }

        textView.setText(whiteBalanceTypes.get(position).toString());

        return convertView;
    }
}
