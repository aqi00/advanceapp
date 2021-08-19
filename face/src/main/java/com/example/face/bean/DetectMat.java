package com.example.face.bean;

import org.opencv.core.Mat;

public class DetectMat {
    private Mat mat; // OpenCV的图像结构
    private Double similarDegree; // 相似程度

    public DetectMat(Mat mat, double similarDegree) {
        this.mat = mat;
        this.similarDegree = similarDegree;
    }

    public void setMat(Mat mat) {
        this.mat = mat;
    }

    public Mat getMat() {
        return this.mat;
    }

    public void setSimilarDegree(Double similarDegree) {
        this.similarDegree = similarDegree;
    }

    public Double getSimilarDegree() {
        return this.similarDegree;
    }

}
