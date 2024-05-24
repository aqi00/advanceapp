package com.example.video;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.video.constant.UrlConstant;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.util.Locale;

public class ExoPlayerActivity extends AppCompatActivity {
    private final static String TAG = "ExoPlayerActivity";
    private final static String URL_HTTPS = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4";
    private final static String URL_VIDEO = UrlConstant.HTTP_PREFIX + "海洋世界.mp4";
    private final static String URL_SUBTITLE = UrlConstant.HTTP_PREFIX + "海洋世界.srt";
    private int CHOOSE_CODE = 3; // 只在视频库挑选图片的请求码
    private SimpleExoPlayer mPlayer; // 声明一个新型播放器对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_player);
        PlayerView pv_content = findViewById(R.id.pv_content);
        findViewById(R.id.btn_play_local).setOnClickListener(v -> {
            // ACTION_GET_CONTENT只可选择近期的视频
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            // ACTION_PICK可选择所有视频
            //Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("video/*"); // 类型为视频
            startActivityForResult(intent, CHOOSE_CODE); // 打开系统视频库
        });
        findViewById(R.id.btn_play_network).setOnClickListener(v -> playVideo(Uri.parse(URL_HTTPS)));
        findViewById(R.id.btn_play_subtitle).setOnClickListener(v -> {
            Uri videoUri = Uri.parse(URL_VIDEO);
            Uri subtitleUri = Uri.parse(URL_SUBTITLE);
            playVideoWithSubtitle(videoUri, subtitleUri); // 播放带字幕的视频
        });
        mPlayer = new SimpleExoPlayer.Builder(this).build();
        pv_content.setPlayer(mPlayer); // 设置播放器视图的播放器对象
        initWelcomeSpinner(); // 初始化迎宾曲下拉框
    }

    // 播放视频
    private void playVideo(Uri uri) {
        DataSource.Factory factory;
        // 创建指定视频格式的工厂对象
        if (uri.toString().startsWith("http")) { // HTTP在线视频
            factory = new DefaultHttpDataSourceFactory(
                    Util.getUserAgent(this, getString(R.string.app_name)),
                    new DefaultBandwidthMeter.Builder(this).build());
        } else { // 本地存储卡视频
           factory = new DefaultDataSourceFactory(this);
        }
        // 创建指定地址的媒体对象
        MediaItem videoItem = new MediaItem.Builder().setUri(uri).build();
        // 基于工厂对象和媒体对象创建媒体来源
        MediaSource videoSource = new ProgressiveMediaSource.Factory(factory)
                .createMediaSource(videoItem);
        mPlayer.setMediaSource(videoSource); // 设置播放器的媒体来源
        // 给播放器添加事件监听器
        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) { // 视频正在缓冲
                    Log.d(TAG, "视频正在缓冲");
                } else if (state == Player.STATE_READY) { // 视频准备就绪
                    Log.d(TAG, "视频准备就绪");
                } else if (state == Player.STATE_ENDED) { // 视频播放完毕
                    Log.d(TAG, "视频播放完毕");
                }
            }
        });
        mPlayer.prepare(); // 播放器准备就绪
        mPlayer.play(); // 播放器开始播放
    }

    // 播放带字幕的视频
    private void playVideoWithSubtitle(Uri videoUri, Uri subtitleUri) {
        Log.d(TAG, "getLanguage="+Locale.getDefault().getLanguage());
        // 创建HTTP在线视频的工厂对象
        DataSource.Factory factory = new DefaultHttpDataSourceFactory(
                Util.getUserAgent(this, getString(R.string.app_name)),
                new DefaultBandwidthMeter.Builder(this).build());
        // 创建指定地址的媒体对象
        MediaItem videoItem = new MediaItem.Builder().setUri(videoUri).build();
                // 基于工厂对象和媒体对象创建媒体来源
        MediaSource videoSource = new ProgressiveMediaSource.Factory(factory)
                .createMediaSource(videoItem);
        // 语言要填null，否则中文会乱码。selectionFlags要填Format.NO_VALUE，否则看不到字幕
        // 创建指定地址的字幕对象。ExoPlayer只支持srt字幕，不支持ass字幕
        MediaItem.Subtitle subtitleItem = new MediaItem.Subtitle(subtitleUri,
                MimeTypes.APPLICATION_SUBRIP, null, Format.NO_VALUE);
        // 基于工厂对象和字幕对象创建字幕来源
        MediaSource subtitleSource = new SingleSampleMediaSource.Factory(factory)
                .createMediaSource(subtitleItem, C.TIME_UNSET);
        // 合并媒体来源与字幕来源
        MergingMediaSource mergingSource = new MergingMediaSource(videoSource, subtitleSource);
        mPlayer.setMediaSource(mergingSource); // 设置播放器的媒体来源
        mPlayer.prepare(); // 播放器准备就绪
        mPlayer.play(); // 播放器开始播放
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && requestCode == CHOOSE_CODE) { // 从视频库回来
            if (intent.getData() != null) {
                playVideo(intent.getData()); // 播放视频
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.release(); // 释放播放器资源
    }

    private long mCurrentPosition = 0; // 当前的播放位置
    @Override
    public void onResume() {
        super.onResume();
        // 恢复页面时立即从上次断点开始播放视频
        if (mCurrentPosition>0 && !mPlayer.isPlaying()) {
            mPlayer.seekTo(mCurrentPosition); // 找到指定位置
        }
        mPlayer.play(); // 播放器开始播放
    }

    @Override
    public void onPause() {
        super.onPause();
        // 暂停页面时保存当前的播放进度
        if (mPlayer.isPlaying()) { // 播放器正在播放
            // 获得播放器当前的播放位置
            mCurrentPosition = mPlayer.getCurrentPosition();
            mPlayer.pause(); // 播放器暂停播放
        }
    }

    // 初始化迎宾曲下拉框
    private void initWelcomeSpinner() {
        ArrayAdapter<String> welcomeAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, welcomeArray);
        Spinner sp_welcome = findViewById(R.id.sp_welcome);
        sp_welcome.setPrompt("请选择要播放的迎宾曲");
        sp_welcome.setAdapter(welcomeAdapter);
        sp_welcome.setOnItemSelectedListener(new WelcomeSelectedListener());
        sp_welcome.setSelection(3);
    }

    private String[] welcomeArray = {"首届（2018年）", "第二届（2019年）", "第三届（2020年）", "第四届（2021年）", "第五届（2022年）", "第六届（2023年）", "第七届（2024年）"};
    private String[] urlArray = {
            "https://ptgl.fujian.gov.cn:8088/masvod/public/2018/04/17/20180417_162d3639356_r38_1200k.mp4",
            "https://ptgl.fujian.gov.cn:8088/masvod/public/2019/04/15/20190415_16a1ef11c24_r38_1200k.mp4",
            "https://ptgl.fujian.gov.cn:8088/masvod/public/2020/09/26/20200926_174c8f9e4b6_r38_1200k.mp4",
            "https://ptgl.fujian.gov.cn:8088/masvod/public/2021/03/19/20210319_178498bcae9_r38.mp4",
            "https://www.fujian.gov.cn/masvod/public/2022/07/15/20220715_18201603713_r38_1200k.mp4",
            "https://www.fujian.gov.cn/masvod/public/2023/04/25/20230425_187b71018de_r38_1200k.mp4",
            "https://video.zohi.tv/fs/transcode/20240520/8cc/355193-1716184798-transv.mp4"
    };

    class WelcomeSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            playVideo(Uri.parse(urlArray[arg2])); // 播放视频
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

}