package com.example.picture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.picture.util.BitmapUtil;
import com.example.picture.util.DateUtil;
import com.example.picture.widget.BitmapView;

public class BitmapChangeActivity extends AppCompatActivity {
    private BitmapView bv_image; // 声明一个位图视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmap_change);
        CheckBox ck_flip = findViewById(R.id.ck_flip);
        bv_image = findViewById(R.id.bv_image);
        ck_flip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            bv_image.flip(); // 左右翻转图像
        });
        findViewById(R.id.btn_save_image).setOnClickListener(v -> {
            Bitmap bitmap = bv_image.getDrawingCache(); // 从绘图缓存获取位图对象
            // 生成图片文件的保存路径
            String path = String.format("%s/%s.jpg",
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                    DateUtil.getNowDateTime());
            BitmapUtil.saveImage(path, bitmap); // 把位图保存为图片文件
            BitmapUtil.notifyPhotoAlbum(this, path); // 通知相册来了张新图片
            Toast.makeText(this, "成功保存图片文件：" + path, Toast.LENGTH_LONG).show();
        });
        initScaleSpinner(); // 初始化缩放比率下拉框
        initRotateSpinner(); // 初始化旋转角度下拉框
    }

    @Override
    protected void onStart() {
        super.onStart();
        bv_image.setDrawingCacheEnabled(true); // 开启位图视图的绘图缓存
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.butterfly);
        bv_image.setImageBitmap(bitmap); // 设置位图视图的位图对象
    }

    @Override
    protected void onStop() {
        super.onStop();
        bv_image.setDrawingCacheEnabled(false); // 关闭位图视图的绘图缓存
    }

    // 初始化缩放比率下拉框
    private void initScaleSpinner() {
        ArrayAdapter<String> scaleAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, scaleArray);
        Spinner sp_scale = findViewById(R.id.sp_scale);
        sp_scale.setPrompt("请选择缩放比率");
        sp_scale.setAdapter(scaleAdapter);
        sp_scale.setOnItemSelectedListener(new ScaleSelectedListener());
        sp_scale.setSelection(3);
    }

    private String[] scaleArray = {"0.25", "0.5", "0.75", "1.0", "1.5", "2.0", "4.0"};
    class ScaleSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            // 设置缩放比例
            bv_image.setScaleRatio(Float.parseFloat(scaleArray[arg2]), true);
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    // 初始化旋转角度下拉框
    private void initRotateSpinner() {
        ArrayAdapter<String> rotateAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, rotateArray);
        Spinner sp_rotate = findViewById(R.id.sp_rotate);
        sp_rotate.setPrompt("请选择旋转角度");
        sp_rotate.setAdapter(rotateAdapter);
        sp_rotate.setOnItemSelectedListener(new RotateSelectedListener());
        sp_rotate.setSelection(0);
    }

    private String[] rotateArray = {"0", "45", "90", "135", "180", "225", "270", "315"};
    class RotateSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            // 设置旋转角度
            bv_image.setRotateDegree(Integer.parseInt(rotateArray[arg2]), true);
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

}
