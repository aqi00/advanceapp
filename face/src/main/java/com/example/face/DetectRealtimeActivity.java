package com.example.face;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.example.face.util.DateUtil;

import org.opencv.android.CameraActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

//OpenCV的实时扫描页面必须继承CameraActivity
public class DetectRealtimeActivity extends CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "DetectRealtimeActivity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private Mat mRgba, mGray; // 全彩矩阵，灰度矩阵
    private CascadeClassifier mJavaDetector; // OpenCV的人脸检测器
    private int mAbsoluteFaceSize = 0; // 绝对人脸大小
    // OpenCV默认横屏扫描，需要旋转90度改成竖屏预览，详细改动见CameraBridgeViewBase.java的deliverAndDrawFrame方法
    private CameraBridgeViewBase jcv_detect; // 声明一个OpenCV的相机视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_realtime);
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("实时检测人脸");
        jcv_detect = findViewById(R.id.jcv_detect);
        jcv_detect.setVisibility(CameraBridgeViewBase.VISIBLE);
        jcv_detect.setCvCameraViewListener(this); // 设置OpenCV的相机视图监听器
    }

    @Override
    public void onPause() {
        super.onPause();
        if (jcv_detect != null) {
            jcv_detect.disableView(); // 禁用OpenCV的相机视图
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(jcv_detect);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        jcv_detect.disableView(); // 禁用OpenCV的相机视图
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    // 相机预览回调
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        Core.rotate(mRgba, mRgba, Core.ROTATE_90_CLOCKWISE); // 适配竖屏，顺时针旋转90度
        Core.rotate(mGray, mGray, Core.ROTATE_90_CLOCKWISE); // 适配竖屏，顺时针旋转90度
        if (mAbsoluteFaceSize == 0) {
            Log.d(TAG, "width="+mGray.width()+", height="+mGray.height()+", cols="+mGray.cols()+", rows="+mGray.rows());
            int height = mGray.rows();
            if (Math.round(height * 0.2f) > 0) {
                mAbsoluteFaceSize = Math.round(height * 0.2f);
            }
//            String filePath = String.format("%s/%s.jpg",
//                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
//                    DateUtil.getNowDateTime());
//            Imgcodecs.imwrite(filePath, mRgba);
//            Log.d(TAG, "filePath="+filePath);
        }
        MatOfRect faces = new MatOfRect();
        if (mJavaDetector != null) { // 检测器开始识别人脸
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        Rect[] faceArray = faces.toArray();
        for (Rect rect : faceArray) { // 给找到的人脸标上相框
            Imgproc.rectangle(mRgba, rect.tl(), rect.br(), FACE_RECT_COLOR, 3);
            Log.d(TAG, rect.toString());
        }
        Core.rotate(mRgba, mRgba, Core.ROTATE_90_COUNTERCLOCKWISE); // 恢复原状，逆时针旋转90度
        return mRgba;
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
                jcv_detect.enableView(); // 启用OpenCV的相机视图
            } else {
                super.onManagerConnected(status);
            }
        }
    };

}