package com.example.audio.task;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class WavRecordTask extends Thread {
    private final static String TAG = "WavRecordTask";
    private Activity mAct; // 声明一个活动实例
    private WavRecordTask.OnRecordListener mListener; // 声明一个录制事件的监听器对象
    private File mRecordFile; // 音频文件的保存路径
    private int mFrequence = 16000; // 音频的采样频率，单位赫兹
    private int mChannel = AudioFormat.CHANNEL_IN_MONO; // 音频的声道类型
    private int mFormat = AudioFormat.ENCODING_PCM_16BIT; // 音频的编码格式
    private boolean isCancel = false; // 是否取消录音
    private Handler mHandler = new Handler(Looper.myLooper()); // 声明一个处理器对象
    private int mRecordTime = 0; // 已录制时间

    public WavRecordTask(Activity act, String filePath, WavRecordTask.OnRecordListener listener) {
        mAct = act;
        mListener = listener;
        mRecordFile = new File(filePath);
    }

    @Override
    public void run() {
        // 延迟1秒后启动刷新录制进度的任务
        mHandler.postDelayed(mRecordRun, 1000);
        // 开通输出流到指定的文件
        try (FileOutputStream fos = new FileOutputStream(mRecordFile);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 根据定义好的几个配置，来获取合适的缓冲大小
            int bufferSize = AudioRecord.getMinBufferSize(mFrequence, mChannel, mFormat);
            byte[] buffer = new byte[bufferSize]; // 创建缓冲区
            // 根据音频配置和缓冲区构建原始音频录制实例
            AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    mFrequence, mChannel, mFormat, bufferSize);
            // 设置需要通知的时间周期为1秒
            record.setPositionNotificationPeriod(1000);
            // 设置录制位置变化的监听器
            record.setRecordPositionUpdateListener(new WavRecordTask.RecordUpdateListener());
            record.startRecording(); // 开始录制原始音频
            // 没有取消录制，则持续读取缓冲区
            while (!isCancel) {
                int readSize = record.read(buffer, 0, buffer.length);
                baos.write(buffer, 0, readSize);
            }
            record.stop(); // 停止原始音频录制
            buffer = baos.toByteArray();
            Log.d(TAG, "audio byte len="+buffer.length);
            fos.write(getWavHeader(buffer.length)); // 先往音频文件写入wav文件头
            fos.write(buffer); // 再往音频文件写入音频数据
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
        }
    };

    // 获取wav文件的头信息
    private byte[] getWavHeader(long totalAudioLen){
        int channels = 1; // 声道数。单声道为1，立体声或双声道为2
        int sampleBits = 16; // 采样位数
        long totalDataLen = totalAudioLen + 36; // 文件总长减去8
        long sampleRate = mFrequence; // 采样频率
        // 采样帧大小。其值为声道数*采样位数/8
        int frameSize = channels * sampleBits / 2;
        // 数据传输速率，其值为采样频率*采样帧大小
        long byteRate = mFrequence * frameSize;
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF块开始
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W'; // WAVE格式包含fmt子块和数据子块
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // fmt子块开始
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // fmt子块大小，从第20位到第36位
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // 音频格式，一般为1
        header[21] = 0;
        header[22] = (byte) channels; // 声道数量
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * sampleBits / 2); // 采样帧大小
        header[33] = 0;
        header[34] = (byte) sampleBits; // 采样位数，每个样本的位数
        header[35] = 0;
        header[36] = 'd'; // data块开始（数据块）
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        return header;
    }

    // 定义一个录制位置变化的监听器
    private class RecordUpdateListener implements AudioRecord.OnRecordPositionUpdateListener {

        // 在标记到达时触发，对应setNotificationMarkerPosition方法的设置
        @Override
        public void onMarkerReached(AudioRecord recorder) {}

        // 在周期到达时触发，对应setPositionNotificationPeriod方法的设置
        @Override
        public void onPeriodicNotification(AudioRecord recorder) {
            if (mListener != null) {
                mAct.runOnUiThread(() -> mListener.onRecordUpdate(mRecordTime));
            }
        }
    }

    // 定义一个录制事件的监听器接口
    public interface OnRecordListener {
        void onRecordUpdate(int duration); // 更新录制进度
        void onRecordFinish(); // 录制完毕
    }

}
