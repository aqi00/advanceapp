package com.example.video.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.video.MainApplication;
import com.example.video.R;
import com.example.video.util.DateUtil;
import com.example.video.util.Utils;
import com.example.video.widget.FloatWindow;

import java.nio.ByteBuffer;

public class RecordService extends Service {
    private static final String TAG = "RecordService";
    private MediaProjectionManager mMpMgr; // 声明一个媒体投影管理器对象
    private MediaProjection mMP; // 声明一个媒体投影对象
    private VirtualDisplay mVirtualDisplay; // 声明一个图像读取器对象
    private String mVideoPath; // 文件路径
    private int mScreenWidth, mScreenHeight, mScreenDensity; // 屏幕宽度，屏幕高度，每英寸的像素数
    private MediaCodec mMediaCodec; // 声明一个媒体编码器对象
    private MediaMuxer mMediaMuxer; // 声明一个媒体转换器对象
    private boolean isRecording = false; // 是否正在录制
    private boolean isMuxerStarted = false; // 是否开始转换
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo(); // 创建一个媒体编码器的缓冲区
    private int mVideoTrackIndex = -1; // 视频轨道的索引
    private FloatWindow mFloatWindow; // 声明一个悬浮窗对象
    private ImageView iv_record; // 用于控制录制操作的图像视图
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
        Log.d(TAG, "mScreenWidth="+mScreenWidth+", mScreenHeight="+mScreenHeight);
        if (mScreenWidth >= 1000) { // 视频宽高太大会报错
            mScreenWidth = mScreenWidth/2;
            mScreenHeight = mScreenHeight/2;
        }
        if (mScreenWidth % 2 != 0) { // 视频宽度必须为偶数
            mScreenWidth--;
        }
        if (mScreenHeight % 2 != 0) { // 视频高度必须为偶数
            mScreenHeight--;
        }
        if (mFloatWindow == null) {
            // 创建一个新的悬浮窗
            mFloatWindow = new FloatWindow(MainApplication.getInstance());
            mFloatWindow.setLayout(R.layout.float_record); // 设置悬浮窗的布局内容
        }
        mFloatWindow.setOnFloatListener(v -> {
            isRecording = !isRecording;
            if (isRecording) { // 正在录屏
                // 给悬浮窗的图像视图设置暂停图标
                iv_record.setImageResource(R.drawable.ic_record_pause);
                Toast.makeText(this, "开始录屏", Toast.LENGTH_SHORT).show();
                recordStart(); // 开始录屏
            } else { // 不在录屏
                // 给悬浮窗的图像视图设置开始图标
                iv_record.setImageResource(R.drawable.ic_record_begin);
                Toast.makeText(this, "结束录屏：" + mVideoPath, Toast.LENGTH_LONG).show();
            }
        }); // 设置悬浮窗的点击监听器
        // 从布局文件中获取用于录屏控制的图像视图
        iv_record = mFloatWindow.mContentView.findViewById(R.id.iv_record);
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
                .setContentText("录屏服务"); // 设置通知栏里面的内容文本
        Notification notify = builder.build(); // 根据通知建造器构建一个通知对象
        startForeground(2, notify); // 把服务推送到前台的通知栏
    }

    // 准备工作
    private String prepare() {
        // 根据屏幕宽高，创建一个视频媒体格式对象
        MediaFormat format = MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC, mScreenWidth, mScreenHeight);
        // 设置视频每秒录制的字节数。这里设置每秒录制300KB
        format.setInteger(MediaFormat.KEY_BIT_RATE, 300 * 1024 * 8);
        // 设置视频每秒录制的帧数。这里设置每秒20帧，则每帧大小=300K/20=15K
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
        // 设置视频录制的颜色格式
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        // 设置视频录制期间关键帧的间隔，单位秒
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
        try {
            // 创建一个用于视频编码的媒体编码器
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            // 给媒体编码器配置媒体格式
            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    // 开始录屏
    private void recordStart() {
        String result = prepare(); // 先进行准备工作
        if (result != null) {
            Toast.makeText(this, "准备录屏发生异常：" + result, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mMP == null) {
            // 根据结果代码和结果意图，从媒体投影管理器中获取一个媒体投影对象
            mMP = mMpMgr.getMediaProjection(mApp.getResultCode(), mApp.getResultIntent());
        }
        // 根据屏幕宽高创建一个虚拟显示层
        mVirtualDisplay = mMP.createVirtualDisplay("ScreenRecords", mScreenWidth, mScreenHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaCodec.createInputSurface(), null, null);
        mMediaCodec.start(); // 开始视频编码
        new Thread(mRecord).start(); // 启动录屏线程
    }

    // 定义一个录屏线程
    private Runnable mRecord = () -> {
        try {
            Log.d(TAG, "RecordThread Start");
            // 生成录屏文件的保存路径
            mVideoPath = String.format("%s/%s.mp4",
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                    DateUtil.getNowDateTime());
            // 根据文件路径，创建一个新的媒体转换器
            mMediaMuxer = new MediaMuxer(mVideoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            while (isRecording) { // 正在录屏
                // 获得媒体编码器的缓冲区索引
                int index = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 10000);
                Log.d(TAG, "缓冲区的索引为" + index);
                if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) { // 输出格式发生变化
                    if (isMuxerStarted) {
                        throw new IllegalStateException("输出格式已经发生变化");
                    }
                    // 获得媒体编码器的输出格式
                    MediaFormat newFormat = mMediaCodec.getOutputFormat();
                    // 给媒体转换器添加指定输出格式的轨道，并返回轨道索引
                    mVideoTrackIndex = mMediaMuxer.addTrack(newFormat);
                    mMediaMuxer.start(); // 媒体转换器开始转换工作
                    isMuxerStarted = true;
                    Log.d(TAG, "新的输出格式是：" + newFormat.toString() + "，媒体转换器的轨道索引是" + mVideoTrackIndex);
                } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) { // 请求超时
                    Thread.sleep(50);
                } else if (index >= 0) { // 正常输出
                    if (!isMuxerStarted) {
                        throw new IllegalStateException("媒体转换器尚未添加格式轨道");
                    }
                    encodeToVideo(index); // 进行视频编码
                    // 视频编码完毕，释放媒体编码器的输出缓冲区
                    mMediaCodec.releaseOutputBuffer(index, false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            release(); // 释放资源
        }
    };

    // 将缓冲区的数据编码为视频文件
    private void encodeToVideo(int index) {
        // 根据缓冲区索引，获得媒体编码器的字节缓冲内容
        ByteBuffer encoded = mMediaCodec.getOutputBuffer(index);
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) { // 如果不是媒体数据
            mBufferInfo.size = 0;
        }
        if (mBufferInfo.size == 0) { // 缓冲区不存在有效数据
            encoded = null;
        } else {
            Log.d(TAG, "缓冲区大小=" + mBufferInfo.size
                    + ", 持续时间=" + mBufferInfo.presentationTimeUs
                    + ", 偏移=" + mBufferInfo.offset);
        }
        if (encoded != null) { // 缓冲区存在有效数据
            encoded.position(mBufferInfo.offset); // 定位到字节缓冲区的指定偏移
            // 限制字节缓冲区的操作区域大小
            encoded.limit(mBufferInfo.offset + mBufferInfo.size);
            // 把编码转换后的数据写入索引位置的轨道，也就是写入视频文件
            mMediaMuxer.writeSampleData(mVideoTrackIndex, encoded, mBufferInfo);
        }
    }

    // 释放各路诸侯的资源
    private void release() {
        isRecording = false;
        isMuxerStarted = false;
        if (mMediaCodec != null) {
            mMediaCodec.stop(); // 媒体编码器停止工作
            mMediaCodec.release(); // 释放媒体编码器资源
            mMediaCodec = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release(); // 释放虚拟显示层资源
            mVirtualDisplay = null;
        }
        if (mMediaMuxer != null) {
            mMediaMuxer.stop(); // 媒体转换器停止工作
            mMediaMuxer.release(); // 释放媒体转换器资源
            mMediaMuxer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        if (mFloatWindow != null && mFloatWindow.isShow()) {
            mFloatWindow.close(); // 关闭悬浮窗
        }
        if (mMP != null) {
            mMP.stop(); // 停止媒体投影
        }
    }
}
