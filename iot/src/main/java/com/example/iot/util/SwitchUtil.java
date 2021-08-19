package com.example.iot.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class SwitchUtil {
    private static final String TAG = "SwitchUtil";

    // 获取定位功能的开关状态
    public static boolean getLocationStatus(Context ctx) {
        // 从系统服务中获取定位管理器
        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // 检查定位功能是否打开，若未打开则跳到系统的定位功能设置页面
    public static void checkLocationIsOpen(Context ctx, String hint) {
        if (!getLocationStatus(ctx)) {
            Toast.makeText(ctx, hint, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            ctx.startActivity(intent);
        }
    }

    // 设置亮度自动调节的开关
    public static void setAutoBrightStatus(Context ctx, boolean enabled) {
        int screenMode = (enabled) ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        Settings.System.putInt(ctx.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, screenMode);
    }

    // 获取亮度自动调节的状态
    public static boolean getAutoBrightStatus(Context ctx) {
        int screenMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        try {
            screenMode = Settings.System.getInt(ctx.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
    }

    // 检查是否允许修改系统设置
    public static boolean checkWriteSettings(Activity act, int requestCode) {
        Log.d(TAG, "checkWriteSettings:");
        boolean result = true;
        // 只对Android6.0及Android7.0系统进行校验
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // 检查当前App是否允许修改系统设置
            if(!Settings.System.canWrite(act)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + act.getPackageName()));
                act.startActivityForResult(intent, requestCode);
                Toast.makeText(act, "需要允许设置权限才能调节亮度噢", Toast.LENGTH_SHORT).show();
                result = false;
            }
        }
        return result;
    }

}
