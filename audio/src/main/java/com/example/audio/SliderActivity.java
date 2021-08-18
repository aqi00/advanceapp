package com.example.audio;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

public class SliderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);
        SeekBar sb_progress = findViewById(R.id.sb_progress);
        sb_progress.setOnSeekBarChangeListener(mSeekListener); // 设置拖动条的拖动监听器
        Slider sl_progress = findViewById(R.id.sl_progress);
        sl_progress.addOnSliderTouchListener(mSliderListener); // 设置滑动条的触摸监听器
    }

    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        // 在进度变更时触发。第三个参数为true表示用户拖动，为false表示代码设置进度
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

        // 在开始拖动进度时触发
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        // 在停止拖动进度时触发
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Toast.makeText(SliderActivity.this, "您选择的进度是"+seekBar.getProgress(),
                    Toast.LENGTH_SHORT).show();
        }
    };

    private Slider.OnSliderTouchListener mSliderListener = new Slider.OnSliderTouchListener() {
        // 在开始滑动进度时触发
        @Override
        public void onStartTrackingTouch(Slider slider) {}

        // 在停止滑动进度时触发
        @Override
        public void onStopTrackingTouch(Slider slider) {
            Toast.makeText(SliderActivity.this, "您选择的进度是"+slider.getValue(),
                    Toast.LENGTH_SHORT).show();
        }
    };

}