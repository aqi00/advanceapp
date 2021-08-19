package com.example.picture;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.picture.widget.CircleDrawable;
import com.example.picture.widget.OvalDrawable;
import com.example.picture.widget.RoundDrawable;

public class DrawableCutActivity extends AppCompatActivity {
    private Bitmap mOriginBitmap; // 原始位图
    private ImageView iv_picture; // 声明一个图像视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawable_cut);
        iv_picture = findViewById(R.id.iv_picture);
        mOriginBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mandarin_duck);
        initMethodSpinner(); // 初始化剪裁方式下拉框
    }

    // 初始化剪裁方式下拉框
    private void initMethodSpinner() {
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, methodArray);
        Spinner sp_method = findViewById(R.id.sp_method);
        sp_method.setPrompt("请选择剪裁方式");
        sp_method.setAdapter(methodAdapter);
        sp_method.setOnItemSelectedListener(new MethodSelectedListener());
        sp_method.setSelection(0);
    }

    private String[] methodArray = {"不裁剪", "圆形剪裁", "椭圆剪裁", "圆角矩形剪裁"};
    class MethodSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (arg2 == 0) { // 不裁剪
                iv_picture.setImageBitmap(mOriginBitmap); // 设置图像视图的位图对象
            } else if (arg2 == 1) { // 圆形剪裁
                Drawable drawable = new CircleDrawable(DrawableCutActivity.this, mOriginBitmap);
                iv_picture.setImageDrawable(drawable); // 设置图像视图的图形对象
            } else if (arg2 == 2) { // 椭圆剪裁
                Drawable drawable = new OvalDrawable(DrawableCutActivity.this, mOriginBitmap);
                iv_picture.setImageDrawable(drawable); // 设置图像视图的图形对象
            } else if (arg2 == 3) { // 圆角矩形剪裁
                Drawable drawable = new RoundDrawable(DrawableCutActivity.this, mOriginBitmap);
                iv_picture.setImageDrawable(drawable); // 设置图像视图的图形对象
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

}