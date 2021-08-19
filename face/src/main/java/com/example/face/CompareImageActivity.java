package com.example.face;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.face.util.BitmapUtil;
import com.example.face.util.FaceUtil;

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

public class CompareImageActivity extends AppCompatActivity {
    private final static String TAG = "CompareImageActivity";
    private int CHOOSE_CODE1=31, CHOOSE_CODE2=32; // 挑选第一张图片和挑选第二张图片的请求码
    private Bitmap mBitmap1, mBitmap2; // 第一张图片和第二张图片的位图对象
    private ImageView iv_face1, iv_face2; // 第一张图片和第二张图片的图像视图
    private TextView tv_result; // 声明一个文本视图对象
    private CascadeClassifier mJavaDetector; // OpenCV的人脸检测器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_image);
        iv_face1 = findViewById(R.id.iv_face1);
        iv_face2 = findViewById(R.id.iv_face2);
        tv_result = findViewById(R.id.tv_result);
        findViewById(R.id.btn_choose1).setOnClickListener(v -> choosePicture(CHOOSE_CODE1));
        findViewById(R.id.btn_choose2).setOnClickListener(v -> choosePicture(CHOOSE_CODE2));
        findViewById(R.id.btn_compare).setOnClickListener(v -> {
            if (mBitmap1==null || mBitmap2==null) {
                Toast.makeText(this, "请先选择两张图片再来比较", Toast.LENGTH_SHORT).show();
                return;
            }
            compareFace(); // 比较两张人脸的相似度
        });
    }

    // 挑选待比较的人脸图片
    private void choosePicture(int chooseCode) {
        // 创建一个内容获取动作的意图（准备跳到系统相册）
        Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        albumIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); // 是否允许多选
        albumIntent.setType("image/*"); // 类型为图像
        startActivityForResult(albumIntent, chooseCode); // 打开系统相册
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && intent.getData() != null) { // 从相册返回
            Uri uri = intent.getData(); // 获得已选择照片的路径对象
            if (requestCode == CHOOSE_CODE1) {
                // 根据指定图片的uri，获得自动缩小后的位图对象
                mBitmap1 = BitmapUtil.getAutoZoomImage(this, uri);
                iv_face1.setImageBitmap(mBitmap1);
            } else if (requestCode == CHOOSE_CODE2) {
                // 根据指定图片的uri，获得自动缩小后的位图对象
                mBitmap2 = BitmapUtil.getAutoZoomImage(this, uri);
                iv_face2.setImageBitmap(mBitmap2);
            }
        }
    }

    // 检测位图中的人脸
    private Mat[] detectFace(Bitmap orig, ImageView imageView) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(orig, rgba); // 把位图对象转为Mat结构
        Mat gray = new Mat();
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGB2GRAY); // 全彩矩阵转灰度矩阵
        // 下面识别人脸
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
        Mat[] matArray = new Mat[faceArray.length];
        for (int i = 0; i < faceArray.length; i++) { // 给找到的人脸标上相框
            Imgproc.rectangle(rgba, faceArray[i].tl(), faceArray[i].br(), new Scalar(0, 255, 0, 255), 3);
            //Log.d(TAG, faceArray[i].toString());
            matArray[i] = rgba.submat(faceArray[i]); // 截取相框中的人脸结构
        }
        Bitmap mark = Bitmap.createBitmap(orig.getWidth(), orig.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, mark); // 把Mat结构转为位图对象
        imageView.setImageBitmap(mark);
        return matArray;
    }

    // 比较两张人脸的相似度
    private void compareFace() {
        Mat[] matArray1 = detectFace(mBitmap1, iv_face1); // 检测位图中的人脸
        Mat[] matArray2 = detectFace(mBitmap2, iv_face2); // 检测位图中的人脸
        if (matArray1.length==0 || matArray2.length==0) {
            Toast.makeText(this, "需要两张图片均可找到人脸才能比较", Toast.LENGTH_SHORT).show();
            tv_result.setText("未能检测到人脸");
            return;
        }
        Mat mat1 = matArray1[0];
        Mat mat2 = matArray2[0];
        Bitmap bitmap1 = Bitmap.createBitmap(mat1.width(), mat1.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat1, bitmap1); // 把Mat结构转为位图对象
        Bitmap bitmap2 = Bitmap.createBitmap(mat2.width(), mat2.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat2, bitmap2); // 把Mat结构转为位图对象
        Bitmap bitmapA, bitmapB;
        // 两幅图片必须尺寸一样才能比较，故而下面事先调整位图尺寸，使得两幅图片的宽高保持一致
        if (bitmap1.getWidth() < bitmap2.getWidth()) {
            bitmapA = bitmap1;
            bitmapB = BitmapUtil.getScaleBitmap(bitmap2, 1.0*bitmap1.getWidth()/bitmap2.getWidth());
        } else {
            bitmapA = bitmap2;
            bitmapB = BitmapUtil.getScaleBitmap(bitmap1, 1.0*bitmap2.getWidth()/bitmap1.getWidth());
        }
        double degree = FaceUtil.matchCompare(bitmapA, bitmapB); // 比较两个位图的相似程度
        String desc = String.format("相似度为%.2f（完全相同为1，完全不同为0）", degree);
        tv_result.setText(desc);
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
            } else{
                super.onManagerConnected(status);
            }
        }
    };

}