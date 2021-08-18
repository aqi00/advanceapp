package com.example.audio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("DefaultLocale")
public class RingToneActivity extends AppCompatActivity {
    private TextView tv_volume; // 声明一个文本视图对象
    private Ringtone mRingtone; // 声明一个铃声对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring_tone);
        tv_volume = findViewById(R.id.tv_volume);
        initVolumeInfo(); // 初始化音量信息
        initRingSpinner(); // 初始化铃声下拉框
        // 生成本App自带的铃声文件res/raw/ring.ogg的Uri实例
        uriArray[uriArray.length - 1] = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ring);
    }

    // 初始化音量信息
    private void initVolumeInfo() {
        // 从系统服务中获取音频管理器
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 获取铃声的最大音量
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_RING);
        // 获取铃声的当前音量
        int nowVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
        String desc = String.format("当前铃声音量为%d，最大音量为%d，请先将铃声音量调至最大",
                nowVolume, maxVolume);
        tv_volume.setText(desc);
    }

    // 初始化铃声下拉框
    private void initRingSpinner() {
        ArrayAdapter<String> ringAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, ringArray);
        Spinner sp_ring = findViewById(R.id.sp_ring);
        sp_ring.setPrompt("请选择要播放的铃声");
        sp_ring.setAdapter(ringAdapter);
        sp_ring.setOnItemSelectedListener(new RingSelectedListener());
        sp_ring.setSelection(0);
    }

    private String[] ringArray = {"来电铃声", "通知铃声", "闹钟铃声",
            "相机快门声", "视频录制声", "门铃叮咚声"};
    private Uri[] uriArray = {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), // 来电铃声
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), // 通知铃声
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), // 闹钟铃声
            Uri.parse("file:///system/media/audio/ui/camera_click.ogg"), // 相机快门声
            Uri.parse("file:///system/media/audio/ui/VideoRecord.ogg"), // 视频录制声
            null
    };

    class RingSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (mRingtone != null) {
                mRingtone.stop(); // 停止播放铃声
            }
            // 从铃声文件的URI中获取铃声对象
            mRingtone = RingtoneManager.getRingtone(RingToneActivity.this, uriArray[arg2]);
            mRingtone.play(); // 开始播放铃声
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRingtone.stop(); // 停止播放铃声
    }

}
