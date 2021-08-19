package com.example.picture;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

public class DrawableConvertActivity extends AppCompatActivity {
    private Bitmap mOriginBitmap; // 原始位图
    private ImageView iv_picture; // 声明一个图像视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawable_convert);
        iv_picture = findViewById(R.id.iv_picture);
        mOriginBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mandarin_duck);
        initAlphaSpinner(); // 初始化灰度比例下拉框
    }

    // 初始化灰度比例下拉框
    private void initAlphaSpinner() {
        ArrayAdapter<String> alphaAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, alphaNameArray);
        Spinner sp_alpha = findViewById(R.id.sp_alpha);
        sp_alpha.setPrompt("请选择灰度比例");
        sp_alpha.setAdapter(alphaAdapter);
        sp_alpha.setOnItemSelectedListener(new AlphaSelectedListener());
        sp_alpha.setSelection(0);
    }

    private String[] alphaNameArray = {"1", "0.75", "0.5", "0.25", "0"};
    class AlphaSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            double ratio = Double.parseDouble(alphaNameArray[arg2]);
            // 根据指定位图创建图形对象
            Drawable drawable = new BitmapDrawable(getResources(), mOriginBitmap);
            drawable.setAlpha((int) (255*ratio)); // 设置图形的灰度值
            iv_picture.setImageDrawable(drawable); // 设置图像视图的图形对象
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

}