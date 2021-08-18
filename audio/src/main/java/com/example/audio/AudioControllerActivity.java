package com.example.audio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.audio.bean.AudioInfo;
import com.example.audio.util.MediaUtil;
import com.example.audio.widget.AudioController;

public class AudioControllerActivity extends AppCompatActivity {
    private final static String TAG = "AudioControllerActivity";
    private LinearLayout ll_controller; // 声明一个线性视图对象
    private TextView tv_title; // 声明一个文本视图对象
    private AudioController ac_play; // 声明一个音频控制条对象
    private int CHOOSE_CODE = 3; // 只在音乐库挑选音频的请求码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_controller);
        ll_controller = findViewById(R.id.ll_controller);
        tv_title = findViewById(R.id.tv_title);
        ac_play = findViewById(R.id.ac_play);
        findViewById(R.id.btn_open).setOnClickListener(v -> {
            // ACTION_GET_CONTENT只可选择近期的音频
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            // ACTION_PICK可选择所有音频
            //Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("audio/*"); // 类型为音频
            startActivityForResult(intent, CHOOSE_CODE); // 打开系统音频库
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && requestCode == CHOOSE_CODE) { // 从音频库回来
            if (intent.getData() != null) {
                ll_controller.setVisibility(View.VISIBLE);
                // 从content://media/external/audio/media/这样的Uri中获取音频信息
                AudioInfo audio = MediaUtil.getPathFromContentUri(this, intent.getData());
                ac_play.prepare(audio.getAudio()); // 准备播放指定路径的音频
                ac_play.start(); // 开始播放
                String desc = String.format("%s的《%s》", audio.getArtist(), audio.getTitle());
                tv_title.setText("当前播放曲目名称："+desc);
            }
        }
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