package com.example.audio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

@SuppressLint("DefaultLocale")
public class SoundPoolActivity extends AppCompatActivity implements OnClickListener {
    private TextView tv_volume; // 声明一个文本视图对象
    private SoundPool mSoundPool; // 初始化一个声音池对象
    private HashMap<Integer, Integer> mSoundMap = new HashMap<>(); // 声音编号映射

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_pool);
        tv_volume = findViewById(R.id.tv_volume);
        findViewById(R.id.btn_play_all).setOnClickListener(this);
        findViewById(R.id.btn_play_first).setOnClickListener(this);
        findViewById(R.id.btn_play_second).setOnClickListener(this);
        findViewById(R.id.btn_play_third).setOnClickListener(this);
        initVolumeInfo(); // 初始化音量信息
        initSound(); // 初始化声音池
    }

    // 初始化音量信息
    private void initVolumeInfo() {
        // 从系统服务中获取音频管理器
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 获取音乐的最大音量
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 获取音乐的当前音量
        int nowVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        String desc = String.format("当前音乐音量为%d，最大音量为%d，请先将音乐音量调至最大",
                nowVolume, maxVolume);
        tv_volume.setText(desc);
    }

    // 初始化声音池
    private void initSound() {
        // 初始化声音池，最多容纳三个声音
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC).build();
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(3).setAudioAttributes(attributes);
        mSoundPool = builder.build();
        loadSound(1, R.raw.beep1); // 加载第一个声音
        loadSound(2, R.raw.beep2); // 加载第二个声音
        loadSound(3, R.raw.ring); // 加载第三个声音
    }

    // 把音频资源添加进声音池
    private void loadSound(int seq, int resid) {
        // 把声音文件加入到声音池中，同时返回该声音文件的编号
        int soundID = mSoundPool.load(this, resid, 1);
        mSoundMap.put(seq, soundID);
    }

    // 播放指定序号的声音
    private void playSound(int seq) {
        int soundID = mSoundMap.get(seq);
        // 播放声音池中指定编号的音频
        mSoundPool.play(soundID, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_play_all) { //同时播放三个声音
            playSound(1); // 播放指定序号的声音
            playSound(2); // 播放指定序号的声音
            playSound(3); // 播放指定序号的声音
        } else if (v.getId() == R.id.btn_play_first) { // 播放第一个声音
            playSound(1); // 播放指定序号的声音
        } else if (v.getId() == R.id.btn_play_second) { // 播放第二个声音
            playSound(2); // 播放指定序号的声音
        } else if (v.getId() == R.id.btn_play_third) { // 播放第三个声音
            playSound(3); // 播放指定序号的声音
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSoundPool != null) {
            mSoundPool.release(); // 释放声音池资源
        }
    }

}
