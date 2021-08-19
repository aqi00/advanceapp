package com.example.picture.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.example.picture.util.MeasureUtil;
import com.example.picture.util.Utils;

@SuppressLint("DrawAllocation")
public class DecorateImageView extends ImageView {
    private final static String TAG = "DecorateImageView";
    private Paint mPaint = new Paint(); // 声明一个画笔对象
    private int mWidth, mHeight; // 视图宽度、视图高度
    private int mTextSize = 30; // 文字大小
    private String mText; // 时间戳文本
    private Bitmap mLogo; // 标志图标
    private Bitmap mFrame; // 照片相框

    public DecorateImageView(Context context) {
        this(context, null);
    }

    public DecorateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint.setColor(0xff00ffff); // 设置画笔颜色
        mPaint.setTextSize(Utils.dip2px(context, mTextSize)); // 设置文字大小
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
        if (mFrame != null) { // 装饰相框非空
            canvas.drawBitmap(mFrame, null, new Rect(0,0,mWidth,mHeight), mPaint);
        }
        if (!TextUtils.isEmpty(mText)) { // 装饰文本非空
            // 获取指定文本的高度
            int textHeight = (int) MeasureUtil.getTextHeight(mText, mTextSize);
            // 在画布上绘制文本
            canvas.drawText(mText, 0, mHeight - textHeight, mPaint);
        }
        if (mLogo != null) { // 装饰标志非空
            canvas.drawBitmap(mLogo, mWidth-mLogo.getWidth(), mHeight-mLogo.getHeight(), mPaint);
        }
    }

    // 不显示任何装饰
    public void showNone() {
        mText = "";
        mLogo = null;
        mFrame = null;
        postInvalidate(); // 立即刷新视图（线程安全方式）
    }

    // 显示装饰文本
    public void showText(String text, boolean isReset) {
        if (isReset) {
            showNone(); // 不显示任何装饰
        }
        mText = text;
        postInvalidate(); // 立即刷新视图（线程安全方式）
    }

    // 显示装饰标志
    public void showLogo(Bitmap bitmap, boolean isReset) {
        if (isReset) {
            showNone(); // 不显示任何装饰
        }
        mLogo = bitmap;
        postInvalidate(); // 立即刷新视图（线程安全方式）
    }

    // 显示装饰相框
    public void showFrame(Bitmap bitmap, boolean isReset) {
        if (isReset) {
            showNone(); // 不显示任何装饰
        }
        mFrame = bitmap;
        postInvalidate(); // 立即刷新视图（线程安全方式）
    }

    // 设置文字字体
    public void setTypeface(Typeface typeface) {
        mPaint.setTypeface(typeface); // 设置文字字体
    }

}
