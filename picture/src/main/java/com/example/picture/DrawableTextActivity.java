package com.example.picture;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.picture.widget.MarkTextDrawable;

public class DrawableTextActivity extends AppCompatActivity {
    private final static String TAG = "DrawableTextActivity";
    private Bitmap mOriginBitmap; // 原始位图
    private ImageView iv_picture; // 声明一个图像视图对象
    private Typeface[] mTypeFaceArray; // 字体数组

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawable_text);
        iv_picture = findViewById(R.id.iv_picture);
        mOriginBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mandarin_duck);
        iv_picture.setImageBitmap(mOriginBitmap); // 设置图像视图的位图对象
        new Handler(Looper.myLooper()).post(() -> loadTypeFace()); // 加载字体文件
    }

    // 加载字体文件
    private void loadTypeFace() {
        mTypeFaceArray = new Typeface[fontFileArray.length];
        for (int i=0; i<fontFileArray.length; i++) {
            String fontFile = "fonts/" + fontFileArray[i] + ".ttf";
            // 根据assets目录下的字体文件创建字体对象
            mTypeFaceArray[i] = Typeface.createFromAsset(getAssets(), fontFile);
        }
        initFontSpinner(); // 初始化中文字体下拉框
    }

    // 初始化中文字体下拉框
    private void initFontSpinner() {
        ArrayAdapter<String> fontAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, fontNameArray);
        Spinner sp_font = findViewById(R.id.sp_font);
        sp_font.setPrompt("请选择中文字体");
        sp_font.setAdapter(fontAdapter);
        sp_font.setOnItemSelectedListener(new FontSelectedListener());
        sp_font.setSelection(0);
    }

    private String[] fontNameArray = {"常规", "仿宋", "楷体", "隶书", "黑体", "幼圆",
            "华文行楷", "华文新魏", "华文彩云", "华文琥珀", "方正舒体", "方正姚体"};
    private String[] fontFileArray = {"Regular", "FangSong", "KaiTi", "LiShu", "HeiTi", "YouYuan",
            "HangKai", "XinWei", "CaiYun", "HuPo", "ShuTi", "YaoTi"};
    class FontSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            // 根据指定位图创建水印图形对象
            MarkTextDrawable drawable = new MarkTextDrawable(DrawableTextActivity.this, mOriginBitmap);
            drawable.setMarkerText("鸳鸯戏水", mTypeFaceArray[arg2]); // 设置水印文字及其字体
            iv_picture.setImageDrawable(drawable); // 设置图像视图的图形对象
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

}