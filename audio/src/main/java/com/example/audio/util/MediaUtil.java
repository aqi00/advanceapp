package com.example.audio.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.audio.bean.AudioInfo;

@SuppressLint("DefaultLocale")
public class MediaUtil {
    private final static String TAG = "MediaUtil";

    // 格式化播放时长（mm:ss）
    public static String formatDuration(int milliseconds) {
        int seconds = milliseconds / 1000;
        int hour = seconds / 3600;
        int minute = seconds / 60;
        int second = seconds % 60;
        String str;
        if (hour > 0) {
            str = String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            str = String.format("%02d:%02d", minute, second);
        }
        return str;
    }

    // 从content://media/external/audio/media/这样的Uri中获取音频信息
    public static AudioInfo getPathFromContentUri(Context context, Uri uri) {
        AudioInfo audio = new AudioInfo(); // 创建一个音频信息对象
        audio.setAudio(uri.toString());
        String path = uri.toString();
        if (path.startsWith("content://")) {
            String[] proj = new String[]{ // 媒体库的字段名称数组
                    MediaStore.Audio.Media._ID, // 编号
                    MediaStore.Audio.Media.TITLE, // 标题
                    MediaStore.Audio.Media.DURATION, // 播放时长
                    MediaStore.Audio.Media.SIZE, // 文件大小
                    MediaStore.Audio.Media.ARTIST, // 演唱者
                    MediaStore.Audio.Media.DATA}; // 文件路径
            try (Cursor cursor = context.getContentResolver().query(uri,
                    proj, null, null, null)) {
                cursor.moveToFirst(); // 把游标移动到开头
                audio.setId(cursor.getLong(0)); // 设置音频编号
                audio.setTitle(cursor.getString(1)); // 设置音频标题
                audio.setDuration(cursor.getInt(2)); // 设置音频时长
                audio.setSize(cursor.getLong(3)); // 设置音频大小
                audio.setArtist(cursor.getString(4)); // 设置音频演唱者
                audio.setAudio(cursor.getString(5)); // 设置音频路径
                Log.d(TAG, audio.getTitle() + " " + audio.getDuration() + " " + audio.getSize() + " " + audio.getAudio());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return audio;
    }

}
