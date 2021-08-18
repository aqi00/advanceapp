package com.example.audio.widget;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.audio.R;
import com.example.audio.util.DateUtil;

public class AudioController extends RelativeLayout implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "AudioController";
    private ImageView iv_play; // 声明用于播放控制的图像视图对象
    private TextView tv_current; // 声明用于展示当前时间的文本视图对象
    private TextView tv_total; // 声明用于展示播放时长的文本视图对象
    private SeekBar sb_progress; // 声明一个拖动条对象
    private MediaPlayer mMediaPlayer = new MediaPlayer(); // 声明一个媒体播放器对象
    private int mDuration = 0; // 播放时长，单位毫秒
    private Handler mHandler = new Handler(Looper.myLooper()); // 声明一个处理器对象

    public AudioController(Context context) {
        this(context, null);
    }

    public AudioController(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context); // 初始化视图
    }

    // 初始化视图
    private void initView(Context context) {
        // 根据布局文件bar_controller.xml生成视图对象
        View view = LayoutInflater.from(context).inflate(R.layout.bar_controller, null);
        iv_play = view.findViewById(R.id.iv_play);
        tv_current = view.findViewById(R.id.tv_current);
        tv_total = view.findViewById(R.id.tv_total);
        sb_progress = view.findViewById(R.id.sb_progress);
        iv_play.setOnClickListener(v -> {
            if (mMediaPlayer.isPlaying()) { // 媒体播放器正在播放
                mMediaPlayer.pause(); // 媒体播放器暂停播放
                iv_play.setImageResource(R.drawable.btn_play);
            } else { // 媒体播放器不在播放
                mMediaPlayer.start(); // 媒体播放器开始播放
                iv_play.setImageResource(R.drawable.btn_pause);
            }
        });
        iv_play.setEnabled(false);
        sb_progress.setEnabled(false);
        sb_progress.setOnSeekBarChangeListener(this); // 设置拖动条的拖动变更监听器
        addView(view); // 添加至当前视图
    }

    // 在进度变更时触发。第三个参数为true表示用户拖动，为false表示代码设置进度
    // 如果是人为的改变进度（即用户拖动进度条），则令音频从指定时间点开始播放
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int time = progress * mDuration / 100; // 计算拖动后的当前时间进度
            mMediaPlayer.seekTo(time); // 拖动播放器的当前进度到指定位置
        }
    }

    // 在开始拖动进度时触发
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    // 在停止拖动进度时触发
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    // 公共的准备播放操作
    private void prepareCommon() {
        setVisibility(View.VISIBLE);
        iv_play.setEnabled(true);
        sb_progress.setEnabled(true);
        mMediaPlayer.reset(); // 重置媒体播放器
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // 设置音频流的类型为音乐
        // 给媒体播放器设置播放完成监听器，播放完毕重置当前进度
        mMediaPlayer.setOnCompletionListener(mp -> setCurrentTime(0));
        mHandler.post(mRefresh); // 立即启动进度刷新任务
    }

    // 准备播放指定路径的音频
    public void prepare(String audioPath) {
        prepareCommon(); // 公共的准备播放操作
        Log.d(TAG, "audioPath="+audioPath);
        try {
            mMediaPlayer.setDataSource(audioPath); // 设置媒体数据的文件路径
            mMediaPlayer.prepare(); // 媒体播放器准备就绪
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 给媒体播放器设置播放准备监听器，准备完毕获取播放时长
        mMediaPlayer.setOnPreparedListener(mp -> mDuration = mMediaPlayer.getDuration());
    }

    // 异步加载指定路径的音频
    public void prepareAsync(String audioPath) {
        prepareCommon(); // 公共的准备播放操作
        Log.d(TAG, "audioPath="+audioPath);
        try {
            mMediaPlayer.setDataSource(audioPath); // 设置媒体数据的文件路径
            mMediaPlayer.prepareAsync(); // 媒体播放器异步加载
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 给媒体播放器设置播放准备监听器
        mMediaPlayer.setOnPreparedListener(mp -> {
            mDuration = mMediaPlayer.getDuration(); // 准备完毕获取播放时长
            mMediaPlayer.start(); // 媒体播放器开始播放
        });
    }

    // 开始播放
    public void start() {
        if (mDuration > 0) {
            mMediaPlayer.start(); // 媒体播放器开始播放
        }
    }

    // 定义一个控制条的进度刷新任务。实时刷新控制条的播放进度，每隔0.5秒刷新一次
    private Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer.isPlaying()) { // 媒体播放器正在播放
                // 给音频控制条设置当前的播放位置
                setCurrentTime(mMediaPlayer.getCurrentPosition());
            }
            // 延迟500毫秒后再次启动进度刷新任务
            mHandler.postDelayed(this, 500);
        }
    };

    // 设置当前的播放时间，同步MediaPlayer的播放进度
    public void setCurrentTime(int current_time) {
        if (current_time == 0 || !mMediaPlayer.isPlaying()) { // 在开头或者处于暂停状态
            iv_play.setImageResource(R.drawable.btn_play); // 显示播放图标
        } else { // 处于播放状态
            iv_play.setImageResource(R.drawable.btn_pause); // 显示暂停图标
        }
        tv_current.setText(DateUtil.formatTime(current_time)); // 显示当前时间
        tv_total.setText(DateUtil.formatTime(mDuration)); //显示总时长
        if (mDuration == 0) { // 播放时长为零
            sb_progress.setProgress(0); // 设置拖动条的当前进度为零
        } else { // 播放时长非零
            // 设置拖动条的当前进度为播放进度
            sb_progress.setProgress((current_time == 0) ? 0 : (current_time * 100 / mDuration));
        }
    }

    private int mCurrentPosition = 0; // 当前的播放位置
    // 恢复播放
    public void resume() {
        // 恢复页面时立即从上次断点开始播放音频
        if (mCurrentPosition>0 && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(mCurrentPosition); // 找到指定位置
            mMediaPlayer.start(); // 媒体播放器开始播放
        }
    }

    // 暂停播放
    public void pause() {
        // 暂停页面时保存当前的播放进度
        if (mMediaPlayer.isPlaying()) { // 媒体播放器正在播放
            // 获得媒体播放器当前的播放位置
            mCurrentPosition = mMediaPlayer.getCurrentPosition();
            mMediaPlayer.pause(); // 媒体播放器暂停播放
        }
    }

    // 释放播放资源
    public void release() {
        mHandler.removeCallbacksAndMessages(null); // 移除所有的处理器任务
        if (mMediaPlayer.isPlaying()) { // 是否正在播放
            mMediaPlayer.stop(); // 结束播放
        }
        mMediaPlayer.release(); // 释放媒体播放器
    }

}
