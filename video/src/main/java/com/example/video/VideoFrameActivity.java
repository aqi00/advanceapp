package com.example.video;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.example.video.util.MediaUtil;
import com.example.video.widget.VideoController;

import java.util.ArrayList;
import java.util.List;

public class VideoFrameActivity extends AppCompatActivity {
    private final static String TAG = "VideoFrameActivity";
    private VideoView vv_content; // 声明一个视频视图对象
    private VideoController vc_play; // 声明一个视频控制条对象
    private LinearLayout ll_cut; // 声明一个线性视图对象
    private ImageView iv_frame; // 声明一个图像视图对象
    private Uri mUri; // 声明一个Uri地址对象
    private int CHOOSE_CODE = 3; // 只在相册挑选视频的请求码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_frame);
        vv_content = findViewById(R.id.vv_content);
        vc_play = findViewById(R.id.vc_play);
        ll_cut = findViewById(R.id.ll_cut);
        iv_frame = findViewById(R.id.iv_frame);
        findViewById(R.id.btn_open).setOnClickListener(v -> {
            // ACTION_GET_CONTENT只可选择近期的视频
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            // ACTION_PICK可选择所有视频
            //Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("video/*"); // 类型为视频
            startActivityForResult(intent, CHOOSE_CODE); // 打开系统视频库
        });
        findViewById(R.id.btn_cut_one).setOnClickListener(v -> {
            Bitmap bitmap = MediaUtil.getOneFrame(this, mUri, vv_content.getCurrentPosition());
            iv_frame.setImageBitmap(bitmap);
        });
        findViewById(R.id.btn_cut_multi).setOnClickListener(v -> {
            List<String> frameList = MediaUtil.getFrameList(this, mUri,
                    vv_content.getCurrentPosition(), 9);
            Intent intent = new Intent(this, PhotoGridActivity.class);
            intent.putStringArrayListExtra("path_list", (ArrayList<String>) frameList);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && requestCode == CHOOSE_CODE) { // 从视频库回来
            if (intent.getData() != null) {
                mUri = intent.getData();
                ll_cut.setVisibility(View.VISIBLE);
                vc_play.prepare(vv_content, mUri); // 准备播放指定路径的视频
                vc_play.start(); // 开始播放

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        vc_play.resume(); // 恢复播放
    }

    @Override
    protected void onPause() {
        super.onPause();
        vc_play.pause(); // 暂停播放
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vc_play.release(); // 释放播放资源
    }

}