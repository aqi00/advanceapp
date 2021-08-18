package com.example.audio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.audio.bean.CommitResponse;
import com.example.audio.constant.UrlConstant;
import com.example.audio.util.BitmapUtil;
import com.example.audio.util.DateUtil;
import com.example.audio.widget.AudioController;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StoryEditActivity extends AppCompatActivity {
    private final static String TAG = "StoryEditActivity";
    private ImageView iv_cover; // 声明一个图像视图对象
    private EditText et_artist; // 声明一个编辑框对象
    private EditText et_title; // 声明一个编辑框对象
    private EditText et_desc; // 声明一个编辑框对象
    private AudioController ac_play; // 声明一个音频控制条对象
    private String mAudioPath; // 音频文件路径
    private int CHOOSE_CODE = 3; // 只在相册挑选图片的请求码
    private Bitmap mCoverBitmap; // 声明一个位图对象
    private ProgressDialog mDialog; // 声明一个对话框对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_edit);
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("编辑说书音频");
        iv_cover = findViewById(R.id.iv_cover);
        et_artist = findViewById(R.id.et_artist);
        et_title = findViewById(R.id.et_title);
        et_desc = findViewById(R.id.et_desc);
        ac_play = findViewById(R.id.ac_play);
        findViewById(R.id.iv_cover).setOnClickListener(v -> {
            // 创建一个内容获取动作的意图（准备跳到系统相册）
            Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
            albumIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); // 是否允许多选
            albumIntent.setType("image/*"); // 类型为图像
            startActivityForResult(albumIntent, CHOOSE_CODE); // 打开系统相册
        });
        findViewById(R.id.btn_upload).setOnClickListener(v -> uploadAudio());
        mAudioPath = getIntent().getStringExtra("audio_path");
        ac_play.prepare(mAudioPath); // 准备播放指定路径的音频
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && requestCode == CHOOSE_CODE) { // 从相册返回
            if (intent.getData() != null) { // 从相册选择一张照片
                Uri uri = intent.getData(); // 获得已选择照片的路径对象
                // 根据指定图片的uri，获得自动缩小后的位图对象
                mCoverBitmap = BitmapUtil.getAutoZoomImage(this, uri);
                iv_cover.setImageBitmap(mCoverBitmap); // 设置图像视图的位图对象
            }
        }
    }

    // 执行音频上传动作
    private void uploadAudio() {
        String artist = et_artist.getText().toString();
        String title = et_title.getText().toString();
        String desc = et_desc.getText().toString();
        if (TextUtils.isEmpty(artist)) {
            Toast.makeText(this, "请先输入音频作者名称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "请先输入音频的标题", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            Toast.makeText(this, "请先输入音频的描述", Toast.LENGTH_SHORT).show();
            return;
        }
        // 弹出进度对话框
        mDialog = ProgressDialog.show(this, "请稍候", "正在上传音频信息......");
        String coverPath = String.format("%s/%s.jpg",
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                DateUtil.getNowDateTime());
        BitmapUtil.saveImage(coverPath, mCoverBitmap); // 把位图保存为图片文件
        // 下面把音频信息（包含封面）提交给HTTP服务端
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 往建造器对象添加文本格式的分段数据
        builder.addFormDataPart("artist", artist); // 作者
        builder.addFormDataPart("title", title); // 标题
        builder.addFormDataPart("desc", desc); // 描述
        // 往建造器对象添加图像格式的分段数据
        builder.addFormDataPart("cover", coverPath.substring(coverPath.lastIndexOf("/")),
                RequestBody.create(new File(coverPath), MediaType.parse("image/*")));
        // 往建造器对象添加音频格式的分段数据
        builder.addFormDataPart("audio", mAudioPath.substring(mAudioPath.lastIndexOf("/")),
                RequestBody.create(new File(mAudioPath), MediaType.parse("audio/*")));
        RequestBody body = builder.build(); // 根据建造器生成请求结构

        OkHttpClient client = new OkHttpClient(); // 创建一个okhttp客户端对象
        // 创建一个POST方式的请求结构
        Request request = new Request.Builder().post(body)
                .url(UrlConstant.HTTP_PREFIX+"commitAudio").build();
        Call call = client.newCall(request); // 根据请求结构创建调用对象
        // 加入HTTP请求队列。异步调用，并设置接口应答的回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { // 请求失败
                // 回到主线程操纵界面
                runOnUiThread(() ->  {
                    mDialog.dismiss(); // 关闭进度对话框
                    Toast.makeText(StoryEditActivity.this,
                            "上传音频信息出错："+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException { // 请求成功
                String resp = response.body().string();
                CommitResponse commitResponse = new Gson().fromJson(resp, CommitResponse.class);
                // 回到主线程操纵界面
                runOnUiThread(() -> {
                    mDialog.dismiss(); // 关闭进度对话框
                    if ("0".equals(commitResponse.getCode())) {
                        finishUpload(); // 结束音频上传动作
                    } else {
                        Toast.makeText(StoryEditActivity.this, "上传音频信息失败："+commitResponse.getDesc(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // 结束音频上传动作
    private void finishUpload() {
        Toast.makeText(this, "成功上传您的说书音频", Toast.LENGTH_SHORT).show();
        // 下面重新打开音频列表浏览界面
        Intent intent = new Intent(this, StoryViewActivity.class);
        // 设置启动标志：跳转到新页面时，栈中的原有实例都被清空，同时开辟新任务的活动栈
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
    }

}