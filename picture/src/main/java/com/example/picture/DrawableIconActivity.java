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

import com.example.picture.widget.MarkIconDrawable;

public class DrawableIconActivity extends AppCompatActivity {
    private final static String TAG = "DrawableIconActivity";
    private Bitmap mOriginBitmap; // 原始位图
    private Bitmap mMarkerBitmap; // 水印位图
    private ImageView iv_picture; // 声明一个图像视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawable_icon);
        iv_picture = findViewById(R.id.iv_picture);
        mOriginBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mandarin_duck);
        mMarkerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flower_lotus);
        iv_picture.setImageBitmap(mOriginBitmap); // 设置图像视图的位图对象
        initDirectionSpinner(); // 初始化图标方位下拉框
    }

    // 初始化图标方位下拉框
    private void initDirectionSpinner() {
        ArrayAdapter<String> directionAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, directionArray);
        Spinner sp_direction = findViewById(R.id.sp_direction);
        sp_direction.setPrompt("请选择图标方位");
        sp_direction.setAdapter(directionAdapter);
        sp_direction.setOnItemSelectedListener(new DirectionSelectedListener());
        sp_direction.setSelection(0);
    }

    private String[] directionArray = {"中间水印", "左上水印", "右上水印", "左下水印", "右下水印"};
    class DirectionSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            // 根据指定位图创建水印图形对象
            MarkIconDrawable drawable = new MarkIconDrawable(DrawableIconActivity.this, mOriginBitmap);
            drawable.setMarkerIcon(mMarkerBitmap, arg2); // 设置水印图标及其方位
            iv_picture.setImageDrawable(drawable); // 设置图像视图的图形对象
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

}