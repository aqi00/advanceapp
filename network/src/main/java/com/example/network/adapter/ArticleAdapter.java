package com.example.network.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.network.R;
import com.example.network.bean.ArticleInfo;

import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = "ArticleAdapter";
    private Context mContext; // 声明一个上下文对象
    private List<ArticleInfo> mArticleList; // 文章列表
    private final int[] COLOR_ARRAY = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA};

    public ArticleAdapter(Context context, List<ArticleInfo> articleList) {
        mContext = context;
        mArticleList = articleList;
    }

    // 获取列表项的个数
    @Override
    public int getItemCount() {
        return mArticleList.size();
    }

    // 创建列表项的视图持有者
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup vg, int viewType) {
        // 根据布局文件item_article.xml生成视图对象
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_article, vg, false);
        return new ItemHolder(v);
    }

    // 绑定列表项的视图持有者
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, final int position) {
        Log.d(TAG, "position="+position);
        ItemHolder holder = (ItemHolder) vh;
        ArticleInfo article = mArticleList.get(position);
        holder.tv_seq.setText(""+(position+1));
        holder.tv_title.setText(article.getTitle());
        holder.tv_title.setTextColor(COLOR_ARRAY[position%6]);
    }

    // 获取列表项的类型
    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    // 获取列表项的编号
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 定义列表项的视图持有者
    public class ItemHolder extends RecyclerView.ViewHolder {
        public TextView tv_seq; // 声明一个文本视图对象
        public TextView tv_title; // 声明一个文本视图对象

        public ItemHolder(View v) {
            super(v);
            tv_seq = v.findViewById(R.id.tv_seq);
            tv_title = v.findViewById(R.id.tv_title);
        }
    }

}
