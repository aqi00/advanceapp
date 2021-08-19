package com.example.picture.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;

import com.example.picture.util.MeasureUtil;
import com.example.picture.util.Utils;

public class MarkTextDrawable extends BitmapDrawable {
    private Context mContext; // 声明一个上下文对象
    private Paint mPaint = new Paint(); // 声明一个画笔对象
    private String mText; // 水印文字
    private int mTextSize = 40; // 文字大小

    public MarkTextDrawable(Context ctx, Bitmap bitmap) {
        super(ctx.getResources(), bitmap);
        mContext = ctx;
    }

    // 设置水印文字及其字体
    public void setMarkerText(String text, Typeface typeface) {
        mText = text;
        if (typeface != null) {
            mPaint.setColor(0xffffffff); // 设置画笔颜色
            mPaint.setTextSize(Utils.dip2px(mContext, mTextSize)); // 设置文字大小
            mPaint.setTypeface(typeface); // 设置文字字体
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mText == null) {
            return;
        }
        int bitmapWidth = getBitmap().getWidth();
        int bitmapHeight = getBitmap().getHeight();
        // 获取指定文本的宽度（其实就是长度）
        int textWidth = (int) MeasureUtil.getTextWidth(mText, mTextSize);
        // 获取指定文本的高度
        int textHeight = (int) MeasureUtil.getTextHeight(mText, mTextSize);
        // 在画布上绘制文本
        canvas.drawText(mText, bitmapWidth/2 - textWidth, bitmapHeight - textHeight, mPaint);
    }

}
