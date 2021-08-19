package com.example.face.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class FaceView extends ImageView {
    public static final String TAG = "FaceView";
    private int mMaxCount = 15; // 最大检测的人脸数量
    private Paint mPaint = new Paint(); // 创建人脸像框的画笔
    private Bitmap mBitmap; // 待处理的位图对象
    private FaceDetector.Face[] mFaceArray; // 存放检测结果的人脸数组
    private int mFaceCount = 0; // 检测到的人脸数量
    private int mWidth, mHeight; // 视图宽度、视图高度

    public FaceView(Context context) {
        this(context, null);
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint.setColor(Color.GREEN); // 设置画笔的颜色
        mPaint.setStyle(Paint.Style.STROKE); // 设置画笔的类型。STROK表示空心，FILL表示实心
        mPaint.setStrokeWidth(3); // 设置画笔的线宽
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth(); // 获取视图的实际宽度
        mHeight = getMeasuredHeight(); // 获取视图的实际高度
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        Log.d(TAG, "bitmap.getWidth="+bitmap.getWidth()+", bitmap.getHeight="+bitmap.getHeight());
        mBitmap = bitmap; // 注意这个位图对象必须是RGB_565格式
        mFaceArray = new FaceDetector.Face[mMaxCount];
        FaceDetector detector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), mMaxCount);
        mFaceCount = detector.findFaces(mBitmap, mFaceArray); // 在指定位图中寻找人脸
        Log.d(TAG, "mFaceCount is " + mFaceCount);
        postInvalidate(); // 立即刷新视图（线程安全方式）
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap == null) {
            return;
        }
        float ratio = Math.min(1.0f*mWidth/mBitmap.getWidth(), 1.0f*mHeight/mBitmap.getHeight());
        Rect rect = new Rect(0, 0, (int) (mBitmap.getWidth()*ratio), (int) (mBitmap.getHeight()*ratio));
        canvas.drawBitmap(mBitmap, null, rect, new Paint()); // 描绘原图像
        for (int i = 0; i < mFaceCount; i++) { // 在每个人脸周围描绘相框
            FaceDetector.Face face = mFaceArray[i];
            PointF point = new PointF();
            face.getMidPoint(point); // 获取人脸的中心点
            float distance = face.eyesDistance(); // 获取人脸中心点和眼间距离
            int faceRadius = (int) (distance*1.5); // 计算人脸的半径
            // 描绘人脸像框
            canvas.drawRect(ratio * (point.x - faceRadius), ratio * (point.y - faceRadius),
                    ratio * (point.x + faceRadius), ratio * (point.y + faceRadius*1.2f), mPaint);
        }
    }
}
