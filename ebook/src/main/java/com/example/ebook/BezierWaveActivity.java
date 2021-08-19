package com.example.ebook;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ebook.widget.WaveView;

public class BezierWaveActivity extends AppCompatActivity {
    private Button btn_start;
    private WaveView wv_wave; // 声明一个波浪视图对象
    private boolean isPlaying = false; // 是否正在播放动画

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bezier_wave);
        wv_wave = findViewById(R.id.wv_wave);
        btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(v -> {
            if (!isPlaying) {
                wv_wave.startAnim(); // 开始播放波浪动画
            } else {
                wv_wave.stopAnim(); // 停止播放波浪动画
            }
            isPlaying = !isPlaying;
            btn_start.setText(isPlaying?"停止播放动画":"开始播放动画");
        });
    }

}