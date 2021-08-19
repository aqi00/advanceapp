package com.example.video;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.video.service.CaptureService;
import com.example.video.service.RecordService;
import com.example.video.util.AuthorityUtil;

public class ScreenCaptureActivity extends AppCompatActivity {
    private static final String TAG = "ScreenCaptureActivity";
    private MediaProjectionManager mMpMgr; // 声明一个媒体投影管理器对象
    private int REQUEST_PROJECTION = 100; // 媒体投影授权的请求代码
    private Intent mResultIntent = null; // 结果意图
    private int mResultCode = 0; // 结果代码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_capture);
        findViewById(R.id.btn_start_capture).setOnClickListener(v -> {
            // 停止录屏服务
            stopService(new Intent(this, RecordService.class));
            startCaptureService(); // 启动截图服务
        });
        initMediaProjection(); // 初始化媒体投影
    }

    // 初始化媒体投影
    private void initMediaProjection() {
        // 从系统服务中获取媒体投影管理器
        mMpMgr = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        // 从全局变量中获取结果意图
        mResultIntent = MainApplication.getInstance().getResultIntent();
        // 从全局变量中获取结果代码
        mResultCode = MainApplication.getInstance().getResultCode();
    }

    // 启动截图服务
    private void startCaptureService() {
        if (mResultIntent != null && mResultCode != 0) { // 不是首次截图或录屏
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                startService(new Intent(this, CaptureService.class)); // 启动截图服务
            } else {
                // 启动前台的截图服务（从Android10开始，媒体投影操作必须在前台服务中运行）
                startForegroundService(new Intent(this, CaptureService.class));
            }
        } else { // 是首次截图或录屏
            try {
                // 弹出授权截图的对话框
                startActivityForResult(mMpMgr.createScreenCaptureIntent(), REQUEST_PROJECTION);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "当前系统不支持截屏功能", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 从授权截图的对话框返回时触发
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode==REQUEST_PROJECTION && resultCode==RESULT_OK) { // 允许授权
            // AppOpsManager.OP_SYSTEM_ALERT_WINDOW是隐藏变量（值为24），不能直接引用
            if (!AuthorityUtil.checkOp(this, 24)) { // 未开启悬浮窗权限
                Toast.makeText(this, "截屏功能需要开启悬浮窗权限", Toast.LENGTH_SHORT).show();
                // 跳到悬浮窗权限的设置页面
                AuthorityUtil.requestAlertWindowPermission(this);
            } else { // 已开启悬浮窗权限
                mResultCode = resultCode;
                mResultIntent = data;
                // 下面把结果代码、结果意图等等信息保存到全局变量中
                MainApplication.getInstance().setResultCode(resultCode);
                MainApplication.getInstance().setResultIntent(data);
                MainApplication.getInstance().setMpMgr(mMpMgr);
                startCaptureService(); // 启动截图服务
            }
        }
    }

}
