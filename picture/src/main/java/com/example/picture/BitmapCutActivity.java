package com.example.picture;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.picture.util.BitmapUtil;
import com.example.picture.util.DateUtil;
import com.example.picture.widget.CropImageView;

public class BitmapCutActivity extends AppCompatActivity {
    private CropImageView civ_over; // 声明一个裁剪视图对象
    private ImageView iv_old; // 声明一个原始图片的图像视图对象
    private ImageView iv_new; // 声明一个最新图片的图像视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmap_cut);
        civ_over = findViewById(R.id.civ_over);
        iv_old = findViewById(R.id.iv_old);
        iv_new = findViewById(R.id.iv_new);
        findViewById(R.id.btn_save_image).setOnClickListener(v -> {
            civ_over.setVisibility(View.GONE);
            Bitmap bitmap = civ_over.getCropBitmap(); // 获取裁剪视图处理后的位图
            iv_new.setImageBitmap(bitmap); // 设置图像视图的位图对象
            // 生成图片文件的保存路径
            String path = String.format("%s/%s.jpg",
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                    DateUtil.getNowDateTime());
            BitmapUtil.saveImage(path, bitmap); // 把位图保存为图片文件
            BitmapUtil.notifyPhotoAlbum(this, path); // 通知相册来了张新图片
            Toast.makeText(this, "成功保存图片文件：" + path, Toast.LENGTH_SHORT).show();
            initZoneSpinner(); // 初始化裁剪区域下拉框
        });
        iv_old.setDrawingCacheEnabled(true); // 开启位图视图的绘图缓存
        iv_old.setImageResource(R.drawable.butterfly); // 设置图像视图的资源编号
        new Handler(Looper.myLooper()).postDelayed(() -> initZoneSpinner(), 200);
    }

    // 初始化裁剪区域下拉框
    private void initZoneSpinner() {
        ArrayAdapter<String> zoneAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, zoneNameArray);
        Spinner sp_zone = findViewById(R.id.sp_zone);
        sp_zone.setPrompt("请选择裁剪区域");
        sp_zone.setAdapter(zoneAdapter);
        sp_zone.setOnItemSelectedListener(new ZoneSelectedListener());
        sp_zone.setSelection(0);
    }

    private String[] zoneNameArray = {"不裁剪", "中间", "左上角", "右上角", "左下角", "右下角"};
    class ZoneSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            civ_over.setVisibility(arg2==0?View.GONE:View.VISIBLE);
            Bitmap bitmap = iv_old.getDrawingCache(); // 从绘图缓存获取位图对象
            int width = bitmap.getWidth(), height = bitmap.getHeight();
            civ_over.setOrigBitmap(bitmap); // 设置裁剪视图的原始位图
            // 以下依据裁剪区域分别设置裁剪视图的位图边界
            if (arg2 == 1) { // 中间
                civ_over.setBitmapRect(new Rect(width/4, height/4, width/2, height/2));
            } else if (arg2 == 2) { // 左上角
                civ_over.setBitmapRect(new Rect(0, 0, width/2, height/2));
            } else if (arg2 == 3) { // 右上角
                civ_over.setBitmapRect(new Rect(width/2, 0, width/2, height/2));
            } else if (arg2 == 4) { // 左下角
                civ_over.setBitmapRect(new Rect(0, height/2, width/2, height/2));
            } else if (arg2 == 5) { // 右下角
                civ_over.setBitmapRect(new Rect(width/2, height/2, width/2, height/2));
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

}
