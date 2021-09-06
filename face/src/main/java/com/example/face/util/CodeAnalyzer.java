package com.example.face.util;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CodeAnalyzer {
    private final static String TAG = "CodeAnalyzer";

    // 从位图中切割四个验证码数字图像
    public static List<Bitmap> splitImage(Bitmap origin) {
        Log.d(TAG, "getWidth="+origin.getWidth()+", getHeight="+origin.getHeight());
        Bitmap bitmap = origin;
        bitmap = BitmapUtil.getScaleBitmap(bitmap, 300.0/bitmap.getWidth());
        List<Bitmap> bitmapList = new ArrayList<>();
        bitmapList.add(Bitmap.createBitmap(bitmap, 32, 13, 39, 55));
        bitmapList.add(Bitmap.createBitmap(bitmap, 95, 13, 39, 55));
        bitmapList.add(Bitmap.createBitmap(bitmap, 158, 13, 39, 55));
        bitmapList.add(Bitmap.createBitmap(bitmap, 221, 13, 39, 55));
        return bitmapList;
    }

    private static int getRed(String color) {
        return Integer.parseInt(color.substring(2, 4), 16);
    }

    private static int getGreen(String color) {
        return Integer.parseInt(color.substring(4, 6), 16);
    }

    private static int getBlue(String color) {
        return Integer.parseInt(color.substring(6, 8), 16);
    }

    // 获取暗色像素的数量
    private static int getBlackCount(Bitmap bitmap, int beginX, int beginY, int endX, int endY) {
        int blackCount = 0;
        for (int j = beginX; j <= endX; j++) {
            for (int k = beginY; k <= endY; k++) {
                String color = Integer.toHexString(bitmap.getPixel(j, k));
                int colorTotal = getRed(color) + getGreen(color) + getBlue(color);
                if (colorTotal <= 500) {
                    blackCount++;
                }
            }
        }
        return blackCount;
    }

    private static boolean blankLeftBottom(Bitmap bitmap) {
        int beginX = 0, beginY = 33, endX = 7, endY = 37;
        int blackCount = getBlackCount(bitmap, beginX, beginY, endX, endY);
        return blackCount < 15;
    }

    private static boolean fewTop(Bitmap bitmap) {
        int beginX = 5, beginY = 0, endX = 34, endY = 4;
        int blackCount = getBlackCount(bitmap, beginX, beginY, endX, endY);
        return blackCount < 50;
    }

    private static boolean blankLeftTop(Bitmap bitmap) {
        int beginX = 0, beginY = 18, endX = 12, endY = 22;
        int blackCount = getBlackCount(bitmap, beginX, beginY, endX, endY);
        return blackCount < 15;
    }

    private static boolean blankCenter(Bitmap bitmap) {
        int beginX = 13, beginY = 24, endX = 26, endY = 28;
        int blackCount = getBlackCount(bitmap, beginX, beginY, endX, endY);
        return blackCount < 15;
    }

    private static boolean blankRightTop(Bitmap bitmap) {
        int beginX = 31, beginY = 15, endX = 38, endY = 20;
        int blackCount = getBlackCount(bitmap, beginX, beginY, endX, endY);
        return blackCount < 15;
    }

    private static boolean blankRightBottom(Bitmap bitmap) {
        int beginX = 31, beginY = 30, endX = 38, endY = 37;
        int blackCount = getBlackCount(bitmap, beginX, beginY, endX, endY);
        return blackCount < 15;
    }

    private static boolean fewBottom(Bitmap bitmap) {
        int beginX = 5, beginY = 50, endX = 34, endY = 54;
        int blackCount = getBlackCount(bitmap, beginX, beginY, endX, endY);
        return blackCount < 50;
    }

    // 从验证码位图获取验证码数字
    public static String getNumber(Bitmap bitmap) {
        List<Bitmap> bitmapList = splitImage(bitmap); // 从位图中切割四个验证码数字图像
        StringBuilder total_num = new StringBuilder();
        int number = 0;
        for (int i = 0; i < bitmapList.size(); i++) {
            if (blankLeftBottom(bitmapList.get(i))) { // 左下角空白。1、2、3、5、7、9
                if (fewBottom(bitmapList.get(i))) { // 底部稀疏。1、7
                    if (fewTop(bitmapList.get(i))) { // 顶部稀疏
                        number = 1;
                    } else { // 顶部稠密
                        number = 7;
                    }
                } else { // 底部稠密。2、3、5、9
                    if (blankLeftTop(bitmapList.get(i))) { // 左上角空白。2、3
                        if (blankRightBottom(bitmapList.get(i))) { // 右下角空白
                            number = 2;
                        } else { // 右下角非空
                            number = 3;
                        }
                    } else { // 左上角非空。5、9
                        if (blankRightTop(bitmapList.get(i))) { // 右上角空白
                            number = 5;
                        } else { // 右上角非空
                            number = 9;
                        }
                    }
                }
            } else { // 左下角非空。0、4、6、8
                if (fewBottom(bitmapList.get(i))) { // 底部稀疏
                    number = 4;
                } else { // 底部稠密
                    if (blankCenter(bitmapList.get(i))) { // 中间空白
                        number = 0;
                    } else { // 中间非空。6、8
                        if (blankRightTop(bitmapList.get(i))) { // 右上角空白
                            number = 6;
                        } else { // 右上角非空
                            number = 8;
                        }
                    }
                }
            }
            total_num.append(number);
        }
        return total_num.toString();
    }

}
