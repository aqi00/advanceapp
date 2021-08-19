package com.example.face;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.face.util.BitmapUtil;
import com.example.face.widget.FaceView;

public class DetectSystemActivity extends AppCompatActivity {
    private final static String TAG = "DetectSystemActivity";
    private int CHOOSE_CODE = 3; // 只在相册挑选图片的请求码
    private FaceView fv_face; // 声明一个人脸视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_system);
        fv_face = findViewById(R.id.fv_face);
        findViewById(R.id.btn_choose).setOnClickListener(v -> {
            // 创建一个内容获取动作的意图（准备跳到系统相册）
            Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
            albumIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); // 是否允许多选
            albumIntent.setType("image/*"); // 类型为图像
            startActivityForResult(albumIntent, CHOOSE_CODE); // 打开系统相册
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && requestCode == CHOOSE_CODE) { // 从相册返回
            if (intent.getData() != null) { // 从相册选择一张照片
                Uri uri = intent.getData(); // 获得已选择照片的路径对象
                // Android自带的人脸检测只支持RGB_565格式
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565; // 构造位图生成的参数，必须为RGB_565
                // 根据指定图片的uri，获得自动缩小后的位图对象
                Bitmap bitmap = BitmapUtil.getAutoZoomImage(this, uri, options);
                fv_face.setImageBitmap(bitmap); // 设置人脸视图的位图对象
            }
        }
    }

}