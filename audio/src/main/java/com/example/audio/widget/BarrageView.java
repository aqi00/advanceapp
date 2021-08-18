package com.example.audio.widget;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.audio.util.MD5Util;
import com.example.audio.util.MeasureUtil;
import com.example.audio.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BarrageView extends LinearLayout {
    private final static String TAG = "BarrageView";
    private Context mContext; // 声明一个上下文对象
    private int mRowCount = 5; // 弹幕行数
    private int mTextSize = 15; // 文字大小
    private List<RelativeLayout> mLayoutList = new ArrayList<>(); // 每行的相对布局列表
    private int mWidth; // 视图宽度
    private int mLastPos1 = -1, mLastPos2 = -1; // 最近两次的弹幕位置
    private final int[] COLOR_ARRAY = {
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.CYAN, Color.MAGENTA, Color.LTGRAY, Color.GRAY,
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.CYAN, Color.MAGENTA, Color.LTGRAY, Color.GRAY,};

    public BarrageView(Context context) {
        this(context, null);
    }

    public BarrageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        intView(context); // 初始化视图
    }

    // 初始化视图
    private void intView(Context context) {
        mContext = context;
        setOrientation(LinearLayout.VERTICAL); // 设置垂直方向
        setBackgroundColor(Color.TRANSPARENT);
        for (int i=0; i<mRowCount; i++) {
            RelativeLayout layout = new RelativeLayout(mContext);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, Utils.dip2px(mContext, 40));
            layout.setLayoutParams(params);
            layout.setBackgroundColor(Color.TRANSPARENT);
            mLayoutList.add(layout);
            addView(layout); // 添加至当前视图
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth(); // 获取视图的实际宽度
    }

    // 获取本次弹幕的位置。不跟最近两次在同一行，避免挨得太近
    private int getPos() {
        int pos;
        do {
            pos = new Random().nextInt(mRowCount);
        } while (pos==mLastPos1 || pos==mLastPos2);
        mLastPos2 = mLastPos1;
        mLastPos1 = pos;
        return pos;
    }

    // 给弹幕视图添加评论
    public void addComment(String comment) {
        RelativeLayout layout = mLayoutList.get(getPos()); // 获取随机位置的相对布局
        TextView tv_comment = getCommentView(comment); // 获取评论文字的文本视图
        float textWidth = MeasureUtil.getTextWidth(comment, Utils.dip2px(mContext, mTextSize));
        layout.addView(tv_comment); // 添加至当前视图
        // 根据估值器和起止位置创建一个属性动画
        ValueAnimator anim = ValueAnimator.ofObject(new MarginEvaluator(), (int) -textWidth, mWidth);
        // 添加属性动画的刷新监听器
        anim.addUpdateListener(animation -> {
            int margin = (int) animation.getAnimatedValue(); // 获取动画的当前值
            RelativeLayout.LayoutParams tv_params = (RelativeLayout.LayoutParams) tv_comment.getLayoutParams();
            tv_params.rightMargin = margin;
            if (margin > mWidth-textWidth) { // 左滑到顶了
                tv_params.leftMargin = (int) (mWidth-textWidth - margin);
            }
            tv_comment.setLayoutParams(tv_params); // 设置文本视图的布局参数
        });
        anim.setTarget(tv_comment); // 设置动画的播放目标
        anim.setDuration(5000); // 设置动画的播放时长
        anim.setInterpolator(new LinearInterpolator()); // 设置属性动画的插值器
        anim.start(); // 属性动画开始播放
        // 添加属性动画的监听器
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                layout.removeView(tv_comment); // 从当前视图移除
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }

    // 获取评论内容的文本视图
    private TextView getCommentView(String content) {
        TextView tv = new TextView(mContext);
        tv.setText(content);
        tv.setTextSize(mTextSize);
        tv.setTextColor(getColorByContent(content));
        tv.setSingleLine(true);
        tv.setBackgroundColor(Color.TRANSPARENT);
        RelativeLayout.LayoutParams tv_params = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tv_params.addRule(RelativeLayout.CENTER_VERTICAL); // 垂直方向居中
        tv_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT); // 与上级布局右对齐
        tv.setLayoutParams(tv_params); // 设置文本视图的布局参数
        return tv;
    }


    // 根据昵称获取对应的头像
    private int getColorByContent(String content) {
        String md5 = MD5Util.encrypt(content);
        char lastChar = md5.charAt(md5.length()-1);
        int pos = lastChar>='A' ? lastChar-'A'+10 : lastChar-'0';
        return COLOR_ARRAY[pos];
    }

    // 定义一个间距估值器，计算动画播放期间的间距大小
    public static class MarginEvaluator implements TypeEvaluator<Integer> {
        @Override
        public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
            return (int) (startValue*(1-fraction) + endValue*fraction);
        }
    }

}
