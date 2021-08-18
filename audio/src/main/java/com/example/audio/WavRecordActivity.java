package com.example.audio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.audio.task.WavRecordTask;
import com.example.audio.util.DateUtil;
import com.example.audio.widget.AudioController;

public class WavRecordActivity extends AppCompatActivity implements WavRecordTask.OnRecordListener {
    private static final String TAG = "WavRecordActivity";
    private TextView tv_audio_record; // 声明一个文本视图对象
    private CheckBox ck_audio_record; // 声明一个复选框对象
    private AudioController ac_play; // 声明一个音频控制条对象
    private String mRecordFilePath; // 录制文件的保存路径
    private WavRecordTask mRecordTask; // 声明一个WAV录制线程对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_record);
        tv_audio_record = findViewById(R.id.tv_audio_record);
        ck_audio_record = findViewById(R.id.ck_audio_record);
        ac_play = findViewById(R.id.ac_play);
        ck_audio_record.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) { // 开始录音
                // 生成WAV的文件路径
                mRecordFilePath = String.format("%s/%s.wav",
                        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                        DateUtil.getNowDateTime());
                ck_audio_record.setText("停止录音");
                // 创建一个WAV录制线程，并设置录制事件监听器
                mRecordTask = new WavRecordTask(this, mRecordFilePath, this);
                mRecordTask.start(); // 启动WAV录制线程
            } else { // 停止录音
                ck_audio_record.setText("开始录音");
                mRecordTask.cancel(); // WAV录制线程取消录音
            }
        });
    }

    // 在录音进度更新时触发
    @Override
    public void onRecordUpdate(int duration) {
        String desc = String.format("WAV已录制%d秒", duration);
        tv_audio_record.setText(desc);
    }

    // 在录音完成时触发
    @Override
    public void onRecordFinish() {
        ck_audio_record.setChecked(false);
        Toast.makeText(this, "已结束录音，音频文件路径为"+mRecordFilePath, Toast.LENGTH_LONG).show();
        ac_play.prepare(mRecordFilePath); // 准备播放指定路径的音频
    }

    @Override
    protected void onResume() {
        super.onResume();
        ac_play.resume(); // 恢复播放
    }

    @Override
    protected void onPause() {
        super.onPause();
        ac_play.pause(); // 暂停播放
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ac_play.release(); // 释放播放资源
    }

}