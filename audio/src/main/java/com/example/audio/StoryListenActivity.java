package com.example.audio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.audio.bean.AudioInfo;
import com.example.audio.bean.JoinInfo;
import com.example.audio.bean.MessageInfo;
import com.example.audio.constant.UrlConstant;
import com.example.audio.util.DateUtil;
import com.example.audio.util.SocketUtil;
import com.example.audio.util.Utils;
import com.example.audio.util.ViewUtil;
import com.example.audio.widget.AudioController;
import com.example.audio.widget.BarrageView;
import com.google.gson.Gson;

import org.json.JSONObject;

import io.socket.client.Socket;

public class StoryListenActivity extends AppCompatActivity {
    private final static String TAG = "StoryListenActivity";
    private TextView tv_title; // 声明一个文本视图对象
    private TextView tv_artist; // 声明一个文本视图对象
    private ImageView iv_cover; // 声明一个图像视图对象
    private TextView tv_desc; // 声明一个文本视图对象
    private BarrageView bv_comment; // 声明一个弹幕视图对象
    private AudioController ac_play; // 声明一个音频控制条对象
    private EditText et_input; // 声明一个编辑框对象
    private String mSelfName, mGroupName; // 自己名称，群组名称
    private Socket mSocket; // 声明一个套接字对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_listen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持屏幕常亮
        initView(); // 初始化视图
        playStory(); // 播放故事音频
        initSocket(); // 初始化套接字
    }

    // 初始化视图
    private void initView() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        tv_title = findViewById(R.id.tv_title);
        tv_artist = findViewById(R.id.tv_artist);
        iv_cover = findViewById(R.id.iv_cover);
        tv_desc = findViewById(R.id.tv_desc);
        bv_comment = findViewById(R.id.bv_comment);
        ac_play = findViewById(R.id.ac_play);
        et_input = findViewById(R.id.et_input);
        findViewById(R.id.btn_send).setOnClickListener(v -> sendMessage());
    }

    // 播放故事音频
    private void playStory() {
        AudioInfo audio = (AudioInfo) getIntent().getSerializableExtra("audio_info");
        mSelfName = DateUtil.getFullDateTime();
        mGroupName = audio.getTitle();
        tv_artist.setText(String.format("《%s》%s", audio.getTitle(), audio.getArtist()));
        tv_title.setText(audio.getTitle());
        tv_desc.setText(audio.getDesc());
        // 使用Glide加载圆角矩形裁剪后的故事封面
        RoundedCorners roundedCorners = new RoundedCorners(Utils.dip2px(this, 30));
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners);
        Glide.with(this).load(UrlConstant.HTTP_PREFIX+audio.getCover()).apply(options).into(iv_cover);
        ac_play.prepareAsync(UrlConstant.HTTP_PREFIX+audio.getAudio()); // 准备播放指定链接的网络音频
    }

    // 初始化套接字
    private void initSocket() {
        mSocket = MainApplication.getInstance().getSocket();
        mSocket.connect(); // 建立Socket连接
        // 等待接收弹幕消息
        mSocket.on("receive_group_message", (args) -> {
            JSONObject json = (JSONObject) args[0];
            MessageInfo message = new Gson().fromJson(json.toString(), MessageInfo.class);
            // 往故事窗口添加弹幕评论
            runOnUiThread(() -> bv_comment.addComment(message.content));
        });
        // 下面向Socket服务器发送入群通知
        JoinInfo joinInfo = new JoinInfo(mSelfName, mGroupName);
        SocketUtil.emit(mSocket, "join_group", joinInfo);
    }

    // 发送评论消息
    private void sendMessage() {
        String content = et_input.getText().toString();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "请输入评论消息", Toast.LENGTH_SHORT).show();
            return;
        }
        et_input.setText("");
        ViewUtil.hideOneInputMethod(this, et_input); // 隐藏软键盘
        bv_comment.addComment(content); // 给弹幕视图添加评论
        // 下面向Socket服务器发送群消息
        MessageInfo message = new MessageInfo(mSelfName, mGroupName, content);
        SocketUtil.emit(mSocket, "send_group_message", message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ac_play.resume(); // 恢复播放
    }

    @Override
    protected void onPause() {
        super.onPause();
        ac_play.pause(); // 暂停播放
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ac_play.release(); // 释放播放资源
        if (mSocket.connected()) { // 已经连上Socket服务器
            // 下面向Socket服务器发送退群通知
            JoinInfo joinInfo = new JoinInfo(mSelfName, mGroupName);
            SocketUtil.emit(mSocket, "leave_group", joinInfo);
            mSocket.off("receive_group_message"); // 取消接收弹幕消息
            mSocket.disconnect(); // 断开Socket连接
        }
    }

}