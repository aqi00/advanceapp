package com.example.picture;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.picture.widget.RippleImageView;

public class ImageRippleActivity extends AppCompatActivity {
    private RippleImageView riv_scene; // 声明一个水波图像对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_ripple);
        riv_scene = findViewById(R.id.riv_scene);
        riv_scene.setOnClickListener(v -> riv_scene.startRipple());
        initRippleSpinner(); // 初始化水波特效下拉框
    }

    // 初始化水波特效下拉框
    private void initRippleSpinner() {
        ArrayAdapter<String> rippleAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, rippleNameArray);
        Spinner sp_ripple = findViewById(R.id.sp_ripple);
        sp_ripple.setPrompt("请选择水波特效");
        sp_ripple.setAdapter(rippleAdapter);
        sp_ripple.setOnItemSelectedListener(new RippleSelectedListener());
        sp_ripple.setSelection(0);
    }

    private String[] rippleNameArray = {"白色", "红色", "绿色", "蓝色", "黄色", "青色", "紫色"};
    private int[] rippleColoArray = {0x99ffffff, 0x99ff0000, 0x9900ff00, 0x990000ff, 0x99ffff00, 0x9900ffff, 0x99ff00ff};
    class RippleSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            riv_scene.setRippleColor(rippleColoArray[arg2]); // 设置水波的颜色
            riv_scene.startRipple(); // 开始播放水波动画
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

}