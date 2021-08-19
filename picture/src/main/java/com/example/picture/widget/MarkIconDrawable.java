package com.example.picture.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;

import com.example.picture.util.BitmapUtil;

public class MarkIconDrawable extends BitmapDrawable {
    private Paint mPaint = new Paint(); // 声明一个画笔对象
    private Bitmap mMarker; // 水印图标
    private int mDirection; // 水印方位

    public MarkIconDrawable(Context ctx, Bitmap bitmap) {
        super(ctx.getResources(), bitmap);
        mPaint.setColor(0x99ffffff); // 设置画笔颜色
    }

    // 设置水印图标及其方位
    public void setMarkerIcon(Bitmap bitmap, int direction) {
        int originHeight = getBitmap().getHeight();
        int markerHeight = bitmap.getHeight();
        double ratio = 1.0*originHeight/markerHeight/3;
        // 创建缩放后的水印图标
        mMarker = BitmapUtil.getScaleBitmap(bitmap, ratio);
        mDirection = direction;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mMarker == null) {
            return;
        }
        int widthGap = getBitmap().getWidth() - mMarker.getWidth();
        int heightGap = getBitmap().getHeight() - mMarker.getHeight();
        if (mDirection == 0) { // 在中间
            canvas.drawBitmap(mMarker, widthGap/2, heightGap/2, mPaint);
        } else if (mDirection == 1) { // 左上角
            canvas.drawBitmap(mMarker, 0, 0, mPaint);
        } else if (mDirection == 2) { // 右上角
            canvas.drawBitmap(mMarker, widthGap, 0, mPaint);
        } else if (mDirection == 3) { // 左下角
            canvas.drawBitmap(mMarker, 0, heightGap, mPaint);
        } else if (mDirection == 4) { // 右下角
            canvas.drawBitmap(mMarker, widthGap, heightGap, mPaint);
        }
    }

}
