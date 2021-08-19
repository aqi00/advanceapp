package com.example.video;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.video.util.BitmapUtil;

public class PhotoDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);
        ImageView iv_photo = findViewById(R.id.iv_photo);
        String photo_path = getIntent().getStringExtra("photo_path");
        // 从指定文件路径解码得到位图对象
        Bitmap bitmap = BitmapFactory.decodeFile(photo_path);
        iv_photo.setImageBitmap(BitmapUtil.getAutoZoomImage(bitmap));
    }
}