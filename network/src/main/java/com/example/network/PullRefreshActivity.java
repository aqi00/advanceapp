package com.example.network;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;

import com.example.network.adapter.ArticleAdapter;
import com.example.network.bean.ArticleInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PullRefreshActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private final static String TAG = "PullRefreshActivity";
    private SwipeRefreshLayout srl_dynamic; // 声明一个下拉刷新布局对象
    private RecyclerView rv_dynamic; // 声明一个循环视图对象
    private LinearLayout ll_bottom; // 声明一个线性视图对象
    private ArticleAdapter mAdapter; // 声明一个线性适配器对象
    private List<ArticleInfo> mArticleList = new ArrayList<>(); // 文章列表
    private int mPageNo = 0; // 已加载的分页序号
    private int mLastVisibleItem = 0; // 最后一个可见项的序号
    private Handler mHandler = new Handler(Looper.myLooper()); // 声明一个处理器对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_refresh);
        srl_dynamic = findViewById(R.id.srl_dynamic);
        // 设置下拉刷新布局的进度圆圈颜色
        srl_dynamic.setColorSchemeResources(
                R.color.red, R.color.orange, R.color.green, R.color.blue);
        srl_dynamic.setOnRefreshListener(this); // 设置下拉布局的下拉刷新监听器
        initRecyclerDynamic(); // 初始化动态线性布局的循环视图
        ll_bottom = findViewById(R.id.ll_bottom);
        mHandler.post(() -> onRefresh()); // 刚打开页面时候的初始加载
    }

    // 初始化动态线性布局的循环视图
    private void initRecyclerDynamic() {
        rv_dynamic = findViewById(R.id.rv_dynamic);
        // 创建一个垂直方向的线性布局管理器
        LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rv_dynamic.setLayoutManager(manager); // 设置循环视图的布局管理器
        // 构建一个文章列表的线性适配器
        mAdapter = new ArticleAdapter(this, mArticleList);
        rv_dynamic.setAdapter(mAdapter);  // 设置循环视图的线性适配器
        rv_dynamic.setItemAnimator(new DefaultItemAnimator());  // 设置循环视图的动画效果
        // 给循环视图添加滚动监听器
        rv_dynamic.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { // 滚动停止
                    // 滚到了最后一项
                    if (mLastVisibleItem+1 == mAdapter.getItemCount()) {
                        // 显示底部的加载更多文字
                        ll_bottom.setVisibility(View.VISIBLE);
                        // 滚到最后一项
                        rv_dynamic.scrollToPosition(mArticleList.size()-1);
                        // 延迟500毫秒后加载更多文章
                        mHandler.postDelayed(() -> loadArticle(), 500);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 寻找最后一个可见项的序号
                mLastVisibleItem = manager.findLastVisibleItemPosition();
            }
        });
    }

    // 一旦在下拉刷新布局内部往下拉动页面，就触发下拉监听器的onRefresh方法
    @Override
    public void onRefresh() {
        mArticleList.clear();
        mPageNo = 0;
        loadArticle(); // 加载网络文章
    }

    // 加载网络文章
    private void loadArticle() {
        String url = String.format("https://www.wanandroid.com/article/list/%d/json", mPageNo);
        OkHttpClient client = new OkHttpClient(); // 创建一个okhttp客户端对象
        // 创建一个GET方式的请求结构
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request); // 根据请求结构创建调用对象
        // 加入HTTP请求队列。异步调用，并设置接口应答的回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { // 请求失败
                runOnUiThread(() -> srl_dynamic.setRefreshing(false));
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException { // 请求成功
                String resp = response.body().string();
                mPageNo++;
                runOnUiThread(() -> showArticle(resp)); // 显示返回的文章
            }
        });
    }

    // 显示返回的文章
    private void showArticle(String resp) {
        srl_dynamic.setRefreshing(false);
        int lastSize = mArticleList.size();
        List<ArticleInfo> addList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(resp);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray datas = data.getJSONArray("datas");
            for (int i=0; i<datas.length(); i++) {
                JSONObject item = datas.getJSONObject(i);
                ArticleInfo article = new ArticleInfo();
                article.setTitle(item.getString("title"));
                addList.add(article);
            }
            mArticleList.addAll(addList);
            if (lastSize == 0) { // 下拉刷新开头文章
                mAdapter.notifyDataSetChanged(); // 刷新所有列表项数据
            } else { // 上拉加载更多文章
                // 只刷新指定范围的列表项数据
                mAdapter.notifyItemRangeInserted(lastSize, addList.size());
            }
            ll_bottom.setVisibility(View.GONE); // 隐藏底部的加载更多文字
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}