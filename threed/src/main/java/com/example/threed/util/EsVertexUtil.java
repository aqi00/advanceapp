package com.example.threed.util;

import android.util.Log;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EsVertexUtil {
    private final static String TAG = "EsVertexUtil";

    // 以下定义了立方体六个面的顶点坐标数组（每个坐标点都由三个浮点数组成）
    private static float[] vertexsFront = {
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f};
    private static float[] vertexsBack = {
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f};
    private static float[] vertexsTop = {
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f};
    private static float[] vertexsBottom = {
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f};
    private static float[] vertexsLeft = {
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f};
    private static float[] vertexsRight = {
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f};
    // 一个矩形有四个顶点，分成两个三角形。012围成一个三角形，123围成另一个三角形
    public static byte[] cubeIndicates = { 0, 1, 2, 1, 2, 3 };
    // 四个顶点在二维坐标系的位置：左下（编号0）、右下（编号1）、左上（编号2）、右上（编号3）
    public static float[] cubeTextCoord = { 0f, 0f, 1f, 0f, 0f, 1f, 1f, 1f };

    // 获得立方体的顶点列表
    public static List<FloatBuffer> getCubeVertexs() {
        List<FloatBuffer> vertexList = new ArrayList<>();
        vertexList.add(GlUtil.getFloatBuffer(vertexsFront));
        vertexList.add(GlUtil.getFloatBuffer(vertexsBack));
        vertexList.add(GlUtil.getFloatBuffer(vertexsTop));
        vertexList.add(GlUtil.getFloatBuffer(vertexsBottom));
        vertexList.add(GlUtil.getFloatBuffer(vertexsLeft));
        vertexList.add(GlUtil.getFloatBuffer(vertexsRight));
        return vertexList;
    }

    // 获得立方体的顶点数量
    public static int getCubePointCount() {
        return vertexsFront.length / 3;
    }

    // 获得球体的顶点列表
    public static List<FloatBuffer> getBallVertexs(int divide, float radius) {
        List<FloatBuffer> vertexList = new ArrayList<>();
        float latitude; // 纬度
        float latitudeNext; // 下一层纬度
        float longitude; // 经度
        float pointX; // 点坐标x
        float pointY; // 点坐标y
        float pointZ; // 点坐标z
        // 将纬度等分成divide份，这样就能计算出每一等份的纬度值
        for (int i = 0; i <= divide; i++) {
            // 获取当前等份的纬度值
            latitude = (float) (Math.PI / 2.0 - i * (Math.PI) / divide);
            // 获取下一等份的纬度值
            latitudeNext = (float) (Math.PI / 2.0 - (i + 1) * (Math.PI) / divide);
            // 当前纬度和下一纬度的点坐标
            float[] vertexs = new float[divide * 6 + 6];
            // 将经度等分成divide份，这样就能得到当前纬度值和下一纬度值的每一份经度值
            for (int j = 0; j <= divide; j++) {
                // 计算经度值
                longitude = (float) (j * (Math.PI * 2) / divide);
                pointX = (float) (Math.cos(latitude) * Math.cos(longitude));
                pointY = (float) Math.sin(latitude);
                pointZ = (float) -(Math.cos(latitude) * Math.sin(longitude));
                // 此经度值下的当前纬度的点坐标
                vertexs[6 * j + 0] = radius * pointX / 4.5f;
                vertexs[6 * j + 1] = radius * pointY / 4.5f;
                vertexs[6 * j + 2] = radius * pointZ / 4.5f;
                pointX = (float) (Math.cos(latitudeNext) * Math.cos(longitude));
                pointY = (float) Math.sin(latitudeNext);
                pointZ = (float) -(Math.cos(latitudeNext) * Math.sin(longitude));
                // 此经度值下的下一纬度的点坐标
                vertexs[6 * j + 3] = radius * pointX / 4.5f;
                vertexs[6 * j + 4] = radius * pointY / 4.5f;
                vertexs[6 * j + 5] = radius * pointZ / 4.5f;
                Log.d(TAG, "vertexs0="+vertexs[6 * j + 0]);
                Log.d(TAG, "vertexs1="+vertexs[6 * j + 1]);
                Log.d(TAG, "vertexs2="+vertexs[6 * j + 2]);
                Log.d(TAG, "vertexs3="+vertexs[6 * j + 3]);
                Log.d(TAG, "vertexs4="+vertexs[6 * j + 4]);
                Log.d(TAG, "vertexs5="+vertexs[6 * j + 5]);
            }
            // 将点坐标转换成FloatBuffer类型添加到点坐标集合ArrayList<FloatBuffer>里
            vertexList.add(GlUtil.getFloatBuffer(vertexs));
        }
        return vertexList;
    }

    // 获得球体的纹理坐标
    public static List<FloatBuffer> getTextureCoords(int divide) {
        List<FloatBuffer> textureList = new ArrayList<>();
        for (int i = 0; i <= divide; i++) {
            float[] texCoords = new float[divide * 4 + 4];
            for (int j = 0; j <= divide; j++) {
                texCoords[4 * j + 0] = j / (float) divide;
                texCoords[4 * j + 1] = i / (float) divide;
                texCoords[4 * j + 2] = j / (float) divide;
                texCoords[4 * j + 3] = (i + 1) / (float) divide;
            }
            textureList.add(GlUtil.getFloatBuffer(texCoords));
        }
        return textureList;
    }

    // 获得球体的顶点列表
    public static float[] getBallVertexArray(int divide, float radius) {
        float[] vertexArray = new float[0];
        int itemLength = divide * 6 + 6;
        float latitude; // 纬度
        float latitudeNext; // 下一层纬度
        float longitude; // 经度
        float pointX; // 点坐标x
        float pointY; // 点坐标y
        float pointZ; // 点坐标z
        // 将纬度等分成divide份，这样就能计算出每一等份的纬度值
        for (int i = 0; i <= divide; i++) {
            vertexArray = Arrays.copyOf(vertexArray, (i+1)*itemLength);
            // 获取当前等份的纬度值
            latitude = (float) (Math.PI / 2.0 - i * (Math.PI) / divide);
            // 获取下一等份的纬度值
            latitudeNext = (float) (Math.PI / 2.0 - (i + 1) * (Math.PI) / divide);
            // 当前纬度和下一纬度的点坐标
            float[] vertexs = new float[divide * 6 + 6];
            // 将经度等分成divide份，这样就能得到当前纬度值和下一纬度值的每一份经度值
            for (int j = 0; j <= divide; j++) {
                // 计算经度值
                longitude = (float) (j * (Math.PI * 2) / divide);
                pointX = (float) (Math.cos(latitude) * Math.cos(longitude));
                pointY = (float) Math.sin(latitude);
                pointZ = (float) -(Math.cos(latitude) * Math.sin(longitude));
                // 此经度值下的当前纬度的点坐标
                vertexs[6 * j + 0] = radius * pointX / 4.5f;
                vertexs[6 * j + 1] = radius * pointY / 4.5f;
                vertexs[6 * j + 2] = radius * pointZ / 4.5f;
                pointX = (float) (Math.cos(latitudeNext) * Math.cos(longitude));
                pointY = (float) Math.sin(latitudeNext);
                pointZ = (float) -(Math.cos(latitudeNext) * Math.sin(longitude));
                // 此经度值下的下一纬度的点坐标
                vertexs[6 * j + 3] = radius * pointX / 4.5f;
                vertexs[6 * j + 4] = radius * pointY / 4.5f;
                vertexs[6 * j + 5] = radius * pointZ / 4.5f;
                Log.d(TAG, "vertexs0="+vertexs[6 * j + 0]);
                Log.d(TAG, "vertexs1="+vertexs[6 * j + 1]);
                Log.d(TAG, "vertexs2="+vertexs[6 * j + 2]);
                Log.d(TAG, "vertexs3="+vertexs[6 * j + 3]);
                Log.d(TAG, "vertexs4="+vertexs[6 * j + 4]);
                Log.d(TAG, "vertexs5="+vertexs[6 * j + 5]);
            }
            System.arraycopy(vertexs, 0, vertexArray, i*itemLength, itemLength);
        }
        return vertexArray;
    }

    // 获得球体的纹理坐标
    public static float[] getTextureCoordArray(int divide) {
        float[] texCoordArray = new float[0];
        int itemLength = divide * 4 + 4;
        for (int i = 0; i <= divide; i++) {
            texCoordArray = Arrays.copyOf(texCoordArray, (i+1)*itemLength);
            float[] texCoords = new float[itemLength];
            for (int j = 0; j <= divide; j++) {
                texCoords[4 * j + 0] = j / (float) divide;
                texCoords[4 * j + 1] = i / (float) divide;
                texCoords[4 * j + 2] = j / (float) divide;
                texCoords[4 * j + 3] = (i + 1) / (float) divide;
            }
            System.arraycopy(texCoords, 0, texCoordArray, i*itemLength, itemLength);
        }
        return texCoordArray;
    }

}
