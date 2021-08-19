package com.example.threed.util;

import java.util.ArrayList;
import java.util.List;

public class PanoramaUtil {

    // 获取全景图片的顶点列表
    public static List<Float> getPanoramaVertexList(int perVertex, double perRadius) {
        List<Float> vertexList = new ArrayList<>();
        for (int i = 0; i < perVertex; i++) {
            for (int j = 0; j < perVertex; j++) {
                float x1 = (float) (Math.sin(i * perRadius / 2) * Math.cos(j * perRadius));
                float z1 = (float) (Math.sin(i * perRadius / 2) * Math.sin(j * perRadius));
                float y1 = (float) Math.cos(i * perRadius / 2);

                float x2 = (float) (Math.sin((i + 1) * perRadius / 2) * Math.cos(j * perRadius));
                float z2 = (float) (Math.sin((i + 1) * perRadius / 2) * Math.sin(j * perRadius));
                float y2 = (float) Math.cos((i + 1) * perRadius / 2);

                float x3 = (float) (Math.sin((i + 1) * perRadius / 2) * Math.cos((j + 1) * perRadius));
                float z3 = (float) (Math.sin((i + 1) * perRadius / 2) * Math.sin((j + 1) * perRadius));
                float y3 = (float) Math.cos((i + 1) * perRadius / 2);

                float x4 = (float) (Math.sin(i * perRadius / 2) * Math.cos((j + 1) * perRadius));
                float z4 = (float) (Math.sin(i * perRadius / 2) * Math.sin((j + 1) * perRadius));
                float y4 = (float) Math.cos(i * perRadius / 2);

                vertexList.add(x1);
                vertexList.add(y1);
                vertexList.add(z1);

                vertexList.add(x2);
                vertexList.add(y2);
                vertexList.add(z2);

                vertexList.add(x3);
                vertexList.add(y3);
                vertexList.add(z3);

                vertexList.add(x3);
                vertexList.add(y3);
                vertexList.add(z3);

                vertexList.add(x4);
                vertexList.add(y4);
                vertexList.add(z4);

                vertexList.add(x1);
                vertexList.add(y1);
                vertexList.add(z1);
            }
        }
        return vertexList;
    }

    // 获取全景图片的纹理列表
    public static List<Float> getPanoramaTextureList(int perVertex) {
        List<Float> textureList = new ArrayList<>();
        double perW = 1 / (float) perVertex;
        double perH = 1 / (float) (perVertex);
        for (int i = 0; i < perVertex; i++) {
            for (int j = 0; j < perVertex; j++) {
                float w1 = (float) (i * perH);
                float h1 = (float) (j * perW);

                float w2 = (float) ((i + 1) * perH);
                float h2 = (float) (j * perW);

                float w3 = (float) ((i + 1) * perH);
                float h3 = (float) ((j + 1) * perW);

                float w4 = (float) (i * perH);
                float h4 = (float) ((j + 1) * perW);

                textureList.add(h1);
                textureList.add(w1);

                textureList.add(h2);
                textureList.add(w2);

                textureList.add(h3);
                textureList.add(w3);

                textureList.add(h3);
                textureList.add(w3);

                textureList.add(h4);
                textureList.add(w4);

                textureList.add(h1);
                textureList.add(w1);
            }
        }
        return textureList;
    }

}
