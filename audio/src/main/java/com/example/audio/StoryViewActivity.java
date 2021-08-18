package com.example.audio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.audio.adapter.StoryRecyclerAdapter;
import com.example.audio.bean.AudioInfo;
import com.example.audio.task.AudioLoadTask;

import java.util.ArrayList;
import java.util.List;

public class StoryViewActivity extends AppCompatActivity {
    private SwipeRefreshLayout srl_dynamic; // 声明一个下拉刷新布局对象
    private List<AudioInfo> mAudioList = new ArrayList<>(); // 声明一个地址列表
    private StoryRecyclerAdapter mAdapter; // 声明一个故事循环适配器对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_view);
        initView(); // 初始化视图
        startLoad(); // 开始加载音频列表
    }

    // 初始化视图
    private void initView() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("声音里的故事");
        srl_dynamic = findViewById(R.id.srl_dynamic);
        srl_dynamic.setOnRefreshListener(() -> startLoad()); // 设置下拉布局的下拉刷新监听器
        RecyclerView rv_content = findViewById(R.id.rv_content);
        // 创建一个每行三列的网格布局管理器
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        rv_content.setLayoutManager(manager); // 设置循环视图的布局管理器
        // 构建一个故事信息的循环适配器
        mAdapter = new StoryRecyclerAdapter(this, mAudioList, position -> {
            Intent intent = new Intent(this, StoryListenActivity.class);
            intent.putExtra("audio_info", mAudioList.get(position));
            startActivity(intent); // 打开听书页面
        });
        rv_content.setAdapter(mAdapter); // 设置循环视图的故事适配器
        findViewById(R.id.iv_add).setOnClickListener(v ->
                startActivity(new Intent(this, StoryTakeActivity.class)));
    }

    // 开始加载音频列表
    private void startLoad() {
        // 创建一个音频列表加载任务
        AudioLoadTask task = new AudioLoadTask(this, audioList -> {
            srl_dynamic.setRefreshing(false); // 结束下拉刷新布局的刷新动作
            mAudioList.clear();
            mAudioList.addAll(audioList);
            mAdapter.notifyDataSetChanged(); // 通知适配器数据发生变化
        });
        task.start(); // 启动音频列表加载任务
    }

}