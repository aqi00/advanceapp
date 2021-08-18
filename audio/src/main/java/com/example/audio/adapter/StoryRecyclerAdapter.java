package com.example.audio.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.audio.R;
import com.example.audio.bean.AudioInfo;
import com.example.audio.constant.UrlConstant;
import com.example.audio.util.Utils;

import java.util.List;

public class StoryRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = "StoryRecyclerAdapter";
    private Context mContext; // 声明一个上下文对象
    private List<AudioInfo> mAudioList; // 音频信息列表
    private OnItemClickListener mListener; // 点击监听器

    public StoryRecyclerAdapter(Context context, List<AudioInfo> pathList, OnItemClickListener listener) {
        mContext = context;
        mAudioList = pathList;
        mListener = listener;
    }

    // 获取列表项的个数
    @Override
    public int getItemCount() {
        return mAudioList.size();
    }

    // 创建列表项的视图持有者
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup vg, int viewType) {
        // 根据布局文件item_cover.xml生成视图对象
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_audio, vg, false);
        return new ItemHolder(v);
    }

    // 绑定列表项的视图持有者
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, final int position) {
        ItemHolder holder = (ItemHolder) vh;
        AudioInfo audio = mAudioList.get(position);
        // 使用Glide加载圆角矩形裁剪后的故事封面
        RoundedCorners roundedCorners = new RoundedCorners(Utils.dip2px(mContext, 10));
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners);
        Glide.with(mContext).load(UrlConstant.HTTP_PREFIX+audio.getCover()).apply(options).into(holder.iv_cover);
        holder.tv_title.setText(audio.getTitle());
        if (mListener != null) {
            holder.ll_story.setOnClickListener(v -> mListener.onItemClick(position));
        }
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
        public LinearLayout ll_story; // 声明一个线性布局对象
        public ImageView iv_cover; // 声明一个图像视图对象
        public TextView tv_title; // 声明一个文本视图对象

        public ItemHolder(View v) {
            super(v);
            ll_story = v.findViewById(R.id.ll_story);
            iv_cover = v.findViewById(R.id.iv_cover);
            tv_title = v.findViewById(R.id.tv_title);
        }
    }

    // 定义一个循环视图列表项的点击监听器接口
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
