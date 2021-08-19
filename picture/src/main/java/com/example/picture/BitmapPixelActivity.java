package com.example.picture;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.picture.util.BitmapUtil;

public class BitmapPixelActivity extends AppCompatActivity {
    private ImageView iv_picture; // 声明一个图像视图对象
    private Bitmap mOriginBitmap; // 原始位图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmap_pixel);
        iv_picture = findViewById(R.id.iv_picture);
        mOriginBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.butterfly);
        initColorSpinner(); // 初始化色彩模式下拉框
    }

    // 初始化色彩模式下拉框
    private void initColorSpinner() {
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, colorNameArray);
        Spinner sp_color = findViewById(R.id.sp_color);
        sp_color.setPrompt("请选择色彩模式");
        sp_color.setAdapter(colorAdapter);
        sp_color.setOnItemSelectedListener(new ColorSelectedListener());
        sp_color.setSelection(0);
    }

    private String[] colorNameArray = {"原色", "黑白", "底片", "怀旧", "模糊"};
    class ColorSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (arg2 == 0) { // 原色
                iv_picture.setImageBitmap(mOriginBitmap); // 设置图像视图的位图对象
            } else if (arg2 == 1) { // 黑白
                Bitmap bitmap = BitmapUtil.convertBlack(mOriginBitmap); // 转换为黑白效果
                iv_picture.setImageBitmap(bitmap); // 设置图像视图的位图对象
            } else if (arg2 == 2) { // 底片
                Bitmap bitmap = BitmapUtil.convertNegative(mOriginBitmap); // 转换为底片效果
                iv_picture.setImageBitmap(bitmap); // 设置图像视图的位图对象
            } else if (arg2 == 3) { // 怀旧
                Bitmap bitmap = BitmapUtil.convertOld(mOriginBitmap); // 转换为怀旧效果
                iv_picture.setImageBitmap(bitmap); // 设置图像视图的位图对象
            } else if (arg2 == 4) { // 模糊
                Bitmap bitmap = BitmapUtil.convertBlur(mOriginBitmap); // 转换为模糊效果
                iv_picture.setImageBitmap(bitmap); // 设置图像视图的位图对象
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

}