package com.example.video;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.video.util.PermissionUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_camera_photo).setOnClickListener(this);
        findViewById(R.id.btn_camera_record).setOnClickListener(this);
        findViewById(R.id.btn_video_controller).setOnClickListener(this);
        findViewById(R.id.btn_camera2_photo).setOnClickListener(this);
        findViewById(R.id.btn_camera2_record).setOnClickListener(this);
        findViewById(R.id.btn_exo_player).setOnClickListener(this);
        findViewById(R.id.btn_video_frame).setOnClickListener(this);
        findViewById(R.id.btn_float_window).setOnClickListener(this);
        findViewById(R.id.btn_screen_capture).setOnClickListener(this);
        findViewById(R.id.btn_screen_record).setOnClickListener(this);
        findViewById(R.id.btn_short_view).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_camera_photo) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.CAMERA}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, CameraPhotoActivity.class));
            }
        } else if (v.getId() == R.id.btn_camera_record) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, CameraRecordActivity.class));
            }
        } else if (v.getId() == R.id.btn_video_controller) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, VideoControllerActivity.class));
            }
        } else if (v.getId() == R.id.btn_camera2_photo) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.CAMERA}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, Camera2PhotoActivity.class));
            }
        } else if (v.getId() == R.id.btn_camera2_record) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, Camera2RecordActivity.class));
            }
        } else if (v.getId() == R.id.btn_exo_player) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, ExoPlayerActivity.class));
            }
        } else if (v.getId() == R.id.btn_video_frame) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, VideoFrameActivity.class));
            }
        } else if (v.getId() == R.id.btn_float_window) {
            startActivity(new Intent(this, FloatWindowActivity.class));
        } else if (v.getId() == R.id.btn_screen_capture) {
            if (PermissionUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, R.id.btn_screen_capture % 4096)) {
                startActivity(new Intent(this, ScreenCaptureActivity.class));
            }
        } else if (v.getId() == R.id.btn_screen_record) {
            if (PermissionUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, R.id.btn_screen_record % 4096)) {
                startActivity(new Intent(this, ScreenRecordActivity.class));
            }
        } else if (v.getId() == R.id.btn_short_view) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, ShortViewActivity.class));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // requestCode不能为负数，也不能大于2的16次方即65536
        if (requestCode == R.id.btn_camera_photo % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, CameraPhotoActivity.class));
            } else {
                Toast.makeText(this, "需要允许摄像头权限才能拍照噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_camera_record % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, CameraRecordActivity.class));
            } else {
                Toast.makeText(this, "需要允许摄像头和录音权限才能录像噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_video_controller % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, VideoControllerActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储权限才能播放视频噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_camera2_photo % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, Camera2PhotoActivity.class));
            } else {
                Toast.makeText(this, "需要允许摄像头权限才能拍照噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_camera2_record % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, Camera2RecordActivity.class));
            } else {
                Toast.makeText(this, "需要允许摄像头和录音权限才能录像噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_exo_player % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, ExoPlayerActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储权限才能播放视频噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_video_frame % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, VideoFrameActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储权限才能截取视频画面噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_screen_capture % 4096) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, ScreenCaptureActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储权限才能截屏噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_screen_record % 4096) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, ScreenRecordActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储权限才能录屏噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_short_view % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, ShortViewActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储、摄像头和录音权限才能分享短视频噢", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
