package com.example.face.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class FaceUtil {
    private static final String TAG = "FaceUtil";

    // 比较两个位图的相似程度
    public static double matchCompare(Bitmap bitmap1, Bitmap bitmap2) {
        Mat mat1 = new Mat();
        Utils.bitmapToMat(bitmap1, mat1); // 把位图对象转为Mat结构
        Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_RGB2GRAY); // 全彩矩阵转灰度矩阵
        // 把矩阵的类型转换为Cv_32F，因为在C++代码中会判断类型
        mat1.convertTo(mat1, CvType.CV_32F);
        Mat mat2 = new Mat();
        Utils.bitmapToMat(bitmap2, mat2); // 把位图对象转为Mat结构
        Imgproc.cvtColor(mat2, mat2, Imgproc.COLOR_RGB2GRAY); // 全彩矩阵转灰度矩阵
        mat2.convertTo(mat2, CvType.CV_32F);
        // 通过直方图比较相似程度
        double similarity = Imgproc.compareHist(mat1, mat2, Imgproc.CV_COMP_CORREL);
        Log.d(TAG, "matchCompare: " + similarity);
        return similarity;
    }

    // 往位图添加多个文字
    public static Bitmap drawTextList(Context ctx, Bitmap origin, List<FaceText> textList) {
        int lineWidth = Math.max(origin.getWidth()/600 + 1, origin.getHeight()/600 + 1);
        float ratio = origin.getWidth()/600f + 1;
        int textSize = com.example.face.util.Utils.dip2px(ctx, 5*ratio);
        Canvas canvas = new Canvas(origin);
        Paint paint = new Paint(); // 创建一个画笔对象
        paint.setColor(Color.GREEN); // 设置画笔的颜色
        paint.setStrokeWidth(lineWidth); // 设置画笔的线宽
        paint.setTextSize(textSize); // 设置画笔的文字大小
        for (FaceText faceText : textList) {
            float textHeight = MeasureUtil.getTextHeight(faceText.text, textSize);
            // 在画布的指定位置描绘文字
            canvas.drawText(faceText.text, faceText.pos.x*origin.getWidth(),
                    faceText.pos.y*origin.getHeight()-textHeight/2, paint);
        }
        return origin;
    }

    public static class FaceText {
        public PointF pos; // 文字位置
        public String text; // 文字内容

        public FaceText(PointF pos, String text) {
            this.pos = pos;
            this.text = text;
        }
    }

}