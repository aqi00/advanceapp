package com.example.audio;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.audio.widget.VolumeDialog;
import com.example.audio.widget.VolumeDialog.VolumeAdjustListener;

public class VolumeDialogActivity extends AppCompatActivity implements VolumeAdjustListener {
    private TextView tv_volume; // 声明一个文本视图对象
    private VolumeDialog dialog; // 声明一个音量对话框对象
    private AudioManager mAudioMgr; // 声明一个音量管理器对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volume_dialog);
        tv_volume = findViewById(R.id.tv_volume);
        // 从系统服务中获取音量管理器
        mAudioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    // 在发生物理按键动作时触发
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP
                && event.getAction() == KeyEvent.ACTION_DOWN) { // 按下音量加键
            showVolumeDialog(AudioManager.ADJUST_RAISE); // 显示对话框并调大
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                && event.getAction() == KeyEvent.ACTION_DOWN) { // 按下音量减键
            showVolumeDialog(AudioManager.ADJUST_LOWER); // 显示对话框并调小
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) { // 按下返回键
            finish(); // 关闭当前页面
            return false;
        } else { // 其他按键
            return false;
        }
    }

    // 显示音量对话框
    private void showVolumeDialog(int direction) {
        if (dialog == null || !dialog.isShowing()) {
            dialog = new VolumeDialog(this); // 创建一个音量对话框
            dialog.setVolumeAdjustListener(this); // 设置音量对话框的音量调节监听器
            dialog.show(); // 显示音量对话框
        }
        dialog.adjustVolume(direction, true); // 令音量对话框按音量方向调整音量
        onVolumeAdjust(mAudioMgr.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    // 在音量调节完成后触发
    @Override
    public void onVolumeAdjust(int volume) {
        tv_volume.setText("调节后的音乐音量大小为：" + volume);
    }

}
