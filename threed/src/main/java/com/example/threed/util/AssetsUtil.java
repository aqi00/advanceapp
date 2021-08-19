package com.example.threed.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetsUtil {
    private final static String TAG = "AssetsUtil";

    // 从asset资产文件中获取文本字符串
    public static String getTxtFromAssets(Context context, String fileName) {
        String result = "";
        // 打开资产文件并获得输入流
        try (InputStream is = context.getAssets().open(fileName)) {
            int lenght = is.available();
            byte[] buffer = new byte[lenght];
            is.read(buffer);
            result = new String(buffer, "utf8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // 从asset资产文件中获取位图对象
    public static Bitmap getImgFromAssets(Context context, String fileName) {
        Bitmap bitmap = null;
        // 打开资产文件并获得输入流
        try (InputStream is = context.getAssets().open(fileName)) {
            // 解析输入流得到位图数据
            bitmap = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    // 把asset资产文件复制到存储卡（若文件已存在就不再复制）
    public static void Assets2Sd(Context context, String assetFile, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            copyAssetToStorage(context, assetFile, filePath);
        }
    }

    // 把asset资产文件复制到存储卡
    public static void copyAssetToStorage(Context context, String fileAssetPath, String filePath) {
        File dir = new File(filePath.substring(0, filePath.lastIndexOf("/")));
        if (!dir.exists()) {
            dir.mkdir();
        }
        try (InputStream is = context.getAssets().open(fileAssetPath);
             OutputStream os = new FileOutputStream(filePath);) {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ( (len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
