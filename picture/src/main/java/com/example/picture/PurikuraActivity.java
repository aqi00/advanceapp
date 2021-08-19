package com.example.picture;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.picture.util.BitmapUtil;
import com.example.picture.util.DateUtil;
import com.example.picture.widget.DecorateImageView;

public class PurikuraActivity extends AppCompatActivity {
    private final static String TAG = "PurikuraActivity";
    private int COLLECT_CODE = 14; // 采集头像的请求码
    private DecorateImageView div_photo; // 声明一个装饰视图对象
    private EditText et_text; // 声明一个编辑框对象
    private boolean haveCollected = false; // 是否已采集头像

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purikura);
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("大头贴");
        TextView tv_option = findViewById(R.id.tv_option);
        tv_option.setText("保存");
        tv_option.setOnClickListener(v -> savePurikura());
        div_photo = findViewById(R.id.div_photo);
        et_text = findViewById(R.id.et_text);
        findViewById(R.id.btn_text).setOnClickListener(v -> {
            String text = et_text.getText().toString();
            div_photo.showText(text, false); // 显示装饰文本
        });
        findViewById(R.id.btn_collect).setOnClickListener(v -> {
            // 下面跳到头像采集页面
            Intent intent = new Intent(this, PortraitActivity.class);
            startActivityForResult(intent, COLLECT_CODE);
        });
        initLogoSpinner(); // 初始化标志图片下拉框
        initFrameSpinner(); // 初始化相框种类下拉框
        new Handler(Looper.myLooper()).post(() -> loadTypeFace()); // 加载字体文件
    }

    // 保存加工好的大头贴图片
    private void savePurikura() {
        if (!haveCollected) {
            Toast.makeText(this, "请先采集头像图片", Toast.LENGTH_SHORT).show();
            return;
        }
        div_photo.setDrawingCacheEnabled(true); // 开启装饰视图的绘图缓存
        Bitmap bitmap = div_photo.getDrawingCache(); // 从绘图缓存获取位图对象
        // 生成图片文件的保存路径
        String path = String.format("%s/%s.jpg",
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                DateUtil.getNowDateTime());
        BitmapUtil.saveImage(path, bitmap); // 把位图保存为图片文件
        BitmapUtil.notifyPhotoAlbum(this, path); // 通知相册来了张新图片
        Toast.makeText(this, "已保存大头贴图片 "+path, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && requestCode == COLLECT_CODE) { // 从采集头像返回
            String pic_path = intent.getStringExtra("pic_path");
            div_photo.setImageURI(Uri.parse(pic_path)); // 设置图像视图的路径对象
            haveCollected = true;
        }
    }

    // 初始化标志图片下拉框
    private void initLogoSpinner() {
        ArrayAdapter<String> logoAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, logoNameArray);
        Spinner sp_logo = findViewById(R.id.sp_logo);
        sp_logo.setPrompt("请选择标志图片");
        sp_logo.setAdapter(logoAdapter);
        sp_logo.setOnItemSelectedListener(new LogoSelectedListener());
        sp_logo.setSelection(0);
    }

    private String[] logoNameArray = {"无标志", "春之兰花", "夏之荷花", "秋之菊花", "冬之梅花"};
    class LogoSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Bitmap bitmap = null;
            if (arg2 == 1) { // 春之兰花
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flower_orchid);
            } else if (arg2 == 2) { // 夏之荷花
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flower_lotus);
            } else if (arg2 == 3) { // 秋之菊花
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flower_chrysanthemum);
            } else if (arg2 == 4) { // 冬之梅花
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flower_plum);
            }
            div_photo.showLogo(bitmap, false); // 显示装饰标志
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    // 初始化相框种类下拉框
    private void initFrameSpinner() {
        ArrayAdapter<String> frameAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, frameNameArray);
        Spinner sp_frame = findViewById(R.id.sp_frame);
        sp_frame.setPrompt("请选择相框种类");
        sp_frame.setAdapter(frameAdapter);
        sp_frame.setOnItemSelectedListener(new FrameSelectedListener());
        sp_frame.setSelection(0);
    }

    private String[] frameNameArray = {"无相框", "长方相框", "椭圆相框", "稻草相框", "爱心相框"};
    class FrameSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Bitmap bitmap = null;
            if (arg2 == 1) { // 长方相框
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.photo_frame1);
            } else if (arg2 == 2) { // 椭圆相框
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.photo_frame2);
            } else if (arg2 == 3) { // 稻草相框
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.photo_frame3);
            } else if (arg2 == 4) { // 爱心相框
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.photo_frame4);
            }
            div_photo.showFrame(bitmap, false); // 显示装饰相框
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    // 加载字体文件
    private void loadTypeFace() {
        // 根据assets目录下的字体文件创建字体对象
        Typeface kaiti = Typeface.createFromAsset(getAssets(), "fonts/KaiTi.ttf");
        div_photo.setTypeface(kaiti); // 设置文字字体
    }
}