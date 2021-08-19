package com.example.face;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.face.bean.DetectMat;
import com.example.face.dao.PersonDao;
import com.example.face.entity.PersonPortrait;
import com.example.face.util.BitmapUtil;
import com.example.face.util.DateUtil;
import com.example.face.util.FaceUtil;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//OpenCV的实时扫描页面必须继承CameraActivity
public class PersonVerifyActivity extends CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private final static String TAG = "PersonVerifyActivity";
    private TextView tv_option; // 声明一个文本视图对象
    private String mPersonName; // 人员名称
    private PersonDao personDao; // 声明一个人员的持久化对象
    private List<Mat> mSampleMatList = new ArrayList<>(); // 样本头像列表
    private List<DetectMat> mDetectMatList = new ArrayList<>(); // 检测到的头像列表
    private Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255); // 绿色
    private Mat mRgba, mGray; // 全彩矩阵，灰度矩阵
    private CascadeClassifier mJavaDetector; // OpenCV的人脸检测器
    private int mAbsoluteFaceSize = 0; // 绝对人脸大小
    // OpenCV默认横屏扫描，需要旋转90度改成竖屏预览，详细改动见CameraBridgeViewBase.java的deliverAndDrawFrame方法
    private CameraBridgeViewBase jcv_detect; // 声明一个OpenCV的相机视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_verify);
        mPersonName = getIntent().getStringExtra("person_name");
        // 从App实例中获取唯一的人员持久化对象
        personDao = MainApplication.getInstance().getPersonDB().personDao();
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("正在追踪"+mPersonName);
        tv_option = findViewById(R.id.tv_option);
        tv_option.setText("完成");
        tv_option.setOnClickListener(v -> finishVerify());
        jcv_detect = findViewById(R.id.jcv_detect);
        jcv_detect.setVisibility(CameraBridgeViewBase.VISIBLE);
        jcv_detect.setCvCameraViewListener(this); // 设置OpenCV的相机视图监听器
    }

    // 结束人脸识别
    private void finishVerify() {
        ArrayList<String> pathList = new ArrayList<>();
        for (DetectMat detectMat : mDetectMatList) {
            Mat mat = detectMat.getMat();
            Bitmap detect = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, detect); // 把Mat结构转为位图对象
            Bitmap realBitmap = BitmapUtil.getRotateBitmap(detect, 90);
            String face_path = String.format("%s/%s.jpg",
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                    DateUtil.getNowDateTimeDetail());
            BitmapUtil.saveImage(face_path, realBitmap); // 把位图保存为图片文件
            face_path = face_path + "|" + detectMat.getSimilarDegree();
            pathList.add(face_path);
        }
        Intent intent = new Intent(); // 创建一个新意图
        intent.putStringArrayListExtra("path_list", pathList); // 把快递包裹塞给意图
        setResult(Activity.RESULT_OK, intent); // 携带意图返回上一个页面
        finish(); // 关闭当前页面
    }

    // 初始化样本头像
    private void initSample() {
        // 从数据库中读取该人员的所有样本头像
        List<PersonPortrait> sampleList = personDao.queryPersonPortrait(mPersonName, 0);
        for (PersonPortrait portrait : sampleList) {
            Bitmap bitmap = BitmapFactory.decodeFile(portrait.getPath());
            Mat result = detectFace(bitmap); // 检测样本头像中的人脸
            if (result != null) {
                mSampleMatList.add(result);
            }
        }
    }

    // 从位图对象中检测并返回一张人脸
    private Mat detectFace(Bitmap orig) {
        Mat result = null;
        Mat rgba = new Mat();
        Utils.bitmapToMat(orig, rgba); // 把位图对象转为Mat结构
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
        Rect[] faceArray = faces.toArray();
        for (Rect rect : faceArray) { // 给找到的人脸标上相框
            result = rgba.submat(rect); // 截取相框中的人脸结构
        }
        return result;
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
        }
        MatOfRect faces = new MatOfRect();
        if (mJavaDetector != null) { // 检测器开始识别人脸
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        Rect[] faceArray = faces.toArray();
        Mat[] matArray = new Mat[faceArray.length];
        for (int i = 0; i < faceArray.length; i++) { // 给找到的人脸标上相框
            Rect rect = faceArray[i];
            Imgproc.rectangle(mRgba, rect.tl(), rect.br(), FACE_RECT_COLOR, 3);
            Log.d(TAG, rect.toString());
            matArray[i] = mRgba.submat(rect); // 截取相框中的人脸结构
        }
        new VerifyThread(matArray).start(); // 启动人脸识别线程
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
                new Thread(() -> initSample()).start(); // 启动样本初始化线程
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    // 定义一个人脸识别线程，比较待检测人脸与样本人脸的相似度
    private class VerifyThread extends Thread {
        private Mat[] mMatArray; // 识别到的人脸矩阵数组
        public VerifyThread(Mat[] matArray) {
            mMatArray = matArray;
        }

        @Override
        public void run() {
            //Log.d(TAG, "similarDegree mMatArray.length="+mMatArray.length+", mSampleMatList.size()="+mSampleMatList.size());
            if (mMatArray.length==0 || mSampleMatList.size()==0) {
                return;
            }
            for (Mat detectMat : mMatArray) {
                for (Mat sampleMat : mSampleMatList) {
                    // 计算待检测人脸与样本人脸的相似度
                    double similarDegree = calculateSimilarity(detectMat, sampleMat);
                    Log.d(TAG, "similarDegree="+similarDegree);
                    if (similarDegree > 0.3) { // 找到了相似的人脸
                        runOnUiThread(() -> tv_option.setVisibility(View.VISIBLE));
                        FACE_RECT_COLOR = new Scalar(255, 0, 0, 255); // 红色
                        addDetectMat(detectMat, similarDegree); // 将待检测人脸添加到已识别人脸
                    }
                }
            }
        }
    }

    // 计算待检测人脸与样本人脸的相似度
    private double calculateSimilarity(Mat detectMat, Mat sampleMat) {
        Bitmap detect = Bitmap.createBitmap(detectMat.width(), detectMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(detectMat, detect); // 把Mat结构转为位图对象
        Bitmap sample = Bitmap.createBitmap(sampleMat.width(), sampleMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(sampleMat, sample); // 把Mat结构转为位图对象
        Bitmap bitmapA, bitmapB;
        // 两幅图片必须尺寸一样才能比较，故而下面事先调整位图尺寸，使得两幅图片的宽高保持一致
        if (detect.getWidth() < sample.getWidth()) {
            bitmapA = detect;
            bitmapB = Bitmap.createScaledBitmap(sample, detect.getWidth(), detect.getHeight(), false);
        } else {
            bitmapA = sample;
            bitmapB = Bitmap.createScaledBitmap(detect, sample.getWidth(), sample.getHeight(), false);
        }
        return FaceUtil.matchCompare(bitmapA, bitmapB); // 比较两个位图的相似程度
    }

    // 将待检测人脸添加到已识别人脸
    private void addDetectMat(Mat detectMat, double degree) {
        Core.rotate(detectMat, detectMat, Core.ROTATE_90_COUNTERCLOCKWISE);
        if (mDetectMatList.size() < 3) {
            mDetectMatList.add(new DetectMat(detectMat, degree));
        } else {
            DetectMat lastDetect = mDetectMatList.get(mDetectMatList.size()-1);
            if (degree > lastDetect.getSimilarDegree()) {
                mDetectMatList.remove(mDetectMatList.size()-1);
                mDetectMatList.add(new DetectMat(detectMat, degree));
            }
        }
        // 将已识别人脸列表按照相似度降序排列
        Collections.sort(mDetectMatList, (o1, o2) -> o2.getSimilarDegree().compareTo(o1.getSimilarDegree()));
        for (DetectMat detect : mDetectMatList) {
            Log.d(TAG, "sort similarDegree="+detect.getSimilarDegree());
        }
    }

}