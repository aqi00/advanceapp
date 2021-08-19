package com.example.location;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.P)
public class WifiRttActivity extends AppCompatActivity {
    private final static String TAG = "WifiRttActivity";
    private TextView tv_result; // 声明一个文本视图对象
    private WifiManager mWifiManager; // 声明一个WiFi管理器对象
    private WifiScanReceiver mWifiScanReceiver = new WifiScanReceiver(); // 声明一个WiFi扫描接收器对象
    private WifiRttManager mRttManager; // 声明一个RTT管理器对象
    private WifiRttReceiver mWifiRttReceiver = new WifiRttReceiver(); // 声明一个RTT接收器对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_rtt);
        tv_result = findViewById(R.id.tv_result);
        findViewById(R.id.btn_indoor_rtt).setOnClickListener(v -> mWifiManager.startScan());
        // 从系统服务中获取WiFi管理器
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // 从系统服务中获取RTT管理器
        mRttManager = (WifiRttManager) getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)) {
            tv_result.setText("当前设备支持室内WiFi定位");
        } else {
            tv_result.setText("当前设备不支持室内WiFi定位");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filterScan = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mWifiScanReceiver, filterScan); // 注册Wifi扫描的广播接收器
        IntentFilter filterRtt = new IntentFilter(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED);
        registerReceiver(mWifiRttReceiver, filterRtt); // 注册RTT状态变更的广播接收器
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mWifiScanReceiver); // 注销Wifi扫描的广播接收器
        unregisterReceiver(mWifiRttReceiver); // 注销RTT状态变更的广播接收器
    }

    // 定义一个扫描周边WiFi的广播接收器
    private class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取WiFi扫描的结果列表
            List<ScanResult> scanList = mWifiManager.getScanResults();
            if (scanList != null) {
                // 查找符合80211标准的WiFi路由器集合
                List<ScanResult> m80211mcList = find80211mcResults(scanList);
                runOnUiThread(() -> {
                    String desc = String.format("找到%d个Wifi热点，其中有%d个支持RTT。",
                            scanList.size(), m80211mcList.size());
                    tv_result.setText(desc);
                });
                if (m80211mcList.size() > 0) {
                    rangingRtt(m80211mcList.get(0)); // 测量与RTT节点之间的距离
                }
            }
        }
    }

    // 查找符合80211标准的WiFi路由器集合
    private List<ScanResult> find80211mcResults(List<ScanResult> originList) {
        List<ScanResult> resultList = new ArrayList<>();
        for (ScanResult scanResult : originList) { // 遍历扫描发现的WiFi列表
            if (scanResult.is80211mcResponder()) { // 符合80211标准
                resultList.add(scanResult);
            }
        }
        return resultList;
    }

    // 测量与RTT节点之间的距离
    private void rangingRtt(ScanResult scanResult) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tv_result.setText("请先授予定位权限");
            return;
        }
        RangingRequest.Builder builder = new RangingRequest.Builder();
        builder.addAccessPoint(scanResult); // 添加测距入口，参数为ScanResult类型
//            builder.addWifiAwarePeer(); // MacAddress类型
        RangingRequest request = builder.build();
        // 开始测量当前设备与指定RTT节点（路由器）之间的距离
        mRttManager.startRanging(request, getMainExecutor(), new RangingResultCallback() {
            // 测距失败时触发
            @Override
            public void onRangingFailure(int code) {
                Log.d(TAG, "RTT扫描失败，错误代码为"+code);
            }

            // 测距成功时触发
            @Override
            public void onRangingResults(List<RangingResult> results) {
                for (RangingResult result : results) {
                    if (result.getStatus() == RangingResult.STATUS_SUCCESS
                            && scanResult.BSSID.equals(result.getMacAddress().toString())) {
                        result.getDistanceMm(); // 获取当前设备与路由器之间的距离（单位毫米）
                        result.getDistanceStdDevMm(); // 获取测量偏差（单位毫米）
                        result.getRangingTimestampMillis(); // 获取测量耗时（单位毫秒）
                        result.getRssi(); // 获取路由器的信号
                    }
                }
            }
        });
    }

    private class WifiRttReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mRttManager.isAvailable()) {
                runOnUiThread(() -> tv_result.setText("室内WiFi定位功能可以使用"));
            } else {
                runOnUiThread(() -> tv_result.setText("室内WiFi定位功能不可使用"));
            }
        }
    }

}