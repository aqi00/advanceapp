package com.example.location;

import com.example.location.util.IPv4Util;
import com.example.location.util.NetUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.TextView;

@SuppressLint("DefaultLocale")
public class WifiInfoActivity extends AppCompatActivity {
    private static final String TAG = "WifiInfoActivity";
    private TextView tv_info; // 声明一个文本视图对象
    private Handler mHandler = new Handler(Looper.myLooper()); // 声明一个处理器对象
    private String[] mWifiStateArray = {"正在断开", "已断开", "正在连接", "已连接", "未知"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_info);
        tv_info = findViewById(R.id.tv_info);
        mHandler.postDelayed(mRefresh, 50); // 延迟50毫秒后启动网络刷新任务
    }

    // 定义一个网络刷新任务
    private Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            getAvailableNet(); // 获取可用的网络信息
            // 延迟1秒后再次启动网络刷新任务
            mHandler.postDelayed(this, 1000);
        }
    };

    // 获取可用的网络信息
    private void getAvailableNet() {
        String desc = "";
        // 从系统服务中获取电话管理器
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // 从系统服务中获取连接管理器
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // 通过连接管理器获得可用的网络信息
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.getState() == NetworkInfo.State.CONNECTED) { // 有网络连接
            if (info.getType() == ConnectivityManager.TYPE_WIFI) { // WiFi网络（无线热点）
                // 从系统服务中获取无线网络管理器
                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                int state = wm.getWifiState(); // 获得无线网络的状态
                WifiInfo wifiInfo = wm.getConnectionInfo(); // 获得无线网络信息
                String SSID = wifiInfo.getSSID(); // 获得无线网络的名称
                if (TextUtils.isEmpty(SSID) || SSID.contains("unknown")) {
                    desc = "\n当前联网的网络类型是WiFi，但未成功连接已知的WiFi信号";
                } else {
                    desc = String.format("当前联网的网络类型是WiFi，状态是%s。\nWiFi名称是：%s\n路由器MAC是：%s\nWiFi信号强度是：%d\n连接速率是：%s\n手机的IP地址是：%s\n手机的MAC地址是：%s\n网络编号是：%s\n",
                            mWifiStateArray[state], SSID, wifiInfo.getBSSID(),
                            wifiInfo.getRssi(), wifiInfo.getLinkSpeed(),
                            IPv4Util.intToIp(wifiInfo.getIpAddress()),
                            wifiInfo.getMacAddress(), wifiInfo.getNetworkId());
                }
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) { // 移动网络（数据连接）
                int net_type = info.getSubtype();
                desc = String.format("\n当前联网的网络类型是%s %s",
                        NetUtil.getNetworkTypeName(tm, net_type),
                        NetUtil.getClassName(tm, net_type));
            } else {
                desc = String.format("\n当前联网的网络类型是%d", info.getType());
            }
        } else { // 无网络连接
            desc = "\n当前无上网连接";
        }
        tv_info.setText(desc);
    }

}
