package com.example.audio;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.audio.util.PermissionUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_slider).setOnClickListener(this);
        findViewById(R.id.btn_audio_manager).setOnClickListener(this);
        findViewById(R.id.btn_volume_dialog).setOnClickListener(this);
        findViewById(R.id.btn_audio_common).setOnClickListener(this);
        findViewById(R.id.btn_audio_raw).setOnClickListener(this);
        findViewById(R.id.btn_audio_controller).setOnClickListener(this);
        findViewById(R.id.btn_ring_tone).setOnClickListener(this);
        findViewById(R.id.btn_sound_pool).setOnClickListener(this);
        findViewById(R.id.btn_wav_record).setOnClickListener(this);
        findViewById(R.id.btn_mp3_record).setOnClickListener(this);
        findViewById(R.id.btn_story_view).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_slider) {
            startActivity(new Intent(this, SliderActivity.class));
        } else if (v.getId() == R.id.btn_audio_manager) {
            startActivity(new Intent(this, AudioManagerActivity.class));
        } else if (v.getId() == R.id.btn_volume_dialog) {
            startActivity(new Intent(this, VolumeDialogActivity.class));
        } else if (v.getId() == R.id.btn_audio_common) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.RECORD_AUDIO}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, AudioCommonActivity.class));
            }
        } else if (v.getId() == R.id.btn_audio_raw) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.RECORD_AUDIO}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, AudioRawActivity.class));
            }
        } else if (v.getId() == R.id.btn_audio_controller) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, AudioControllerActivity.class));
            }
        } else if (v.getId() == R.id.btn_ring_tone) {
            startActivity(new Intent(this, RingToneActivity.class));
        } else if (v.getId() == R.id.btn_sound_pool) {
            startActivity(new Intent(this, SoundPoolActivity.class));
        } else if (v.getId() == R.id.btn_wav_record) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.RECORD_AUDIO}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, WavRecordActivity.class));
            }
        } else if (v.getId() == R.id.btn_mp3_record) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.RECORD_AUDIO}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, Mp3RecordActivity.class));
            }
        } else if (v.getId() == R.id.btn_story_view) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.RECORD_AUDIO}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, StoryViewActivity.class));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // requestCode不能为负数，也不能大于2的16次方即65536
        if (requestCode == R.id.btn_audio_common % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, AudioCommonActivity.class));
            } else {
                Toast.makeText(this, "需要允许录音权限才能录制音频噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_audio_raw % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, AudioRawActivity.class));
            } else {
                Toast.makeText(this, "需要允许录音权限才能录制音频噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_audio_controller % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, AudioControllerActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储权限才能播放音频噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_wav_record % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, WavRecordActivity.class));
            } else {
                Toast.makeText(this, "需要允许录音权限才能录制音频噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_mp3_record % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, Mp3RecordActivity.class));
            } else {
                Toast.makeText(this, "需要允许录音权限才能录制音频噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_story_view % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, StoryViewActivity.class));
            } else {
                Toast.makeText(this, "需要允许录音权限才能分享音频噢", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
