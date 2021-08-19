package com.example.threed.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class GlVertexUtil {
    // 以下定义了立方体六个面的顶点坐标数组（每个坐标点都由三个浮点数组成）
    private static float[] vertexsFront = {2f, 2f, 2f, 2f, 2f, -2f, -2f, 2f, -2f, -2f, 2f, 2f};
    private static float[] vertexsBack = {2f, -2f, 2f, 2f, -2f, -2f, -2f, -2f, -2f, -2f, -2f, 2f};
    private static float[] vertexsTop = {2f, 2f, 2f, 2f, -2f, 2f, -2f, -2f, 2f, -2f, 2f, 2f};
    private static float[] vertexsBottom = {2f, 2f, -2f, 2f, -2f, -2f, -2f, -2f, -2f, -2f, 2f, -2f};
    private static float[] vertexsLeft = {-2f, 2f, 2f, -2f, 2f, -2f, -2f, -2f, -2f, -2f, -2f, 2f};
    private static float[] vertexsRight = {2f, 2f, 2f, 2f, 2f, -2f, 2f, -2f, -2f, 2f, -2f, 2f};

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
                vertexs[6 * j + 0] = radius * pointX;
                vertexs[6 * j + 1] = radius * pointY;
                vertexs[6 * j + 2] = radius * pointZ;
                pointX = (float) (Math.cos(latitudeNext) * Math.cos(longitude));
                pointY = (float) Math.sin(latitudeNext);
                pointZ = (float) -(Math.cos(latitudeNext) * Math.sin(longitude));
                // 此经度值下的下一纬度的点坐标
                vertexs[6 * j + 3] = radius * pointX;
                vertexs[6 * j + 4] = radius * pointY;
                vertexs[6 * j + 5] = radius * pointZ;
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

}
