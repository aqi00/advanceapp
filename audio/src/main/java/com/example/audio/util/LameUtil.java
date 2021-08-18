package com.example.audio.util;

public class LameUtil {
    static {
        System.loadLibrary("lamemp3"); // 加载so库
    }
    // 查看Lame版本号
    public native static String version();
    // 初始化Lame
    public native static void init(int inSampleRate, int inChannel, int outSampleRate, int outBitrate, int quality);
    // 开始MP3转码
    public native static int encode(short[] buffer_l, short[] buffer_r, int samples, byte[] mp3buf);
    // 写入缓冲区
    public native static int flush(byte[] mp3buf);
    // 关闭Lame
    public native static void close();
}
