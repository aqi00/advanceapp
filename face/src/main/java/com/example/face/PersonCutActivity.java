package com.example.face;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.face.util.BitmapUtil;
import com.example.face.util.DateUtil;
import com.example.face.widget.BitmapView;
import com.example.face.widget.MeituView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class PersonCutActivity extends AppCompatActivity implements MeituView.ImageChangetListener {
    private final static String TAG = "PersonCutActivity";
    private MeituView mv_content; // 声明一个美图视图对象
    private BitmapView bv_content; // 声明一个位图视图对象
    private CascadeClassifier mJavaDetector; // OpenCV的人脸检测器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_cut);
        findViewById(R.id.iv_cancel).setOnClickListener(v -> finish());
        findViewById(R.id.iv_confirm).setOnClickListener(v -> saveFace());
        mv_content = findViewById(R.id.mv_content);
        mv_content.setImageChangetListener(this); // 设置美图视图的图像变更监听器
        bv_content = findViewById(R.id.bv_content);
        bv_content.setDrawingCacheEnabled(true); // 开启位图视图的绘图缓存
        String image_path = getIntent().getStringExtra("image_path");
        Log.d(TAG, "image_path="+image_path);
        bv_content.setImageBitmap(BitmapFactory.decodeFile(image_path)); // 设置位图视图的位图对象
        // 延迟200毫秒再刷新图像展示，因为开启绘图缓存需要时间
        new Handler(Looper.myLooper()).postDelayed(() -> refreshImage(true), 200);
    }

    // 保存人脸图片
    private void saveFace() {
        Bitmap bitmap = mv_content.getCropBitmap(); // 获取美图视图处理后的位图
        int detectCount = detectFaceCount(bitmap); // 检测位图中的人脸数量
        if (detectCount == 0) {
            Toast.makeText(this, "该图像未检测到人脸，请重新选择", Toast.LENGTH_SHORT).show();
        } else if (detectCount > 1) {
            Toast.makeText(this, "该图像检测到多张人脸，请重新选择", Toast.LENGTH_SHORT).show();
        } else { // 有且仅有一张人脸
            String face_path = String.format("%s/%s.jpg",
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                    DateUtil.getNowDateTime());
            BitmapUtil.saveImage(face_path, bitmap); // 把位图保存为图片文件
            Intent intent = new Intent(); // 创建一个新意图
            intent.putExtra("face_path", face_path); // 把快递包裹塞给意图
            setResult(Activity.RESULT_OK, intent); // 携带意图返回上一个页面
            finish(); // 关闭当前页面
        }
    }

    // 刷新图像展示
    private void refreshImage(boolean is_first) {
        Bitmap bitmap = bv_content.getDrawingCache(); // 从绘图缓存获取位图对象
        mv_content.setOrigBitmap(bitmap); // 设置美图视图的原始位图
        if (is_first) { // 首次打开
            int left = bitmap.getWidth() / 4;
            int top = bitmap.getHeight() / 4;
            // 设置美图视图的位图边界
            mv_content.setBitmapRect(new Rect(left, top, left * 2, top * 2));
        } else { // 非首次打开
            // 设置美图视图的位图边界
            mv_content.setBitmapRect(mv_content.getBitmapRect());
        }
    }

    // 在图片平移时触发
    @Override
    public void onImageTraslate(int offsetX, int offsetY, boolean bReset) {
        bv_content.setOffset(offsetX, offsetY, bReset); // 设置位图视图的偏移距离
        refreshImage(false); // 刷新图像展示
    }

    // 在图片缩放时触发
    @Override
    public void onImageScale(float ratio) {
        bv_content.setScaleRatio(ratio, false); // 设置位图视图的缩放比率
        refreshImage(false); // 刷新图像展示
    }

    // 在图片旋转时触发
    @Override
    public void onImageRotate(int degree) {
        bv_content.setRotateDegree(degree, false); // 设置位图视图的旋转角度
        refreshImage(false); // 刷新图像展示
    }

    // 在图片点击时触发
    @Override
    public void onImageClick() {}

    // 在图片长按时触发
    @Override
    public void onImageLongClick() {}

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.d(TAG, "OpenCV loaded successfully");
                // 在OpenCV初始化完成后加载so库
                System.loadLibrary("detection_based_tracker");
                File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                File cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                // 从应用程序资源加载级联文件
                try (InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                     FileOutputStream os = new FileOutputStream(cascadeFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 根据级联文件创建OpenCV的人脸检测器
                mJavaDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
                if (mJavaDetector.empty()) {
                    Log.d(TAG, "Failed to load cascade classifier");
                    mJavaDetector = null;
                } else {
                    Log.d(TAG, "Loaded cascade classifier from " + cascadeFile.getAbsolutePath());
                }
                cascadeDir.delete();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    // 检测位图中的人脸数量
    private int detectFaceCount(Bitmap origin) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(origin, rgba); // 把位图对象转为Mat结构
        Mat gray = new Mat();
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGB2GRAY); // 全彩矩阵转灰度矩阵
        // 下面开始检测人脸
        MatOfRect faces = new MatOfRect();
        int absoluteFaceSize = 0;
        int height = gray.rows();
        if (Math.round(height * 0.2f) > 0) {
            absoluteFaceSize = Math.round(height * 0.2f);
        }
        if (mJavaDetector != null) { // 检测器开始识别人脸
            mJavaDetector.detectMultiScale(gray, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        org.opencv.core.Rect[] faceArray = faces.toArray();
        return faceArray.length;
    }

}