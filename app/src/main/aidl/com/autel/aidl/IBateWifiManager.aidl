// IBateWifiManager.aidl
package com.autel.aidl;
import com.autel.aidl.WIFiScanResult;
//import android.net.wifi.ScanResult;
import java.util.List;
// Declare any non-default types here with import statements


interface IBateWifiManager {
    void startScan();
    void connect(in WIFiScanResult ssid,String pwd);
}
