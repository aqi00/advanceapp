package com.example.video.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.example.video.MainApplication;
import com.example.video.R;
import com.example.video.widget.FloatWindow;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@SuppressLint("HandlerLeak")
public class StockService extends Service {
    private final static String TAG = "StockService";
    private FloatWindow mFloatWindow; // 声明一个悬浮窗对象
    private TextView tv_sh_stock, tv_sz_stock; // 声明一个文本视图对象
    public static int OPEN = 0; // 打开悬浮窗
    public static int CLOSE = 1; // 关闭悬浮窗
    private static int SHANGHAI = 0; // 上证综指
    private static int SHENZHEN = 1; // 深圳成指
    private final int delayTime = 5000; // 刷新的间隔时间

    // 创建一个处理器对象
    private Handler mHandler = new Handler(Looper.myLooper()) {
        // 在收到消息时触发
        public void handleMessage(Message msg) {
            // 上证指数,3019.9873,-5.6932,-0.19,1348069,14969598
            String desc = (String) msg.obj;
            String[] array = desc.split(",");
            String stock = array[1]; // 当前指数
            float distance = Float.parseFloat(array[2]); // 与上一交易日的指数差额
            String range = array[3]; // 涨跌百分比
            String text = String.format("%s  %s%%", stock, range);
            int type = msg.what;
            int color = distance>0 ? Color.RED : Color.GREEN; // 股指上涨，标红；股指下跌，标绿
            if (type == SHANGHAI) { // 上证综指
                tv_sh_stock.setText(text);
                tv_sh_stock.setTextColor(color);
            } else if (type == SHENZHEN) { // 深圳成指
                tv_sz_stock.setText(text);
                tv_sz_stock.setTextColor(color);
            }
        }
    };

    // 定义一个股指刷新任务
    private Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            if (mFloatWindow != null && mFloatWindow.isShow()) {
                getStockIndex(SHANGHAI); // 获取上证综指
                getStockIndex(SHENZHEN); // 获取深圳成指
            }
            mHandler.postDelayed(this, delayTime); // 延迟若干秒后再次启动股指刷新任务
        }
    };

    // 获取股市证券指数
    private void getStockIndex(int type) {
        String url = null;
        if (type == SHANGHAI) { // 上证综指
            url = "https://hq.sinajs.cn/list=s_sh000001";
        } else if (type == SHENZHEN) { // 深圳成指
            url = "https://hq.sinajs.cn/list=s_sz399001";
        }
        OkHttpClient client = new OkHttpClient(); // 创建一个okhttp客户端对象
        // 创建一个GET方式的请求结构
        Request request = new Request.Builder()
                .header("Accept-Language", "zh-CN") // 给http请求添加头部信息
                .url(url) // 指定http请求的调用地址
                .build();
        Call call = client.newCall(request); // 根据请求结构创建调用对象
        // 加入HTTP请求队列。异步调用，并设置接口应答的回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { // 请求失败
                Log.d(TAG, "调用股指接口报错："+e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException { // 请求成功
                String resp = response.body().string();
                Message msg = Message.obtain(); // 获得一个默认的消息对象
                msg.what = type; // 消息类型
                msg.obj = resp.substring(resp.indexOf("\"") + 1, resp.lastIndexOf("\"")); // 消息描述
                mHandler.sendMessage(msg); // 向处理器发送消息
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mFloatWindow == null) {
            // 创建一个新的悬浮窗
            mFloatWindow = new FloatWindow(MainApplication.getInstance());
            // 设置悬浮窗的布局内容
            mFloatWindow.setLayout(R.layout.float_stock);
            // 从布局文件中获取展示上证综指的文本视图
            tv_sh_stock = mFloatWindow.mContentView.findViewById(R.id.tv_sh_stock);
            // 从布局文件中获取展示深圳成指的文本视图
            tv_sz_stock = mFloatWindow.mContentView.findViewById(R.id.tv_sz_stock);
        }
        mHandler.post(mRefresh); // 立即启动股指刷新任务
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int type = intent.getIntExtra("type", OPEN); // 从意图中解包获得操作类型
            if (type == OPEN) { // 打开
                if (mFloatWindow != null && !mFloatWindow.isShow()) {
                    tv_sh_stock.setText("正在努力加载股指信息");
                    mFloatWindow.show(Gravity.LEFT | Gravity.TOP); // 显示悬浮窗
                }
            } else if (type == CLOSE) { // 关闭
                if (mFloatWindow != null && mFloatWindow.isShow()) {
                    mFloatWindow.close(); // 关闭悬浮窗
                }
                stopSelf(); // 停止自身服务
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRefresh); // 移除股指刷新任务
    }

}
