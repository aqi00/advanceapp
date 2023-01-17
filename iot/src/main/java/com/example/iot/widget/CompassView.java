package com.example.iot.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.iot.R;
import com.example.iot.util.Utils;

public class CompassView extends View {
    private final static String TAG = "CompassView";
    private int mWidth; // 视图宽度
    private Paint mPaintLine; // 弧线的画笔
    private Paint mPaintText; // 文字的画笔
    private Paint mPaintAngle; // 刻度的画笔
    private Bitmap mCompassBg; // 背景罗盘的位图
    private Rect mRectSrc; // 位图的原始边界
    private Rect mRectDest; // 位图的目标边界
    private RectF mRectAngle; // 刻度的矩形边界
    private int mScaleLength = 25; // 刻度线的长度
    private float mBorder = 0.9f; // 边界的倍率，比如只在整个区域的90%内部绘图

    private RectF mRectSourth; // 指南针的矩形边界
    private int mDirection = -1024; // 指南针的方向
    private Paint mPaintSourth; // 指南针的画笔

    public CompassView(Context context) {
        this(context, null);
    }

    public CompassView(Context context, AttributeSet attr) {
        super(context, attr);
        // 以下初始化弧线的画笔
        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true); // 设置抗锯齿
        mPaintLine.setColor(Color.GREEN); // 设置画笔的颜色
        mPaintLine.setStrokeWidth(2); // 设置画笔的线宽
        mPaintLine.setStyle(Style.STROKE); // 设置画笔的类型。STROKE表示空心，FILL表示实心
        // 以下初始化文字的画笔
        mPaintText = new Paint();
        mPaintText.setAntiAlias(true); // 设置抗锯齿
        mPaintText.setColor(Color.RED); // 设置画笔的颜色
        mPaintText.setStrokeWidth(1); // 设置画笔的线宽
        mPaintText.setTextSize(Utils.dip2px(context, 14));
        // 以下初始化刻度的画笔
        mPaintAngle = new Paint();
        mPaintAngle.setAntiAlias(true); // 设置抗锯齿
        mPaintAngle.setColor(Color.BLACK); // 设置画笔的颜色
        mPaintAngle.setStrokeWidth(1); // 设置画笔的线宽
        mPaintAngle.setTextSize(Utils.dip2px(context, 12));
        // 以下初始化指南针的画笔
        mPaintSourth = new Paint();
        mPaintSourth.setAntiAlias(true); // 设置抗锯齿
        mPaintSourth.setColor(Color.RED); // 设置画笔的颜色
        mPaintSourth.setStrokeWidth(4); // 设置画笔的线宽
        mPaintSourth.setStyle(Style.STROKE); // 设置画笔的类型。STROKE表示空心，FILL表示实心
        // 从资源图片中获取罗盘背景的位图
        mCompassBg = BitmapFactory.decodeResource(getResources(), R.drawable.compass_bg);
        // 根据位图的宽高创建位图的原始边界
        mRectSrc = new Rect(0, 0, mCompassBg.getWidth(), mCompassBg.getHeight());
    }

    // 重写onMeasure方法，使得该视图无论竖屏还是横屏都保持正方形状
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        mWidth = getMeasuredWidth(); // 获取视图的实际宽度
        if (width < height) { // 宽度比高度小，则缩短高度使之与宽度一样长
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        } else { // 宽度比高度大，则缩短宽度使之与高度一样长
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        }
        // 根据视图的宽高创建位图的目标边界
        mRectDest = new Rect(0, 0, mWidth, mWidth);
        // 创建刻度的矩形边界
        mRectAngle = new RectF(mWidth / 10, mWidth / 10, mWidth * 9 / 10, mWidth * 9 / 10);
        // 创建指南针的矩形边界
        mRectSourth = new RectF(mWidth * 0.3f / 10, mWidth * 0.3f / 10, mWidth * 9.7f / 10, mWidth * 9.7f / 10);
        Log.d(TAG, "mWidth=" + mWidth);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        int radius = mWidth / 2;
        int margin = radius / 10;
        // 在画布上绘制罗盘背景
        canvas.drawBitmap(mCompassBg, mRectSrc, mRectDest, new Paint());
        // 以下在画布上绘制各种半径的圆圈
        canvas.drawCircle(radius, radius, radius * 3 / 10, mPaintLine);
        canvas.drawCircle(radius, radius, radius * 5 / 10, mPaintLine);
        canvas.drawCircle(radius, radius, radius * 7 / 10, mPaintLine);
        canvas.drawCircle(radius, radius, radius * 9 / 10, mPaintLine);
        // 在画布上绘制罗盘的中央垂直线
        canvas.drawLine(radius, margin, radius, mWidth - margin, mPaintLine);
        // 在画布上绘制罗盘的中央水平线
        canvas.drawLine(margin, radius, mWidth - margin, radius, mPaintLine);
        // 画罗盘的刻度
        for (int i = 0; i < 360; i += 30) {
            Path path = new Path(); // 创建一个路径对象
            path.addArc(mRectAngle, i - 3, i + 3); // 往路径添加圆弧
            int angle = (i + 90) % 360;
            // 在画布上绘制刻度文字
            canvas.drawTextOnPath("" + angle, path, 0, 0, mPaintAngle);
            // 在画布上绘制刻度线条
            canvas.drawLine(getXpos(radius, angle, radius * mBorder),
                    getYpos(radius, angle, radius * mBorder),
                    getXpos(radius, angle, (radius - mScaleLength) * mBorder),
                    getYpos(radius, angle, (radius - mScaleLength) * mBorder),
                    mPaintAngle);
        }
        // 画指南针
        if (mDirection > -1024) {
            int angle = (-mDirection + 450) % 360;
            // 在画布上绘制组成指南针的四个线条，包括一个三角形加上一根杆
            canvas.drawLine(getXpos(radius, angle, radius * mBorder),
                    getYpos(radius, angle, radius * mBorder),
                    getXpos(radius, angle, 0),
                    getYpos(radius, angle, 0),
                    mPaintSourth);
            canvas.drawLine(getXpos(radius, angle, radius * mBorder),
                    getYpos(radius, angle, radius * mBorder),
                    getXpos(radius, angle - 10, radius * 7 / 10),
                    getYpos(radius, angle - 10, radius * 7 / 10),
                    mPaintSourth);
            canvas.drawLine(getXpos(radius, angle, radius * mBorder),
                    getYpos(radius, angle, radius * mBorder),
                    getXpos(radius, angle + 10, radius * 7 / 10),
                    getYpos(radius, angle + 10, radius * 7 / 10),
                    mPaintSourth);
            canvas.drawLine(getXpos(radius, angle - 10, radius * 7 / 10),
                    getYpos(radius, angle - 10, radius * 7 / 10),
                    getXpos(radius, angle + 10, radius * 7 / 10),
                    getYpos(radius, angle + 10, radius * 7 / 10),
                    mPaintSourth);
            Path path = new Path(); // 创建一个路径对象
            path.addArc(mRectSourth, angle - 2, angle + 2); // 往路径添加圆弧
            // 在画布上绘制指南的“南”
            canvas.drawTextOnPath("南", path, 0, 0, mPaintText);
        } else {
            // 在画布上绘制指北的“北”
            canvas.drawText("北", radius - 15, margin - 15, mPaintText);
        }
    }

    // 根据半径、角度、线长，计算该点的横坐标
    private float getXpos(int radius, int angle, double length) {
        return (float) (radius + getCos(angle) * length);
    }

    // 根据半径、角度、线长，计算该点的纵坐标
    private float getYpos(int radius, int angle, double length) {
        return (float) (radius + getSin(angle) * length);
    }

    // 获得指定角度的正弦值
    private double getSin(int angle) {
        return Math.sin(Math.PI * angle / 180.0);
    }

    // 获得指定角度的余弦值
    private double getCos(int angle) {
        return Math.cos(Math.PI * angle / 180.0);
    }

    // 设置正南方的方向，用于指南针
    public void setDirection(int direction) {
        mDirection = direction;
        postInvalidate(); // 立即刷新视图（线程安全方式）
    }

}
