package com.example.audio.task;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.audio.util.LameUtil;

import java.io.File;

public class Mp3RecordTask extends Thread {
    private final static String TAG = "Mp3RecordTask";
    private Activity mAct; // 声明一个活动实例
    private OnRecordListener mListener; // 声明一个录制事件的监听器对象
    private File mRecordFile; // 音频文件的保存路径
    private int mFrequence = 16000; // 音频的采样频率，单位赫兹
    private int mChannel = AudioFormat.CHANNEL_IN_MONO; // 音频的声道类型
    private int mFormat = AudioFormat.ENCODING_PCM_16BIT; // 音频的编码格式
    private boolean isCancel = false; // 是否取消录音
    private Handler mHandler = new Handler(Looper.myLooper()); // 声明一个处理器对象
    private int mRecordTime = 0; // 已录制时间
    private static final int FRAME_COUNT = 160; // 时间周期，单位毫秒

    public Mp3RecordTask(Activity act, String filePath, OnRecordListener listener) {
        mAct = act;
        mListener = listener;
        mRecordFile = new File(filePath);
        // 最后一个参数表示录音质量，取值为0～9。 其中0最好，但转换慢；9是最差。
        LameUtil.init(mFrequence, 1, mFrequence, 32, 5);
    }

    // 根据样本数重新计算缓冲区大小
    private int calculateBufferSize() {
        // 根据定义好的几个配置，来获取合适的缓冲大小
        int bufferSize = AudioRecord.getMinBufferSize(mFrequence, mChannel, mFormat);
        int bytesPerFrame = 2;
        // 根据样本数重新计算缓冲区大小（能够整除样本数），以便周期性通知
        int frameSize = bufferSize / bytesPerFrame;
        if (frameSize % FRAME_COUNT != 0) {
            frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
            bufferSize = frameSize * bytesPerFrame;
        }
        return bufferSize;
    }

    @Override
    public void run() {
        // 延迟1秒后启动刷新录制进度的任务
        mHandler.postDelayed(mRecordRun, 1000);
        int bufferSize = calculateBufferSize(); // 根据样本数重新计算缓冲区大小
        short[] buffer = new short[bufferSize];
        try {
            // 构建MP3转码线程
            Mp3EncodeTask encodeTask = new Mp3EncodeTask(mRecordFile, bufferSize);
            encodeTask.start(); // 启动MP3转码线程
            // 根据音频配置和缓冲区构建原始音频录制实例
            AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    mFrequence, mChannel, mFormat, bufferSize);
            // 设置需要通知的时间周期
            record.setPositionNotificationPeriod(FRAME_COUNT);
            // 设置录制位置变化的监听器
            record.setRecordPositionUpdateListener(encodeTask, encodeTask.getHandler());
            record.startRecording(); // 开始录制原始音频
            while (!isCancel) { // 没有取消录制，则持续读取缓冲区
                int readSize = record.read(buffer, 0, buffer.length);
                if (readSize > 0) {
                    encodeTask.addTask(buffer, readSize); // 添加MP3转码任务
                }
            }
            record.stop(); // 停止原始音频录制
            encodeTask.sendStopMessage(); // 发送停止消息
            Log.d(TAG, "file_path=" + mRecordFile.getAbsolutePath() + ", length=" + mRecordFile.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mListener != null) {
            mAct.runOnUiThread(() -> mListener.onRecordFinish());
        }
        mHandler.removeCallbacks(mRecordRun); // 移除刷新录制进度的任务
    }

    // 取消录音
    public void cancel() {
        isCancel = true;
    }

    // 定义一个刷新录制进度的任务
    private Runnable mRecordRun = new Runnable() {
        @Override
        public void run() {
            mRecordTime++;
            // 延迟1秒后再次启动刷新录制进度的任务
            mHandler.postDelayed(this, 1000);
            mListener.onRecordUpdate(mRecordTime);
        }
    };

    // 定义一个录制事件的监听器接口
    public interface OnRecordListener {
        void onRecordUpdate(int duration); // 更新录制进度
        void onRecordFinish(); // 录制完毕
    }

}
