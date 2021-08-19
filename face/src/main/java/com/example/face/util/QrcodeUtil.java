package com.example.face.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

public class QrcodeUtil {
    private final static String TAG = "QrcodeUtil";
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    // 获取相机尺寸
    public static Point getCameraSize(Camera.Parameters params, Point screenSize) {
        String previewSizeValueString = params.get("preview-size-values");
        if (previewSizeValueString == null) {
            previewSizeValueString = params.get("preview-size-value");
        }
        Point cameraSize = null;
        if (previewSizeValueString != null) {
            Log.d(TAG, "preview-size-values parameter: " + previewSizeValueString);
            cameraSize = findBestPreviewSizeValue(previewSizeValueString, screenSize);
        }
        if (cameraSize == null) {
            cameraSize = new Point((screenSize.x >> 3) << 3, (screenSize.y >> 3) << 3);
        }
        return cameraSize;
    }

    // 寻找最佳的预览尺寸
    private static Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenSize) {
        int bestX = 0, bestY = 0;
        int diff = Integer.MAX_VALUE;
        for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {
            previewSize = previewSize.trim();
            int dimPosition = previewSize.indexOf('x');
            if (dimPosition < 0) {
                Log.d(TAG, "Bad preview-size: " + previewSize);
                continue;
            }

            int newX, newY;
            try {
                newX = Integer.parseInt(previewSize.substring(0, dimPosition));
                newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
            } catch (NumberFormatException nfe) {
                Log.d(TAG, "Bad preview-size: " + previewSize);
                continue;
            }

            int newDiff = Math.abs((newX - screenSize.x) + (newY - screenSize.y));
            if (newDiff == 0) {
                bestX = newX;
                bestY = newY;
                break;
            } else if (newDiff < diff) {
                bestX = newX;
                bestY = newY;
                diff = newDiff;
            }
        }

        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return null;
    }

    // 获取更小尺寸的位图
    public static Bitmap getSmallerBitmap(Bitmap bitmap) {
        int size = bitmap.getWidth() * bitmap.getHeight() / 160000;
        if (size <= 1)
            return bitmap; // 如果小于
        else {
            Matrix matrix = new Matrix();
            matrix.postScale((float) (1 / Math.sqrt(size)), (float) (1 / Math.sqrt(size)));
            Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return resizeBitmap;
        }
    }

    // 从位图中解析二维码
    public static Result parserQrcode(Bitmap bitmap) {
        Bitmap smallBitmap = QrcodeUtil.getSmallerBitmap(bitmap);
        // 获取bitmap的宽高，像素矩阵
        int width = smallBitmap.getWidth();
        int height = smallBitmap.getHeight();
        int[] pixels = new int[width * height];
        smallBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        // 最新的库中，RGBLuminanceSource 的构造器参数不只是bitmap了
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();
        Result result = null;
        try {
            result = reader.decode(binaryBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // 对字符串重新编码
    public static String reEncode(String str) {
        String formarStr = "";
        try {
            boolean ISO = Charset.forName("ISO-8859-1").newEncoder().canEncode(str);
            if (ISO) {
                formarStr = new String(str.getBytes("ISO-8859-1"), "GB2312");
            } else {
                formarStr = str;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formarStr;
    }

}
