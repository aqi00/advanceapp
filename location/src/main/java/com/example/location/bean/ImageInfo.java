package com.example.location.bean;

import android.graphics.Bitmap;

public class ImageInfo {
    private long id; // 图片编号
    private String name; // 图片标题
    private long size; // 文件大小
    private String path; // 文件路径
    private Bitmap bitmap; // 位图对象

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

}
