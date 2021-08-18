package com.example.animation;

import java.io.InputStream;

import com.example.animation.util.GifImage;
import com.example.animation.widget.GifView;

import android.graphics.ImageDecoder;
import android.graphics.Movie;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class GifActivity extends AppCompatActivity {
    private final static String TAG = "GifActivity";
    private ImageView iv_gif; // 声明一个图像视图对象
    private GifView gv_gif; // 声明一个动图视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif);
        iv_gif = findViewById(R.id.iv_gif);
        gv_gif = findViewById(R.id.gv_gif);
        initTypeSpinner(); // 初始化图像类型下拉框
    }

    // 初始化图像类型下拉框
    private void initTypeSpinner() {
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, typeArray);
        Spinner sp_type = findViewById(R.id.sp_type);
        sp_type.setPrompt("请选择图像类型");
        sp_type.setAdapter(typeAdapter);
        sp_type.setOnItemSelectedListener(new ImageTypeListener());
        sp_type.setSelection(0);
    }

    private String[] typeArray = {"显示GIF动图", "显示WebP动图"};
    class ImageTypeListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (arg2 == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // 利用Android9新增的AnimatedImageDrawable显示GIF动画
                    showAnimateDrawable(R.drawable.happy);
                } else {
                    showGifAnimationOld(R.drawable.happy); // 借助帧动画播放gif动图
                }
                showGifMovie(R.drawable.happy); // 通过Movie类播放动图
            } else if (arg2 == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    showAnimateDrawable(R.drawable.world_cup_2014);
                } else {
                    Toast.makeText(GifActivity.this, "播放WebP动图需要Android9及更高版本", Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void showAnimateDrawable(int imageId) {
        try {
            // 利用Android9新增的ImageDecoder获取图像来源
            ImageDecoder.Source source = ImageDecoder.createSource(getResources(), imageId);
            // 从数据源中解码得到图形数据
            Drawable drawable = ImageDecoder.decodeDrawable(source);
            iv_gif.setImageDrawable(drawable); // 设置图像视图的图形
            if (drawable instanceof Animatable) { // 如果是动图类型，就开始播放动图
                ((Animatable) iv_gif.getDrawable()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 显示GIF动画
    private void showGifAnimationOld(int imageId) {
        // 从资源文件中获取输入流对象
        InputStream is = getResources().openRawResource(imageId);
        GifImage gifImage = new GifImage(); // 创建一个GIF图像对象
        int code = gifImage.read(is); // 从输入流中读取gif数据
        if (code == GifImage.STATUS_OK) { // 读取成功
            GifImage.GifFrame[] frameList = gifImage.getFrames();
            // 创建一个帧动画
            AnimationDrawable ad_gif = new AnimationDrawable();
            for (GifImage.GifFrame frame : frameList) {
                // 把Bitmap位图对象转换为Drawable图形格式
                BitmapDrawable drawable = new BitmapDrawable(getResources(), frame.image);
                // 给帧动画添加指定图形，以及该帧的播放延迟
                ad_gif.addFrame(drawable, frame.delay);
            }
            // 设置帧动画是否只播放一次。为true表示只播放一次，为false表示循环播放
            ad_gif.setOneShot(false);
            iv_gif.setImageDrawable(ad_gif); // 设置图像视图的图形为帧动画
            ad_gif.start(); // 开始播放帧动画
        } else if (code == GifImage.STATUS_FORMAT_ERROR) {
            Toast.makeText(this, "该图片不是gif格式", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "gif图片读取失败:" + code, Toast.LENGTH_LONG).show();
        }
    }

    // 通过Movie类播放动图
    private void showGifMovie(int imageId) {
        // 从资源图片中解码得到电影对象
        Movie movie = Movie.decodeStream(getResources().openRawResource(imageId));
        gv_gif.setMovie(movie); // 设置电影对象
    }

}
