package com.example.audio;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.audio.util.DateUtil;
import com.example.audio.util.MediaUtil;

import java.util.Timer;
import java.util.TimerTask;

public class AudioCommonActivity extends AppCompatActivity {
    private static final String TAG = "AudioCommonActivity";
    private Button btn_record; // 声明一个按钮对象
    private LinearLayout ll_record_progress; // 声明一个线性布局对象
    private ProgressBar pb_record_progress; // 声明一个进度条对象
    private TextView tv_record_progress; // 声明一个文本视图对象
    private Button btn_play; // 声明一个按钮对象
    private LinearLayout ll_play_progress; // 声明一个线性布局对象
    private ProgressBar pb_play_progress; // 声明一个进度条对象
    private TextView tv_play_progress; // 声明一个文本视图对象

    private int mAudioEncoder; // 音频编码
    private int mOutputFormat; // 输出格式
    private int mDuration; // 录制时长
    private String mRecordFilePath; // 录音文件的保存路径
    
    private MediaRecorder mMediaRecorder = new MediaRecorder(); // 媒体录制器
    private boolean isRecording = false; // 是否正在录音
    private Timer mRecordTimer = new Timer(); // 录音计时器
    private int mRecordTimeCount; // 录音时间计数
    
    private MediaPlayer mMediaPlayer = new MediaPlayer(); // 媒体播放器
    private boolean isPlaying = false; // 是否正在播音
    private Timer mPlayTimer = new Timer(); // 录音计时器
    private int mPlayTimeCount; // 录音时间计数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_common);
        btn_record = findViewById(R.id.btn_record);
        ll_record_progress = findViewById(R.id.ll_record_progress);
        pb_record_progress = findViewById(R.id.pb_record_progress);
        tv_record_progress = findViewById(R.id.tv_record_progress);
        btn_play = findViewById(R.id.btn_play);
        ll_play_progress = findViewById(R.id.ll_play_progress);
        pb_play_progress = findViewById(R.id.pb_play_progress);
        tv_play_progress = findViewById(R.id.tv_play_progress);
        btn_record.setOnClickListener(v -> {
            if (!isRecording) { // 未在录音
                startRecord(); // 开始录音
            } else { // 正在录音
                stopRecord(); // 停止录音
            }
        });
        btn_play.setOnClickListener(v -> {
            if (!isPlaying) { // 未在播音
                startPlay(); // 开始播音
            } else { // 正在播音
                stopPlay(); // 停止播音
            }
        });
        initEncoderSpinner(); // 初始化音频编码的下拉框
        initFormatSpinner(); // 初始化输出格式的下拉框
        initDurationSpinner(); // 初始化录制时长的下拉框
    }

    // 初始化音频编码的下拉框
    private void initEncoderSpinner() {
        ArrayAdapter<String> encoderAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, encoderDescArray);
        Spinner sp_encoder = findViewById(R.id.sp_encoder);
        sp_encoder.setPrompt("请选择音频编码");
        sp_encoder.setAdapter(encoderAdapter);
        sp_encoder.setOnItemSelectedListener(new EncoderSelectedListener());
        sp_encoder.setSelection(0);
    }

    private String[] encoderDescArray = {
            "默认编码",
            "窄带编码",
            "宽带编码",
            "低复杂度的高级编码",
            "高效率的高级编码",
            "增强型低延时的高级编码"
    };
    private int[] encoderArray = {
            MediaRecorder.AudioEncoder.DEFAULT,
            MediaRecorder.AudioEncoder.AMR_NB,
            MediaRecorder.AudioEncoder.AMR_WB,
            MediaRecorder.AudioEncoder.AAC,
            MediaRecorder.AudioEncoder.HE_AAC,
            MediaRecorder.AudioEncoder.AAC_ELD
    };

    class EncoderSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mAudioEncoder = encoderArray[arg2];
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    // 初始化输出格式的下拉框
    private void initFormatSpinner() {
        ArrayAdapter<String> formatAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, formatDescArray);
        Spinner sp_format = findViewById(R.id.sp_format);
        sp_format.setPrompt("请选择输出格式");
        sp_format.setAdapter(formatAdapter);
        sp_format.setSelection(0);
        sp_format.setOnItemSelectedListener(new FormatSelectedListener());
    }

    private String[] formatDescArray = {
            "默认格式",
            "窄带格式",
            "宽带格式",
            "高级的音频传输流格式"
    };
    private int[] formatArray = {
            MediaRecorder.OutputFormat.DEFAULT,
            MediaRecorder.OutputFormat.AMR_NB,
            MediaRecorder.OutputFormat.AMR_WB,
            MediaRecorder.OutputFormat.AAC_ADTS
    };
    class FormatSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mOutputFormat = formatArray[arg2];
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    // 初始化录制时长的下拉框
    private void initDurationSpinner() {
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, durationDescArray);
        Spinner sp_duration = findViewById(R.id.sp_duration);
        sp_duration.setPrompt("请选择录制时长");
        sp_duration.setAdapter(durationAdapter);
        sp_duration.setSelection(0);
        sp_duration.setOnItemSelectedListener(new DurationSelectedListener());
    }

    private String[] durationDescArray = {"5秒", "10秒", "20秒", "30秒", "60秒"};
    private int[] durationArray = {5, 10, 20, 30, 60};
    class DurationSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mDuration = durationArray[arg2];
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    // 开始录音
    private void startRecord() {
        Log.d(TAG, "startRecord mAudioEncoder="+mAudioEncoder+", mOutputFormat="+mOutputFormat+", mDuration="+mDuration);
        ll_record_progress.setVisibility(View.VISIBLE);
        isRecording = !isRecording;
        btn_record.setText("停止录音");
        pb_record_progress.setMax(mDuration); // 设置进度条的最大值
        mRecordTimeCount = 0; // 录音时间计数清零
        mRecordTimer = new Timer(); // 创建一个录音计时器
        mRecordTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                pb_record_progress.setProgress(mRecordTimeCount); // 设置进度条的当前进度
                tv_record_progress.setText(MediaUtil.formatDuration(mRecordTimeCount*1000));
                mRecordTimeCount++;
            }
        }, 0, 1000); // 计时器每隔一秒就更新进度条上的录音进度
        // 获取本次录制的媒体文件路径
        mRecordFilePath = String.format("%s/%s.amr",
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                DateUtil.getNowDateTime());
        // 下面是媒体录制器的处理代码
        mMediaRecorder.reset(); // 重置媒体录制器
        // 设置媒体录制器的信息监听器
        mMediaRecorder.setOnInfoListener((mr, what, extra) -> {
            // 录制达到最大时长，或者达到文件大小限制，都停止录制
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
                    || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                stopRecord(); // 停止录音
            }
        });
        // 设置音频源为麦克风
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(mOutputFormat); // 设置媒体的输出格式。该方法要先于setAudioEncoder调用
        mMediaRecorder.setAudioEncoder(mAudioEncoder); // 设置媒体的音频编码器
        mMediaRecorder.setAudioSamplingRate(8000); // 设置媒体的音频采样率。可选
        // mMediaRecorder.setAudioChannels(2); // 设置媒体的音频声道数。可选
        // mMediaRecorder.setAudioEncodingBitRate(1024); // 设置音频每秒录制的字节数。可选
        mMediaRecorder.setMaxDuration(mDuration * 1000); // 设置媒体的最大录制时长
        // mMediaRecorder.setMaxFileSize(1024*1024*10); // 设置媒体的最大文件大小
        // setMaxFileSize与setMaxDuration设置其一即可
        mMediaRecorder.setOutputFile(mRecordFilePath); // 设置媒体文件的保存路径
        try {
            mMediaRecorder.prepare(); // 媒体录制器准备就绪
            mMediaRecorder.start(); // 媒体录制器开始录制
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 停止录音
    private void stopRecord() {
        btn_record.setText("开始录音");
        mRecordTimer.cancel(); // 取消录音定时器
        if (isRecording) {
            isRecording = !isRecording;
            mMediaRecorder.stop(); // 媒体录制器停止录制
            Toast.makeText(this, "已结束录音，音频文件路径为"+mRecordFilePath, Toast.LENGTH_LONG).show();
            btn_play.setVisibility(View.VISIBLE);
        }
    }

    // 开始播音
    private void startPlay() {
        ll_play_progress.setVisibility(View.VISIBLE);
        isPlaying = !isPlaying;
        btn_play.setText("停止播音");
        mMediaPlayer.reset(); // 重置媒体播放器
        // 设置媒体播放器的完成监听器
        mMediaPlayer.setOnCompletionListener(mp -> stopPlay());
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // 设置音频流的类型为音乐
        try {
            mMediaPlayer.setDataSource(mRecordFilePath); // 设置媒体数据的文件路径
            mMediaPlayer.prepare(); // 媒体播放器准备就绪
            mMediaPlayer.start(); // 媒体播放器开始播放
        } catch (Exception e) {
            e.printStackTrace();
        }
        pb_play_progress.setMax(mMediaPlayer.getDuration()/1000); // 设置进度条的最大值
        mPlayTimeCount = 0; // 播音时间计数清零
        mPlayTimer = new Timer(); // 创建一个播音计时器
        mPlayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                pb_play_progress.setProgress(mPlayTimeCount); // 设置进度条的当前进度
                tv_play_progress.setText(MediaUtil.formatDuration(mPlayTimeCount*1000));
                mPlayTimeCount++;
            }
        }, 0, 1000); // 计时器每隔一秒就更新进度条上的播音进度
    }

    // 停止播音
    private void stopPlay() {
        btn_play.setText("开始播音");
        mPlayTimer.cancel(); // 取消播音定时器
        if (mMediaPlayer.isPlaying() || isPlaying) { // 如果正在播放
            isPlaying = !isPlaying;
            mMediaPlayer.stop(); // 停止播放
            Toast.makeText(this, "已结束播音", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecord(); // 停止录音
        stopPlay(); // 停止播音
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaRecorder.release(); // 释放媒体录制器
        mMediaPlayer.release(); // 释放媒体播放器
    }
}
