package com.example.video.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.video.MainApplication;
import com.example.video.R;
import com.example.video.util.BitmapUtil;
import com.example.video.util.DateUtil;
import com.example.video.util.Utils;
import com.example.video.widget.FloatWindow;

@SuppressLint("WrongConstant")
public class CaptureService extends Service {
    private static final String TAG = "CaptureService";
    private MediaProjectionManager mMpMgr; // 声明一个媒体投影管理器对象
    private MediaProjection mMP; // 声明一个媒体投影对象
    private ImageReader mImageReader; // 声明一个图像读取器对象
    private String mImagePath; // 文件路径
    private int mScreenWidth, mScreenHeight, mScreenDensity; // 屏幕宽度，屏幕高度，每英寸的像素数
    private VirtualDisplay mVirtualDisplay; // 声明一个虚拟显示层对象
    private FloatWindow mFloatWindow; // 声明一个悬浮窗对象
    private MainApplication mApp; // App的应用单例

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = MainApplication.getInstance();
        mMpMgr = mApp.getMpMgr(); // 从全局变量中获取媒体投影管理器
        mScreenWidth = Utils.getScreenWidth(this); // 获得屏幕的宽度
        mScreenHeight = Utils.getScreenHeight(this); // 获得屏幕的高度
        // 获得屏幕每英寸中的像素数
        mScreenDensity = Utils.getScreenDensityDpi(this);
        // 根据屏幕宽高创建一个新的图像读取器
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 2);
        if (mFloatWindow == null) {
            mFloatWindow = new FloatWindow(mApp); // 创建一个新的悬浮窗
            mFloatWindow.setLayout(R.layout.float_capture); // 设置悬浮窗的布局内容
        }
        mFloatWindow.setOnFloatListener(v -> {
            mHandler.post(mStartVirtual); // 准备屏幕
            mHandler.postDelayed(mCapture, 500); // 延迟500毫秒后截屏
            mHandler.postDelayed(mStopVirtual, 1000); // 延迟1000毫秒后释放屏幕
        }); // 设置悬浮窗的点击监听器
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mFloatWindow != null && !mFloatWindow.isShow()) {
            mFloatWindow.show(Gravity.LEFT | Gravity.TOP); // 显示悬浮窗
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            bindForegroundService(); // 绑定前台服务
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // 绑定前台服务。要先创建通知渠道，创建代码见MainApplication.java
    private void bindForegroundService() {
        // 创建一个通知消息的建造器
        Notification.Builder builder = new Notification.Builder(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0开始必须给每个通知分配对应的渠道
            builder = new Notification.Builder(this, getString(R.string.app_name));
        }
        builder.setAutoCancel(false) // 点击通知栏后是否自动清除该通知
                .setSmallIcon(R.mipmap.ic_launcher) // 设置应用名称左边的小图标
                .setContentTitle(getString(R.string.app_name)) // 设置通知栏里面的标题文本
                .setContentText("截屏服务"); // 设置通知栏里面的内容文本
        Notification notify = builder.build(); // 根据通知建造器构建一个通知对象
        startForeground(1, notify); // 把服务推送到前台的通知栏
    }


    private Handler mHandler = new Handler(); // 创建一个处理器对象
    // 定义一个屏幕准备任务
    private Runnable mStartVirtual = () -> {
        // 截图过程中先隐藏悬浮窗
        mFloatWindow.mContentView.setVisibility(View.INVISIBLE);
        if (mMP == null) {
            // 根据结果代码和结果意图，从媒体投影管理器中获取一个媒体投影对象
            mMP = mMpMgr.getMediaProjection(mApp.getResultCode(), mApp.getResultIntent());
        }
        // 根据屏幕宽高创建一个虚拟显示层
        mVirtualDisplay = mMP.createVirtualDisplay("capture_screen", mScreenWidth, mScreenHeight,
                mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    };

    // 定义一个屏幕截取任务
    private Runnable mCapture = () -> {
        // 生成截图文件的保存路径
        mImagePath = String.format("%s/%s.png",
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                DateUtil.getNowDateTime());
        Log.d(TAG, "mImagePath=" + mImagePath);
        // 从图像读取器中获取最近的一个Image对象
        Image image = mImageReader.acquireLatestImage();
        Bitmap bitmap = BitmapUtil.getBitmap(image); // 把Image对象转换成位图对象
        BitmapUtil.saveImage(mImagePath, bitmap); // 把位图对象保存为图片文件
        BitmapUtil.notifyPhotoAlbum(this, mImagePath); // 通知相册来了张新图片
        //Toast.makeText(this, "截图成功：" + mImagePath, Toast.LENGTH_LONG).show();
    };

    // 定义一个屏幕释放任务
    private Runnable mStopVirtual = () -> {
        // 完成截图后再恢复悬浮窗
        mFloatWindow.mContentView.setVisibility(View.VISIBLE);
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release(); // 释放虚拟显示层资源
            mVirtualDisplay = null;
        }
        showThumbnail(); // 显示缩略图窗口
    };

    // 显示缩略图窗口
    private void showThumbnail() {
        FloatWindow thumb = new FloatWindow(mApp); // 创建一个新的悬浮窗
        thumb.setLayout(R.layout.float_thumb); // 设置悬浮窗的布局内容
        thumb.setOnFloatListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setType("vnd.android.cursor.dir/image");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            mHandler.post(() -> thumb.close());
        }); // 设置悬浮窗的点击监听器
        thumb.show(Gravity.RIGHT | Gravity.BOTTOM); // 显示悬浮窗
        ImageView iv_thumb = thumb.mContentView.findViewById(R.id.iv_thumb);
        iv_thumb.setImageURI(Uri.parse(mImagePath)); // 设置图像视图的路径对象
        mHandler.postDelayed(() -> thumb.close(), 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatWindow != null && mFloatWindow.isShow()) {
            mFloatWindow.close(); // 关闭悬浮窗
        }
        if (mMP != null) {
            mMP.stop(); // 停止媒体投影
        }
    }

}
