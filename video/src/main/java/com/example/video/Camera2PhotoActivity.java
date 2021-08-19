package com.example.video;

import android.content.Intent;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.video.util.Utils;
import com.example.video.widget.Camera2View;

public class Camera2PhotoActivity extends AppCompatActivity {
    private final static String TAG = "Camera2PhotoActivity";
    private Camera2View c2v_preview; // 声明一个二代相机视图对象
    private View v_black; // 声明一个视图对象
    private ImageView iv_photo; // 声明一个图像视图对象
    private Handler mHandler = new Handler(Looper.myLooper()); // 声明一个处理器对象
    private int mCameraType = CameraCharacteristics.LENS_FACING_BACK; // 默认使用后置摄像头拍照
    private int mTakeType; // 拍摄类型，分单拍和连拍两种

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_photo);
        initView(); // 初始化视图
        initCamera(); // 初始化相机
    }

    // 初始化视图
    private void initView() {
        c2v_preview = findViewById(R.id.c2v_preview);
        v_black = findViewById(R.id.v_black);
        iv_photo = findViewById(R.id.iv_photo);
        iv_photo.setOnClickListener((v) -> { // 拍照动作
            mTakeType = Camera2View.TYPE_SINGLE;
            iv_photo.setEnabled(false);
            v_black.setVisibility(View.VISIBLE);
            c2v_preview.takePicture(); // 拍摄照片
            mHandler.postDelayed(() -> v_black.setVisibility(View.GONE), 500);
        });
        iv_photo.setOnLongClickListener((v) -> { // 连拍动作
            Toast.makeText(this, "连拍开始", Toast.LENGTH_SHORT).show();
            mTakeType = Camera2View.TYPE_SHOOTING;
            iv_photo.setEnabled(false);
            c2v_preview.startShooting(15000); // 开始连拍
            return true;
        });
        findViewById(R.id.iv_switch).setOnClickListener((v) -> {
            mCameraType = (mCameraType == CameraCharacteristics.LENS_FACING_BACK) ?
                    CameraCharacteristics.LENS_FACING_FRONT : CameraCharacteristics.LENS_FACING_BACK;
            c2v_preview.switchCamera(mCameraType); // 切换摄像头
        });
        findViewById(R.id.btn_album).setOnClickListener((v) -> {
            // 下面跳到系统相册界面
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            startActivity(intent);
        });
        findViewById(R.id.btn_view).setOnClickListener((v) -> {
            if (TextUtils.isEmpty(c2v_preview.getPhotoPath()) && c2v_preview.getShootingList().size()<=0) {
                Toast.makeText(this, "请先拍照再浏览照片", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mTakeType == Camera2View.TYPE_SINGLE) { // 只拍一张照片
                Intent intent = new Intent(this, PhotoDetailActivity.class);
                intent.putExtra("photo_path", c2v_preview.getPhotoPath());
                startActivity(intent);
            } else if (mTakeType == Camera2View.TYPE_SHOOTING) { // 连拍多张照片
                Intent intent = new Intent(this, PhotoGridActivity.class);
                intent.putStringArrayListExtra("path_list", c2v_preview.getShootingList());
                startActivity(intent);
            }
        });
    }

    // 初始化相机
    private void initCamera() {
        // 初始化二代相机，并指定准备就绪监听器和停止拍摄监听器
        c2v_preview.initCamera(mCameraType, (size) -> {
            int adjustHeight = Utils.getScreenWidth(this) * size.getWidth() / size.getHeight();
            Log.d(TAG, "onResume getWidth="+size.getWidth()+", getHeight="+size.getHeight());
            Log.d(TAG, "onResume getScreenWidth="+Utils.getScreenWidth(this)+", adjustHeight="+adjustHeight);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) c2v_preview.getLayoutParams();
            params.height = adjustHeight; // 根据预览尺寸调整预览窗口的高度
            c2v_preview.setLayoutParams(params); // 设置预览视图的布局参数
        }, (result) -> {
            runOnUiThread(() -> {
                iv_photo.setEnabled(true);
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        c2v_preview.closeCamera(); // 关闭相机
    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        c2v_preview.openCamera(); // 打开相机
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        c2v_preview.closeCamera(); // 关闭相机
//    }
}