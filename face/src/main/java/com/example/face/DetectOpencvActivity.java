package com.example.face;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.face.util.BitmapUtil;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class DetectOpencvActivity extends AppCompatActivity {
    private final static String TAG = "DetectOpencvActivity";
    private int CHOOSE_CODE = 3; // 只在相册挑选图片的请求码
    private ImageView iv_face; // 声明一个图像视图对象
    private CascadeClassifier mJavaDetector; // OpenCV的人脸检测器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_opencv);
        iv_face = findViewById(R.id.iv_face);
        findViewById(R.id.btn_choose).setOnClickListener(v -> {
            // 创建一个内容获取动作的意图（准备跳到系统相册）
            Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
            albumIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); // 是否允许多选
            albumIntent.setType("image/*"); // 类型为图像
            startActivityForResult(albumIntent, CHOOSE_CODE); // 打开系统相册
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && requestCode == CHOOSE_CODE) { // 从相册返回
            if (intent.getData() != null) { // 从相册选择一张照片
                Uri uri = intent.getData(); // 获得已选择照片的路径对象
                // 根据指定图片的uri，获得自动缩小后的位图对象
                Bitmap bitmap = BitmapUtil.getAutoZoomImage(this, uri);
                detectFace(bitmap); // 检测位图中的人脸
            }
        }
    }

    // 检测位图中的人脸
    private void detectFace(Bitmap orig) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(orig, rgba); // 把位图对象转为Mat结构
        //Mat rgba = Imgcodecs.imread(mFilePath); // 从文件路径读取Mat结构
        //Imgcodecs.imwrite(tempFile.getAbsolutePath(), rgba); // 把Mate结构保存为文件
        Mat gray = new Mat();
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGB2GRAY); // 全彩矩阵转灰度矩阵
        // 下面检测并显示人脸
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
        Rect[] faceArray = faces.toArray();
        for (Rect rect : faceArray) { // 给找到的人脸标上相框
            Imgproc.rectangle(rgba, rect.tl(), rect.br(), new Scalar(0, 255, 0, 255), 3);
            Log.d(TAG, rect.toString());
        }
        Bitmap mark = Bitmap.createBitmap(orig.getWidth(), orig.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, mark); // 把Mat结构转为位图对象
        iv_face.setImageBitmap(mark);
    }

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

}