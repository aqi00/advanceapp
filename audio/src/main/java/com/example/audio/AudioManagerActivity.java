package com.example.audio;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import androidx.appcompat.app.AppCompatActivity;

public class AudioManagerActivity extends AppCompatActivity implements OnSeekBarChangeListener, OnClickListener {
    private static final String TAG = "VolumeManagerActivity";
    private SeekBar sb_voice, sb_system, sb_ring, sb_music, sb_alarm, sb_notify;
    private ImageView iv_volumn_up, iv_system_up, iv_ring_up, iv_music_up, iv_alarm_up, iv_notify_up;
    private ImageView iv_volumn_down, iv_system_down, iv_ring_down, iv_music_down, iv_alarm_down, iv_notify_down;
    private int[] mStreamType = { // 音频流类型数组
            AudioManager.STREAM_VOICE_CALL, AudioManager.STREAM_SYSTEM,
            AudioManager.STREAM_RING, AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_ALARM, AudioManager.STREAM_NOTIFICATION};
    private int[] mMaxVolume = {0, 0, 0, 0, 0, 0}; // 最大音量数组
    private int[] mNowVolume = {0, 0, 0, 0, 0, 0}; // 当前音量数组
    private SeekBar[] mSeekBar = { // 拖动条的控件数组
            sb_voice, sb_system, sb_ring,
            sb_music, sb_alarm, sb_notify};
    private int[] mStreamRes = { // 拖动条的资源编号数组
            R.id.sb_voice, R.id.sb_system, R.id.sb_ring,
            R.id.sb_music, R.id.sb_alarm, R.id.sb_notify};
    private ImageView[] mAddView = { // 增大音量按钮的控件数组
            iv_volumn_up, iv_system_up, iv_ring_up,
            iv_music_up, iv_alarm_up, iv_notify_up};
    private int[] mAddRes = { // 增大音量按钮的资源编号数组
            R.id.iv_volumn_up, R.id.iv_system_up, R.id.iv_ring_up,
            R.id.iv_music_up, R.id.iv_alarm_up, R.id.iv_notify_up};
    private ImageView[] mDelView = { // 减小音量按钮的控件数组
            iv_volumn_down, iv_system_down, iv_ring_down,
            iv_music_down, iv_alarm_down, iv_notify_down};
    private int[] mDelRes = { // 减小音量按钮的资源编号数组
            R.id.iv_volumn_down, R.id.iv_system_down, R.id.iv_ring_down,
            R.id.iv_music_down, R.id.iv_alarm_down, R.id.iv_notify_down};
    private int SEEK_BAR = 1, ADD_VIEW = 2, DEL_VIEW = 3;
    private AudioManager mAudioMgr; // 声明一个音频管理器对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_manager);
        // 从布局文件中依次获取各音量类型的拖动条、增大音量按钮、减小音量按钮
        for (int i = 0; i < mStreamType.length; i++) {
            mSeekBar[i] = findViewById(mStreamRes[i]);
            mAddView[i] = findViewById(mAddRes[i]);
            mDelView[i] = findViewById(mDelRes[i]);
        }
        setStreamVolume(); // 设置各音量类型的拖动条进度
        for (int i = 0; i < mStreamType.length; i++) {
            // 给各音量类型的拖动条设置拖动变更监听器
            mSeekBar[i].setOnSeekBarChangeListener(this);
            // 给各音量类型的增大音量按钮设置点击监听器
            mAddView[i].setOnClickListener(this);
            // 给各音量类型的减小音量按钮设置点击监听器
            mDelView[i].setOnClickListener(this);
        }
    }

    // 设置各音量类型的拖动条进度
    void setStreamVolume() {
        // 从系统服务中获取音频管理器
        mAudioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        for (int i = 0; i < mStreamType.length; i++) {
            int type = mStreamType[i];
            // 获取指定音频类型的最大音量
            mMaxVolume[i] = mAudioMgr.getStreamMaxVolume(type);
            // 获取指定音频类型的当前音量
            mNowVolume[i] = mAudioMgr.getStreamVolume(type);
            // 设置拖动条的音量大小进度
            mSeekBar[i].setProgress(mSeekBar[i].getMax() * mNowVolume[i] / mMaxVolume[i]);
        }
    }

    // 在进度变更时触发。第三个参数为true表示用户拖动，为false表示代码设置进度
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

    // 在开始拖动进度时触发
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    // 在停止拖动进度时触发
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "当前进度为：" + seekBar.getProgress() + ", 最大进度为" + seekBar.getMax());
        int index = getArrayIndex(seekBar.getId(), SEEK_BAR);
        int type = mStreamType[index];
        int volume = mMaxVolume[index] * seekBar.getProgress() / seekBar.getMax();
        Log.d(TAG, "volume=" + volume + ", last volume=" + mNowVolume[index] + ", max volume=" + mMaxVolume[index]);
        if (volume != mNowVolume[index]) {
            mNowVolume[index] = volume;
            // 根据拖动位置，计算并设置拖动条的当前进度
            seekBar.setProgress(seekBar.getMax() * mNowVolume[index] / mMaxVolume[index]);
        }
        // 设置该音频类型的当前音量
        mAudioMgr.setStreamVolume(type, volume, AudioManager.FLAG_PLAY_SOUND);
    }

    @Override
    public void onClick(View v) {
        int add_index = getArrayIndex(v.getId(), ADD_VIEW);
        int del_index = getArrayIndex(v.getId(), DEL_VIEW);
        if (add_index != -1) { // 点击了增大音量按钮
            SeekBar seekBar = mSeekBar[add_index];
            if (mNowVolume[add_index] < mMaxVolume[add_index]) {
                mNowVolume[add_index] = mNowVolume[add_index] + 1;
                // 设置拖动条的音量大小进度
                seekBar.setProgress(seekBar.getMax() * mNowVolume[add_index] / mMaxVolume[add_index]);
                // 把该音频类型的当前音量调大一级
                mAudioMgr.adjustStreamVolume(mStreamType[add_index], AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
            }
        } else if (del_index != -1) { // 点击了减小音量按钮
            SeekBar seekBar = mSeekBar[del_index];
            if (mNowVolume[del_index] > 0) {
                mNowVolume[del_index] = mNowVolume[del_index] - 1;
                // 设置拖动条的音量大小进度
                seekBar.setProgress(seekBar.getMax() * mNowVolume[del_index] / mMaxVolume[del_index]);
                // 把该音频类型的当前音量调小一级
                mAudioMgr.adjustStreamVolume(mStreamType[del_index], AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
            }
        }
    }

    // 根据资源编号与类型寻找它在对应数组中的位置
    private int getArrayIndex(int resid, int type) {
        int index = -1;
        if (type == SEEK_BAR) { // 这是拖动条
            for (int i = 0; i < mSeekBar.length; i++) {
                if (mSeekBar[i].getId() == resid) {
                    index = i;
                    break;
                }
            }
        } else if (type == ADD_VIEW) { // 这是增大音量按钮
            for (int i = 0; i < mAddView.length; i++) {
                if (mAddView[i].getId() == resid) {
                    index = i;
                    break;
                }
            }
        } else if (type == DEL_VIEW) { // 这是减小音量按钮
            for (int i = 0; i < mDelView.length; i++) {
                if (mDelView[i].getId() == resid) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

}
