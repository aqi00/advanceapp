package com.example.network;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.network.util.DateUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadExecutorActivity extends AppCompatActivity {
    private final static String TAG = "ThreadExecutorActivity";
    private TextView tv_desc; // 声明一个文本视图对象
    private String mDesc = "";
    private ExecutorService mThreadPool; // 声明一个线程池对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_executor);
        tv_desc = findViewById(R.id.tv_desc);
        initPoolSpinner(); // 初始化线程池下拉框
    }

    // 初始化线程池下拉框
    private void initPoolSpinner() {
        ArrayAdapter<String> poolAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, poolArray);
        Spinner sp_pool = findViewById(R.id.sp_pool);
        sp_pool.setPrompt("请选择线程池类型");
        sp_pool.setAdapter(poolAdapter);
        sp_pool.setOnItemSelectedListener(new PoolSelectedListener());
        sp_pool.setSelection(0);
    }

    private String[] poolArray = {"无线程池", "单线程线程池", "多线程线程池", "无限制线程池", "自定义线程池", "主线程"};

    class PoolSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (mThreadPool != null) {
                //mThreadPool.shutdown(); // 停止接收新任务，原来的任务继续执行
                mThreadPool.shutdownNow(); // 停止接收新任务，原来的任务停止执行
            }
            mDesc = poolArray[arg2] + "正在处理";
            if (arg2 == 1) { // 单线程线程池
                mThreadPool = Executors.newSingleThreadExecutor();
                startPoolTask(); // 开始执行线程池处理
            } else if (arg2 == 2) { // 多线程线程池
                mThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
                startPoolTask(); // 开始执行线程池处理
            } else if (arg2 == 3) { // 无限制线程池
                mThreadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
                startPoolTask(); // 开始执行线程池处理
            } else if (arg2 == 4) { // 自定义线程池
                mThreadPool = new ThreadPoolExecutor(
                        2, 5, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(19));
                startPoolTask(); // 开始执行线程池处理
            } else if (arg2 == 5) { // 主线程。注意耗时任务会堵塞主线程
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    for (int i = 0; i < 20; i++) {
                        // 创建一个新的消息分发任务
                        MessageRunnable task = new MessageRunnable(i);
                        getApplication().getMainExecutor().execute(task); // 命令主线程执行该任务
                    }
                } else {
                    Toast.makeText(ThreadExecutorActivity.this,
                            "主线程需要Android9或更高版本", Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    // 开始执行线程池处理
    private void startPoolTask() {
        for (int i = 0; i < 20; i++) {
            // 创建一个新的消息分发任务
            MessageRunnable task = new MessageRunnable(i);
            mThreadPool.execute(task); // 命令线程池执行该任务
        }
    }

    // 定义一个消息分发任务
    private class MessageRunnable implements Runnable {
        private int mIndex;
        public MessageRunnable(int index) {
            mIndex = index;
        }

        @Override
        public void run() {
            runOnUiThread(() -> {
                mDesc = String.format("%s\n%s 当前序号是%d", mDesc, DateUtil.getNowTime(), mIndex);
                tv_desc.setText(mDesc);
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}