package com.example.location.util;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.InputStream;

public class ExifUtil {
    private final static String TAG = "ExifUtil";

    // 从图片uri中获取位置信息
    public static String getLocationFromImage(Context ctx, Uri uri) {
        String path = uri.toString();
        String imageId;
        if (path.contains("%3A")) { // %3A为斜杆“/”的转义符
            imageId = path.substring(path.lastIndexOf("%3A")+3);
        } else {
            imageId = path.substring(path.lastIndexOf("/")+1);
        }
        return getLocationFromImage(ctx, imageId);
    }

    // 获取指定图片的位置信息。需要声明权限ACCESS_MEDIA_LOCATION，并在代码中动态授权
    public static String getLocationFromImage(Context ctx, String imageId) {
        Uri imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
        String location = "未获得经纬度信息";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            float[] lati_long = new float[2];
            Uri photoUri = MediaStore.setRequireOriginal(imageUri);
            Log.d(TAG, "imageUri="+imageUri+", photoUri="+photoUri);
            try (InputStream is = ctx.getContentResolver().openInputStream(photoUri)) {
                // 根据输入流对象构建相片信息接口
                ExifInterface exifInterface = new ExifInterface(is);
                // 从相片信息接口获取拍摄时候所处的经纬度
                boolean isSuccess = exifInterface.getLatLong(lati_long);
                if (isSuccess) {
                    // 从相片信息接口获取拍摄时候所处的海拔高度
                    double altitude = Math.abs(exifInterface.getAltitude(0.0));
                    // 从相片信息接口获取拍摄时间
                    String datetime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                    location = String.format("北纬%f\n东经%f\n海拔%.2f米\n拍摄时间为%s",
                            lati_long[0], lati_long[1], altitude, datetime);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return location;
    }

}
