package com.example.face;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.face.util.BitmapUtil;
import com.example.face.util.FaceUtil;
import com.example.face.util.FaceUtil.FaceText;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GuessAgeActivity extends AppCompatActivity {
    private final static String TAG = "GuessAgeActivity";
    private int CHOOSE_CODE = 3; // 只在相册挑选图片的请求码
    private ImageView iv_face; // 声明一个图像视图对象
    private CascadeClassifier mJavaDetector; // OpenCV的人脸检测器
    private Net mAgeNet, mGenderNet; // 年龄模型，性别模型

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_age);
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
                guessAgeAndSex(bitmap); // 根据人脸猜测年龄和性别
            }
        }
    }

    // 根据人脸猜测年龄和性别
    private void guessAgeAndSex(Bitmap orig) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(orig, rgba); // 把位图对象转为Mat结构
        Mat gray = new Mat();
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGB2GRAY); // 全彩矩阵转灰度矩阵
        Mat three = new Mat();
        Imgproc.cvtColor(rgba, three, Imgproc.COLOR_RGBA2RGB); // 四通道转三通道
        // 下面识别人脸并预测年龄和性别
        MatOfRect faces = new MatOfRect();
        int height = gray.rows();
        int absoluteFaceSize = 0;
        if (Math.round(height * 0.2f) > 0) {
            absoluteFaceSize = Math.round(height * 0.2f);
        }
        if (mJavaDetector != null) { // 检测器开始识别人脸
            mJavaDetector.detectMultiScale(gray, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        Rect[] faceArray = faces.toArray();
        List<FaceText> textList = new ArrayList<>();
        int lineWidth = Math.max(orig.getWidth()/600 + 1, orig.getHeight()/600 + 1);
        for (Rect rect : faceArray) { // 给找到的人脸标上相框
            String ageText = predictAge(mAgeNet, three.submat(rect)); // 猜测年龄
            String genderText = predictGender(mGenderNet, three.submat(rect)); // 猜测性别
            Scalar scalar = new Scalar(0, 255, 0, 255);
            Imgproc.rectangle(rgba, rect.tl(), rect.br(), scalar, lineWidth);
            PointF pos = new PointF((float) rect.tl().x / rgba.width(), (float) rect.tl().y / rgba.height());
            textList.add(new FaceText(pos, genderText + "，" + ageText));
            //OpenCV的putText方法写中文会乱码，目前OpenCV的Java开发包还不支持中文
            //Imgproc.putText(rgba, ageText, rect.tl(), FONT_HERSHEY_PLAIN, 1.2, new Scalar(0, 0, 255), 1);
        }
        Bitmap mark = Bitmap.createBitmap(orig.getWidth(), orig.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, mark); // 把Mat结构转为位图对象
        mark = FaceUtil.drawTextList(this, mark, textList); // 往位图添加多个文字
        iv_face.setImageBitmap(mark);
    }

    // 获取年龄列表
    private List<String> ageLabels() {
        List<String> ageList = new ArrayList<>();
        ageList.add("0 - 3");
        ageList.add("4 - 7");
        ageList.add("8 - 14");
        ageList.add("15 - 24");
        ageList.add("25 - 37");
        ageList.add("38 - 50");
        ageList.add("51 - 65");
        ageList.add("66 -");
        return ageList;
    }

    // 根据模型网络分析预测图像矩阵
    private Core.MinMaxLocResult predictResult(Net modelNet, Mat imageMat) {
        // 输入图像矩阵
        Mat blob = Dnn.blobFromImage(imageMat, 1.0, new Size(227, 227));
        modelNet.setInput(blob, "data");
        Mat prob = modelNet.forward("prob"); // 模型网络开始预测
        Mat probMat = prob.reshape(1, 1);
        return Core.minMaxLoc(probMat);
    }

    // 猜测年龄
    private String predictAge(Net modelNet, Mat imageMat) {
        Core.MinMaxLocResult result = predictResult(modelNet, imageMat);
        return ageLabels().get((int) result.maxLoc.x);
    }

    // 猜测性别
    private String predictGender(Net modelNet, Mat imageMat) {
        Core.MinMaxLocResult result = predictResult(modelNet, imageMat);
        //return ((int) result.maxLoc.x)==1 ? "男" : "女";
        return ((int) result.maxLoc.x)==1 ? "女" : "男";
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
                new Thread(() -> importModel()).start(); // 启动分线程导入年龄模型和性别模型
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

    // 导入年龄模型和性别模型
    private void importModel() {
        String prePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
        String age_model = prePath + "age_net.caffemodel";
        String age_text = prePath + "age_deploy.prototxt";
        String gender_model = prePath + "gender_net.caffemodel";
        String gender_text = prePath + "gender_deploy.prototxt";
        mAgeNet = Dnn.readNetFromCaffe(age_text, age_model);
        mGenderNet = Dnn.readNetFromCaffe(gender_text, gender_model);
    }

}