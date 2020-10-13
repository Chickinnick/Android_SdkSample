package com.autel.sdksample.rtk;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.autel.common.CallbackWithOneParam;
import com.autel.common.error.AutelError;
import com.autel.common.flycontroller.AuthInfo;
import com.autel.sdksample.R;
import com.qxwz.sdk.configs.AccountInfo;
import com.qxwz.sdk.configs.OssConfig;
import com.qxwz.sdk.configs.SDKConfig;
import com.qxwz.sdk.core.CapInfo;
import com.qxwz.sdk.core.Constants;
import com.qxwz.sdk.core.IRtcmSDKCallback;
import com.qxwz.sdk.core.RtcmSDKManager;
import com.qxwz.sdk.types.KeyType;

import java.util.List;

import static com.qxwz.sdk.core.Constants.QXWZ_SDK_CAP_ID_NOSR;
import static com.qxwz.sdk.core.Constants.QXWZ_SDK_STAT_AUTH_SUCC;

/**
 * 说明：待飞机连接上后，先从飞机获取千寻账号信息{flyController.getRtkAuthInfo}，
 * 再调用千寻sdk初始化,成功后将回调{public void onData(int type, byte[] bytes)}后的差分数据
 * 通过{flyController.sendRtkData(bytes)}下发给飞行器
 */

public class Evo2RTKActivity extends RtkBaseActivity implements IRtcmSDKCallback {
    private static final String TAG = "Evo2RTKActivity";

    private boolean isStart = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        findViewById(R.id.getAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRtkAuthInfo();
            }
        });
    }
    protected void getRtkAuthInfo(){
        if(flyController != null) {
            flyController.getRtkAuthInfo(new CallbackWithOneParam<AuthInfo>() {
                @Override
                public void onSuccess(AuthInfo info) {
                    authInfo = info;
                    logOut("getRtkAuthInfo  " + info.key+" "+info.secret+" "+info.deviceId+" "+info.deviceType);
                    if(null != authInfo) {
                        init();
                    }
                }

                @Override
                public void onFailure(AutelError autelError) {
                    logOut("getRtkAuthInfo onFailure " + autelError);
                }
            });
        }
    }

    private void init(){

        AccountInfo accountInfo = AccountInfo.builder().setKeyType(KeyType.QXWZ_SDK_KEY_TYPE_DSK).setKey(authInfo.key)
                .setSecret(authInfo.secret).setDeviceId(authInfo.deviceId).setDeviceType(authInfo.deviceType).build();

        OssConfig ossConfig = OssConfig.builder().setHeartbeatInterval(30).setRetryInterval(20).build();

        SDKConfig sdkConfig = SDKConfig.builder()
                .setAccountInfo(accountInfo).setRtcmSDKCallback(this)/*.setServerInfo(serverInfo)*/.setOssConfig(ossConfig)
                .build();
        int itCode = RtcmSDKManager.getInstance().init(sdkConfig);
        int authCode = RtcmSDKManager.getInstance().auth();

        Log.d(TAG, "itCode is " + itCode+" authCode is "+authCode);

    }


    @Override
    protected int getCustomViewResId() {
        return R.layout.activity_rtk;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        isStart = false;
        RtcmSDKManager.getInstance().stop(QXWZ_SDK_CAP_ID_NOSR);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RtcmSDKManager.getInstance().cleanup();
    }

    @Override
    public void onData(int type, byte[] bytes) {
        logOut("接收到千寻差分数据 "+bytes.length);
//        Log.d(TAG, "rtcm data received, data length is " + bytes.length);
        if(null != flyController){
            flyController.sendRtkData(bytes);
        }
    }

    @Override
    public void onStatus(int status) {
        Log.d(TAG, "status changed to " + status);
    }

    @Override
    public void onAuth(int code, List<CapInfo> caps) {
        if (code == QXWZ_SDK_STAT_AUTH_SUCC) {
            Log.d(TAG, "auth successfully.");
            for (CapInfo capInfo : caps) {
                Log.d(TAG, "capInfo:" + capInfo.toString());
            }
            /* if you want to call the start api in the callback function, you must invoke it in a new thread. */
            new Thread() {
                public void run() {
                    RtcmSDKManager.getInstance().start(QXWZ_SDK_CAP_ID_NOSR);
                }
            }.start();
        } else {
            Log.d(TAG, "failed to auth, code is " + code);
        }
    }

    @Override
    public void onStart(int code, int capId) {
        if (code == Constants.QXWZ_SDK_STAT_CAP_START_SUCC) {
            Log.d(TAG, "start successfully.");
            isStart = true;
            new Thread() {
                public void run() {
                    while (isStart) {
                        RtcmSDKManager.getInstance().sendGga(GGA);
                        SystemClock.sleep(1000);
                    }
                }
            }.start();
        } else {
            Log.d(TAG, "failed to start, code is " + code);
        }
    }


}
