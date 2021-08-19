package com.example.picture.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.example.picture.util.Utils;

public class RippleImageView extends ImageView {
    private Context mContext; // 声明一个上下文对象
    private Paint mPaint = new Paint(); // 声明一个画笔对象
    private int mWidth, mHeight; // 视图宽度、视图高度
    private int mRadius; // 水波的半径
    private int mIncrease; // 半径的增量
    private Handler mHandler = new Handler(Looper.myLooper()); // 声明一个处理器对象

    public RippleImageView(Context context) {
        this(context, null);
    }

    public RippleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mIncrease = Utils.dip2px(mContext, 5);
        mPaint.setColor(0x99ffffff); // 设置画笔颜色
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth(); // 获取视图的实际宽度
        mHeight = getMeasuredHeight(); // 获取视图的实际高度
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mRadius > 0) {
            canvas.drawCircle(mWidth/2, mHeight/2, mRadius, mPaint); // 画水波
        }
    }

    // 设置水波的颜色
    public void setRippleColor(int color) {
        mPaint.setColor(color); // 设置画笔颜色
    }

    // 开始播放水波动画
    public void startRipple() {
        mRadius = 0;
        mHandler.post(mRipple); // 立即启动水波刷新任务
    }

    // 定义一个水波刷新任务
    private Runnable mRipple = new Runnable() {
        @Override
        public void run() {
            mRadius += mIncrease;
            if (mRadius*mRadius > (mWidth*mWidth/4 + mHeight*mHeight/4)) { // 水波半径已超出对角线
                mRadius = 0;
                mIncrease = Utils.dip2px(mContext, 5);
            } else { // 水波半径未超出对角线
                mIncrease += Utils.dip2px(mContext, 1);
                mHandler.postDelayed(this, 50); // 延迟50毫秒后再次启动水波刷新任务
            }
            postInvalidate(); // 立即刷新视图（线程安全方式）
        }
    };

}
