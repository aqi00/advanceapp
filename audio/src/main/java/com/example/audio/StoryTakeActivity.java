package com.example.audio;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.audio.task.Mp3RecordTask;
import com.example.audio.util.DateUtil;
import com.example.audio.util.MediaUtil;

public class StoryTakeActivity extends AppCompatActivity implements Mp3RecordTask.OnRecordListener {
    private ImageView iv_left; // 声明一个图像视图对象
    private ImageView iv_right; // 声明一个图像视图对象
    private TextView tv_duration; // 声明一个文本视图对象
    private TextView tv_record; // 声明一个文本视图对象
    private ObjectAnimator leftRotateAnim, rightRotateAnim; // 声明两个属性动画对象
    private boolean isRecording = false; // 是否正在录音
    private String mRecordFilePath; // 录制文件的保存路径
    private Mp3RecordTask mRecordTask; // 声明一个MP3录制线程对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_take);
        initView(); // 初始化视图
        initAnim(); // 初始化属性动画
    }

    // 初始化视图
    private void initView() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("录制说书音频");
        iv_left = findViewById(R.id.iv_left);
        iv_right = findViewById(R.id.iv_right);
        tv_duration = findViewById(R.id.tv_duration);
        tv_record = findViewById(R.id.tv_record);
        findViewById(R.id.iv_reset).setOnClickListener(v -> reset());
        findViewById(R.id.iv_record).setOnClickListener(v -> recordMP3());
        findViewById(R.id.iv_save).setOnClickListener(v -> {
            if (isRecording) {
                Toast.makeText(this, "请先停止录音再重录", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, StoryEditActivity.class);
            intent.putExtra("audio_path", mRecordFilePath);
            startActivity(intent); // 打开故事编辑页面
        });
    }

    // 初始化属性动画
    private void initAnim() {
        // 构造一个围绕中心点旋转的属性动画
        leftRotateAnim = ObjectAnimator.ofFloat(iv_left, "rotation", 0f, 360f);
        leftRotateAnim.setDuration(4000); // 设置属性动画的持续时间
        leftRotateAnim.setRepeatCount(ValueAnimator.INFINITE); // 设置属性动画的重播次数。INFINITE表示持续重播
        leftRotateAnim.setInterpolator(new LinearInterpolator()); // 设置属性动画的插值器
        // 构造一个围绕中心点旋转的属性动画
        rightRotateAnim = ObjectAnimator.ofFloat(iv_right, "rotation", 0f, 360f);
        rightRotateAnim.setDuration(4000); // 设置属性动画的持续时间
        rightRotateAnim.setRepeatCount(ValueAnimator.INFINITE); // 设置属性动画的重播次数。INFINITE表示持续重播
        rightRotateAnim.setInterpolator(new LinearInterpolator()); // 设置属性动画的插值器
    }

    // 重新录音
    private void reset() {
        if (isRecording) {
            Toast.makeText(this, "请先停止录音再重录", Toast.LENGTH_SHORT).show();
            return;
        }
        tv_record.setText("开始录音");
        tv_duration.setText("00:00");
    }

    // 录制MP3音频
    private void recordMP3() {
        if (!isRecording) {
            // 生成MP3的文件路径
            mRecordFilePath = String.format("%s/%s.mp3",
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                    DateUtil.getNowDateTime());
            tv_record.setText("停止录音");
            // 创建一个MP3录制线程，并设置录制事件监听器
            mRecordTask = new Mp3RecordTask(this, mRecordFilePath, this);
            mRecordTask.start(); // 启动MP3录制线程
            leftRotateAnim.start(); // 左侧属性动画开始播放
            rightRotateAnim.start(); // 右侧属性动画开始播放
        } else {
            tv_record.setText("开始录音");
            mRecordTask.cancel(); // MP3录制线程取消录音
            leftRotateAnim.cancel(); // 左侧属性动画取消播放
            rightRotateAnim.cancel(); // 右侧属性动画取消播放
        }
        isRecording = !isRecording;
    }

    // 在录音进度更新时触发
    @Override
    public void onRecordUpdate(int duration) {
        tv_duration.setText(MediaUtil.formatDuration(duration*1000));
    }

    // 在录音完成时触发
    @Override
    public void onRecordFinish() {
        //Toast.makeText(this, "已结束录音，音频文件路径为"+mRecordFilePath, Toast.LENGTH_LONG).show();
    }
}