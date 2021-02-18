package com.autel.sdksample;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.autel.aidl.IHardwareManager;
import com.autel.aidl.IHardwareRealTimeInterface;
import com.autel.aidl.ISerialG5_8StatusListener;
import com.autel.aidl.ISerialKeystrokeListener;
import com.autel.aidl.WIFiScanResult;
import com.autel.common.product.AutelProductType;
import com.autel.sdk.Autel;
import com.autel.sdk.ProductConnectListener;
import com.autel.sdk.product.BaseProduct;
import com.autel.sdksample.dragonfish.DFLayout;

import java.util.concurrent.atomic.AtomicBoolean;


public class ProductActivity extends AppCompatActivity {
    private static final String TAG = "ProductActivity";
    private int index;
    private long timeStamp;
    static AtomicBoolean hasInitProductListener = new AtomicBoolean(false);
    private AutelProductType currentType = AutelProductType.UNKNOWN;
    private DFLayout dfLayout;

    private IHardwareManager mService;
    private boolean mIsBound;
    private AdditionServiceConnection mServiceConnection;

    private SerialKeyStrokeCallBack mSerialKeyStrokeCallback = new SerialKeyStrokeCallBack();
    private SerialG5_8StatusCallback mSerial58gStatusCallback = new SerialG5_8StatusCallback();
    private SerialRealTimeCallback mSerialRealTimeCallback = new SerialRealTimeCallback();


    /**
     * bind service
     */
    private void doBindService() {
        mServiceConnection = new AdditionServiceConnection();
        Intent intent = new Intent();
        intent.setAction("com.autel.aidlservice.aidl");
        intent.setPackage("com.autel.basestation");
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

    }

    /**
     * unbind service
     */
    private void doUnbindService() {
        if (mIsBound) {
            unbindService(mServiceConnection);
            mServiceConnection = null;
            mIsBound = false;
        }
    }

    class AdditionServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 连接的时候获取本地代理，这样我们就可以调用 service 中的方法了。
            mService = IHardwareManager.Stub.asInterface((IBinder) service);
            mIsBound = true;
            try {
                mService.addSerialKeystrokeListener(mSerialKeyStrokeCallback);
                mService.addSerialG5_8StatusListener(mSerial58gStatusCallback);

                //设置死亡代理
                service.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "onServiceConnected: ");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mIsBound = false;
            Log.d(TAG, "onServiceDisconnected: ");
        }
    }

    /**
     * 监听Binder是否死亡
     */
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (mService == null) {
                return;
            }
            mService.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mService = null;
            //重新绑定
            doBindService();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        doUnbindService();
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_product);
        setContentView(createView(AutelProductType.DRAGONFISH));
        Log.v("productType", "ProductActivity onCreate ");
        //*/
        Autel.setProductConnectListener(new ProductConnectListener() {
            @Override
            public void productConnected(BaseProduct product) {
                Log.v("productType", "product " + product.getType());
                currentType = product.getType();
                setTitle(currentType.toString());
                setContentView(createView(currentType));
                /**
                 * 避免从WiFi切换到USB时，重新弹起MainActivity界面
                 * Avoid MainAcitivity when switch from wifi to USB
                 */
                hasInitProductListener.compareAndSet(false, true);

                BaseProduct previous = ((TestApplication) getApplicationContext()).getCurrentProduct();
                ((TestApplication) getApplicationContext()).setCurrentProduct(product);
                /**
                 * 如果产品类型发生变化，退出到该界面下
                 * If product type has changed, go back to this Activity
                 */
                if (null != previous) {
                    if (previous.getType() != product.getType()) {
                        startActivity(new Intent(getApplicationContext(), ProductActivity.class));
                    }
                }
            }


            @Override
            public void productDisconnected() {
                Log.v("productType", "productDisconnected ");
                currentType = AutelProductType.UNKNOWN;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTitle(currentType.toString());
                    }
                });
            }
        });
        /*/
        productSelector.productConnected(AutelProductType.X_STAR);
        //*/

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        }
//        FileUtils.Initialize(getAppContext(),fileConfig1,"autel288_7.cfg");
//        FileUtils.Initialize(getAppContext(),fileConfig2,"autel288_7_final.weights");
//        FileUtils.Initialize(getAppContext(),fileConfig3,"autel13.cfg");
//        FileUtils.Initialize(getAppContext(),fileConfig4,"autel13.backup");

    }

    private void initView() {

        dfLayout.getLayout().findViewById(R.id.startScan).setOnClickListener(v -> {
            try {
                mService.start5_8gPairing();

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        dfLayout.getLayout().findViewById(R.id.connectWifi).setOnClickListener(v -> {
            try {
                mService.addHardwareRealTimeListener(mSerialRealTimeCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private View createView(AutelProductType productType) {
//        switch (productType) {
//            case DRAGONFISH:
//                return new DFLayout(this).getLayout();
//            case EVO:
//                return new G2Layout(this).getLayout();
//            case EVO_2:
//                return new EVO2Layout(this).getLayout();
//            case PREMIUM:
//                return new XStarPremiumLayout(this).getLayout();

//        }
        dfLayout = new DFLayout(this);
        return dfLayout.getLayout();
    }

    public void setFrequency(View view) {
//        DspRFManager2.getInstance().bingAircraftToRemote(AircraftRole.SLAVER);
    }
    public void setMasterFequency(View view) {
//        DspRFManager2.getInstance().bingAircraftToRemote(AircraftRole.MASTER);
    }

    public void onResume() {
        super.onResume();
        setTitle(currentType.toString());
        doBindService();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (needExit) {
            hasInitProductListener.set(false);
        }
    }

    public void finish() {
        super.finish();
        Log.v("productType", "activity finish ");
    }

    private boolean needExit = false;

    public void onBackPressed() {
        if (System.currentTimeMillis() - timeStamp < 1500) {
            needExit = true;
            super.onBackPressed();
        } else {
            timeStamp = System.currentTimeMillis();
        }
    }

    public static void receiveUsbStartCommand(Context context) {
        if (hasInitProductListener.compareAndSet(false, true)) {
            Intent i = new Intent();
            i.setClass(context, ProductActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }


    private static final class SerialKeyStrokeCallBack extends ISerialKeystrokeListener.Stub{

        @Override
        public void onResponse(String event) throws RemoteException {
            Log.d(TAG,"SerialKeyStrokeCallBack "+event);
        }
    }
    private static final class SerialG5_8StatusCallback extends ISerialG5_8StatusListener.Stub{

        @Override
        public void onConnect(boolean isConnect) throws RemoteException {
            Log.d(TAG,"SerialG5_8StatusCallback "+isConnect);
        }
    }
    private static final class SerialRealTimeCallback extends IHardwareRealTimeInterface.Stub{

        @Override
        public void onRealTimeListener(String data) throws RemoteException {
            Log.d(TAG,"onRealTimeListener -> "+data);
        }
    }
}
