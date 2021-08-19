package com.example.picture.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;

public class OvalDrawable extends BitmapDrawable {
    private Paint mPaint = new Paint(); // 声明一个画笔对象

    public OvalDrawable(Context ctx, Bitmap bitmap) {
        super(ctx.getResources(), bitmap);
        // 创建一个位图着色器，CLAMP表示边缘拉伸
        BitmapShader shader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
        mPaint.setShader(shader); // 设置画笔的着色器对象
    }

    @Override
    public void draw(Canvas canvas) {
        // 在画布上绘制椭圆，也就是只显示椭圆内部的图像
        canvas.drawOval(0, 0, getBitmap().getWidth(), getBitmap().getHeight(), mPaint);
    }

}
