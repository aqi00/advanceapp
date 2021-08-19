package com.example.face.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.face.util.BitmapUtil;

@SuppressLint("DrawAllocation")
public class BitmapView extends View {
    private static final String TAG = "BitmapView";
    private float mScaleRatio = 1.0f; // 缩放比例
    private float mRotateDegree = 0; // 旋转角度
    private Bitmap mBitmap; // 声明一个位图对象
    private int mBitmapWidth; // 位图宽度
    private int mBitmapHeight; // 位图高度
    private int mOffsetX = 0, mOffsetY = 0; // 横轴和纵轴上的偏移
    private int mLastOffsetX = 0, mLastOffsetY = 0; // 上一次在横轴和纵轴上的偏移

    public BitmapView(Context context) {
        this(context, null);
    }

    public BitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 设置位图对象
    public void setImageBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
        postInvalidate(); // 立即刷新视图（线程安全方式）
    }

    // 设置缩放比例。isReset为true表示按照原始尺寸进行缩放，为false表示按照当前尺寸进行缩放
    public void setScaleRatio(float ratio, boolean isReset) {
        if (isReset) {
            mScaleRatio = ratio;
        } else {
            mScaleRatio *= ratio;
        }
        postInvalidate(); // 立即刷新视图（线程安全方式）
    }

    // 设置旋转角度。isReset为true表示按照原始方向进行旋转，为false表示按照当前方向进行缩放
    public void setRotateDegree(int degree, boolean isReset) {
        if (isReset) {
            mRotateDegree = degree;
        } else {
            mRotateDegree += degree;
        }
        postInvalidate(); // 立即刷新视图（线程安全方式）
    }

    // 设置偏移距离。isReset为true表示按照原始位置进行移动，为false表示按照当前位置进行移动
    public void setOffset(int offsetX, int offsetY, boolean isReset) {
        if (isReset) {
            mLastOffsetX = mOffsetX;
            mLastOffsetY = mOffsetY;
        }
        mOffsetX = mLastOffsetX + offsetX;
        mOffsetY = mLastOffsetY + offsetY;
        postInvalidate(); // 立即刷新视图（线程安全方式）
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap == null) {
            return;
        }
        int width = getMeasuredWidth(); // 获取视图的实际宽度
        int height = getMeasuredHeight(); // 获取视图的实际高度
        int new_width = (int) (mBitmapWidth * mScaleRatio);
        int new_height = (int) (mBitmapHeight * mScaleRatio);
        // 生成缩放后的位图对象
        Bitmap scaledBitmap = BitmapUtil.getScaleBitmap(mBitmap, mScaleRatio);
        // 生成旋转后的位图对象
        Bitmap rotatedBitmap = BitmapUtil.getRotateBitmap(scaledBitmap, mRotateDegree);
        // 在画布上的指定位置绘制位图对象
        canvas.drawBitmap(rotatedBitmap, (width - new_width) / 2 + mOffsetX,
                (height - new_height) / 2 + mOffsetY, new Paint());
    }

}
