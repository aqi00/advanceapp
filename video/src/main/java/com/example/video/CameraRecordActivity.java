package com.example.video;

import android.content.Intent;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.video.util.Utils;
import com.example.video.widget.CameraView;

public class CameraRecordActivity extends AppCompatActivity {
    private final static String TAG = "CameraRecordActivity";
    private CameraView cv_preview; // 声明一个经典相机视图对象
    private Chronometer chr_cost; // 声明一个计时器对象
    private ImageView iv_record; // 声明一个图像视图对象
    private int mCameraType = CameraInfo.CAMERA_FACING_BACK; // 默认使用后置摄像头拍照
    private boolean isRecording = false; // 是否正在录像

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_record);
        initView(); // 初始化视图
        initCamera(); // 初始化相机
    }

    // 初始化视图
    private void initView() {
        cv_preview = findViewById(R.id.cv_preview);
        chr_cost = findViewById(R.id.chr_cost);
        iv_record = findViewById(R.id.iv_record);
        iv_record.setOnClickListener((v) -> dealRecord()); // 处理录像动作
        findViewById(R.id.iv_switch).setOnClickListener((v) -> {
            mCameraType = (mCameraType == CameraInfo.CAMERA_FACING_BACK) ?
                    CameraInfo.CAMERA_FACING_FRONT : CameraInfo.CAMERA_FACING_BACK;
            cv_preview.switchCamera(mCameraType); // 切换摄像头
        });
        findViewById(R.id.btn_play).setOnClickListener((v) -> {
            if (TextUtils.isEmpty(cv_preview.getVideoPath())) {
                Toast.makeText(this, "请先录像再观看视频", Toast.LENGTH_SHORT).show();
                return;
            }
            // 下面跳到视频播放界面
            Intent intent = new Intent(this, VideoDetailActivity.class);
            intent.putExtra("video_path", cv_preview.getVideoPath());
            startActivity(intent);
        });
    }

    // 处理录像动作
    private void dealRecord() {
        if (!isRecording) {
            iv_record.setImageResource(R.drawable.record_stop);
            cv_preview.startRecord(); // 开始录像
            chr_cost.setVisibility(View.VISIBLE);
            chr_cost.setBase(SystemClock.elapsedRealtime()); // 设置计时器的基准时间
            chr_cost.start(); // 开始计时
            isRecording = !isRecording;
        } else {
            iv_record.setEnabled(false);
            cv_preview.stopRecord(); // 停止录像
        }
    }

    // 初始化相机
    private void initCamera() {
        // 初始化经典相机，并指定准备就绪监听器和停止录像监听器
        cv_preview.initCamera(CameraInfo.CAMERA_FACING_BACK, (size) -> {
            int adjustHeight = Utils.getScreenWidth(this) * size.getWidth() / size.getHeight();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cv_preview.getLayoutParams();
            params.height = adjustHeight; // 根据预览尺寸调整预览窗口的高度
            cv_preview.setLayoutParams(params); // 设置预览视图的布局参数
        }, (result) -> {
            runOnUiThread(() -> {
                chr_cost.setVisibility(View.GONE);
                chr_cost.stop(); // 停止计时
                iv_record.setImageResource(R.drawable.record_start);
                iv_record.setEnabled(true);
                isRecording = false;
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cv_preview.closeCamera(); // 关闭相机
    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        cv_preview.openCamera(); // 打开相机
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        cv_preview.closeCamera(); // 关闭相机
//    }
}