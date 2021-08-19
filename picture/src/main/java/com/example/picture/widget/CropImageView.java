package com.example.picture.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.example.picture.util.BitmapUtil;

@SuppressLint("ClickableViewAccessibility")
public class CropImageView extends View {
    private Paint mPaintShade; // 声明一个阴影画笔对象
    private Bitmap mOrigBitmap = null; // 声明一个原始的位图对象
    private Bitmap mCropBitmap = null; // 声明一个裁剪后的位图对象
    private Rect mRect = new Rect(0, 0, 0, 0); // 矩形边界

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaintShade = new Paint(); // 创建一个画笔
        mPaintShade.setColor(0x99000000); // 设置画笔的颜色
    }

    // 设置原始的位图对象
    public void setOrigBitmap(Bitmap orig) {
        mOrigBitmap = orig;
    }

    // 获得裁剪后的位图对象
    public Bitmap getCropBitmap() {
        return mCropBitmap;
    }

    // 设置位图的矩形边界
    public boolean setBitmapRect(Rect rect) {
        if (mOrigBitmap == null) { // 原始位图为空
            return false;
        } else if (rect.left < 0 || rect.left > mOrigBitmap.getWidth()) { // 左侧边界非法
            return false;
        } else if (rect.top < 0 || rect.top > mOrigBitmap.getHeight()) { // 上方边界非法
            return false;
        } else if (rect.right <= 0 || rect.left + rect.right > mOrigBitmap.getWidth()) { // 右侧边界非法
            return false;
        } else if (rect.bottom <= 0 || rect.top + rect.bottom > mOrigBitmap.getHeight()) { // 下方边界非法
            return false;
        }
        mRect = rect;
        // 根据指定的四周边界，裁剪相应尺寸的位图对象
        mCropBitmap = Bitmap.createBitmap(mOrigBitmap,
                mRect.left, mRect.top, mRect.right, mRect.bottom);
        postInvalidate(); // 立即刷新视图（线程安全方式）
        return true;
    }

    // 获取位图的矩形边界
    public Rect getBitmapRect() {
        return mRect;
    }

    // 左右翻转图像
    public void flip() {
        // 水平翻转图像，也就是把镜中像左右翻过来
        mCropBitmap = BitmapUtil.getFlipBitmap(mCropBitmap);
        postInvalidate(); // 立即刷新视图（线程安全方式）
    }

    // 在下级视图都绘制完成后触发
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mOrigBitmap == null) {
            return;
        }
        // 画外圈阴影
        Rect rectShade = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
        canvas.drawRect(rectShade, mPaintShade);
        // 画高亮处的图像
        canvas.drawBitmap(mCropBitmap, mRect.left, mRect.top, new Paint());
    }

}
