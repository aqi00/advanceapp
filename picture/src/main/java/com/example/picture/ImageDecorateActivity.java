package com.example.picture;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.picture.util.DateUtil;
import com.example.picture.widget.DecorateImageView;

public class ImageDecorateActivity extends AppCompatActivity {
    private DecorateImageView div_scene; // 声明一个装饰视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_decorate);
        div_scene = findViewById(R.id.div_scene);
        initDecorateSpinner(); // 初始化装饰类型下拉框
    }

    // 初始化装饰类型下拉框
    private void initDecorateSpinner() {
        ArrayAdapter<String> decorateAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, decorateNameArray);
        Spinner sp_decorate = findViewById(R.id.sp_decorate);
        sp_decorate.setPrompt("请选择装饰类型");
        sp_decorate.setAdapter(decorateAdapter);
        sp_decorate.setOnItemSelectedListener(new DecorateSelectedListener());
        sp_decorate.setSelection(0);
    }

    private String[] decorateNameArray = {"无装饰", "时间戳装饰", "标志图装饰", "相框装饰"};
    class DecorateSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (arg2 == 0) { // 无装饰
                div_scene.showNone(); // 不显示任何装饰
            } else if (arg2 == 1) { // 时间戳装饰
                String text = DateUtil.getNowFullDateTime();
                div_scene.showText(text, true); // 显示装饰文本
            } else if (arg2 == 2) { // 标志图装饰
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flower_lotus);
                div_scene.showLogo(bitmap, true); // 显示装饰标志
            } else if (arg2 == 3) { // 相框装饰
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.photo_frame1);
                div_scene.showFrame(bitmap, true); // 显示装饰相框
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

}