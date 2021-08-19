package com.example.location.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BitmapUtil {
    private final static String TAG = "BitmapUtil";

    // 把位图数据保存到指定路径的图片文件
    public static void saveImage(String path, Bitmap bitmap) {
        // 根据指定的文件路径构建文件输出流对象
        try (FileOutputStream fos = new FileOutputStream(path)) {
            // 把位图数据压缩到文件输出流中
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 把位图数据保存到指定路径的图片文件
    public static void saveImage(String path, ByteBuffer buffer) {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            fos.write(data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获得旋转角度之后的位图对象
    public static Bitmap getRotateBitmap(Bitmap bitmap, float rotateDegree) {
        Matrix matrix = new Matrix(); // 创建操作图片用的矩阵对象
        matrix.postRotate(rotateDegree); // 执行图片的旋转动作
        // 创建并返回旋转后的位图对象
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, false);
    }

    // 获得比例缩放之后的位图对象
    public static Bitmap getScaleBitmap(Bitmap bitmap, double scaleRatio) {
        Matrix matrix = new Matrix(); // 创建操作图片用的矩阵对象
        matrix.postScale((float)scaleRatio, (float)scaleRatio);
        // 创建并返回缩放后的位图对象
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, false);
    }

    // 获得自动缩小后的位图对象
    public static Bitmap getAutoZoomImage(Context ctx, Uri uri) {
        Log.d(TAG, "getAutoZoomImage uri="+uri.toString());
        Bitmap zoomBitmap = null;
        // 打开指定uri获得输入流对象
        try (InputStream is = ctx.getContentResolver().openInputStream(uri)) {
            // 从输入流解码得到原始的位图对象
            Bitmap originBitmap = BitmapFactory.decodeStream(is);
            int ratio = originBitmap.getWidth()/2000+1;
            // 获得比例缩放之后的位图对象
            zoomBitmap = getScaleBitmap(originBitmap, 1.0/ratio);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zoomBitmap;
    }

    // 获得自动缩小后的位图对象
    public static Bitmap getAutoZoomImage(Context ctx, Uri uri, int fenmu) {
        Log.d(TAG, "getAutoZoomImage uri="+uri.toString());
        Bitmap zoomBitmap = null;
        // 打开指定uri获得输入流对象
        try (InputStream is = ctx.getContentResolver().openInputStream(uri)) {
            // 从输入流解码得到原始的位图对象
            Bitmap originBitmap = BitmapFactory.decodeStream(is);
            int ratio = originBitmap.getWidth()/fenmu+1;
            // 获得比例缩放之后的位图对象
            zoomBitmap = getScaleBitmap(originBitmap, 1.0/ratio);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zoomBitmap;
    }

    // 获得自动缩小后的位图对象
    public static Bitmap getAutoZoomImage(Bitmap origin) {
        int ratio = origin.getWidth()/2000+1;
        // 获得并返回比例缩放之后的位图对象
        return getScaleBitmap(origin, 1.0/ratio);
    }

}
