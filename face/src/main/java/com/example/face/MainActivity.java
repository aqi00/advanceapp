package com.example.face;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.face.util.AssetsUtil;
import com.example.face.util.PermissionUtil;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_verify_code).setOnClickListener(this);
        findViewById(R.id.btn_generate_qrcode).setOnClickListener(this);
        findViewById(R.id.btn_scan_qrcode).setOnClickListener(this);
        findViewById(R.id.btn_detect_system).setOnClickListener(this);
        findViewById(R.id.btn_detect_opencv).setOnClickListener(this);
        findViewById(R.id.btn_detect_realtime).setOnClickListener(this);
        findViewById(R.id.btn_compare_image).setOnClickListener(this);
        findViewById(R.id.btn_guess_age).setOnClickListener(this);
        findViewById(R.id.btn_wisdom_eye).setOnClickListener(this);
        new Thread(() -> copyModel()).start();
    }

    private String[] modelArray = {"age_net.caffemodel", "age_deploy.prototxt", "gender_net.caffemodel", "gender_deploy.prototxt"};
    // 复制模型资源到存储卡
    private void copyModel() {
        Log.d(TAG, "copyModel");
        String path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
        for (String model : modelArray) {
            File file = new File(path + model);
            if (!file.exists()) {
                Log.d(TAG, "import "+path+model);
                AssetsUtil.Assets2Sd(this, model, path+model);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_verify_code) {
            startActivity(new Intent(this, VerifyCodeActivity.class));
        } else if (v.getId() == R.id.btn_generate_qrcode) {
            startActivity(new Intent(this, GenerateQrcodeActivity.class));
        } else if (v.getId() == R.id.btn_scan_qrcode) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, ScanQrcodeActivity.class));
            }
        } else if (v.getId() == R.id.btn_detect_system) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, DetectSystemActivity.class));
            }
        } else if (v.getId() == R.id.btn_detect_opencv) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, DetectOpencvActivity.class));
            }
        } else if (v.getId() == R.id.btn_detect_realtime) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, DetectRealtimeActivity.class));
            }
        } else if (v.getId() == R.id.btn_compare_image) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, CompareImageActivity.class));
            }
        } else if (v.getId() == R.id.btn_guess_age) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, GuessAgeActivity.class));
            }
        } else if (v.getId() == R.id.btn_wisdom_eye) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, WisdomEyeActivity.class));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // requestCode不能为负数，也不能大于2的16次方即65536
        if (requestCode == R.id.btn_scan_qrcode % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, ScanQrcodeActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储卡和摄像头权限才能扫描二维码噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_detect_system % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, DetectSystemActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储卡和摄像头权限才能检测人脸图像噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_detect_opencv % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, DetectOpencvActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储卡和摄像头权限才能检测人脸图像噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_detect_realtime % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, DetectRealtimeActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储卡和摄像头权限才能实时检测人脸噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_compare_image % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, CompareImageActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储卡和摄像头权限才能判断相似程度噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_guess_age % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, GuessAgeActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储卡和摄像头权限才能猜测年龄噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_wisdom_eye % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, WisdomEyeActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储卡、摄像头和定位权限才能追踪人脸噢", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
