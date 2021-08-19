package com.example.video.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.video.util.BitmapUtil;
import com.example.video.util.DateUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraView extends SurfaceView {
    private static final String TAG = "CameraView";
    public static final int TYPE_SINGLE = 0; // 单拍
    public static final int TYPE_SHOOTING = 1; // 连拍
    private Context mContext; // 声明一个上下文对象
    private Camera mCamera; // 声明一个相机对象
    private SurfaceHolder mHolder; // 声明一个表面持有者对象
    private Size mPreviewSize; // 相机画面的尺寸
    private int mCameraType = CameraInfo.CAMERA_FACING_BACK; // 摄像头类型
    private OnStopListener mStopListener; // 停止拍摄监听器
    private OnPrepareListener mPrepareListener; // 准备就绪监听器

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public interface OnPrepareListener {
        void onPrepare(Size size);
    }

    public interface OnStopListener {
        void onStop(String result);
    }

    // 初始化相机
    public void initCamera(int camera_type, OnPrepareListener pl, OnStopListener sl) {
        mCameraType = camera_type;
        mPrepareListener = pl;
        mStopListener = sl;
        SurfaceHolder holder = getHolder(); // 获取表面视图的表面持有者
        holder.addCallback(mSurfaceCallback); // 给表面持有者添加表面变更监听器
        // 去除黑色背景。TRANSLUCENT半透明；TRANSPARENT透明
        holder.setFormat(PixelFormat.TRANSPARENT);
    }

    // 切换摄像头
    public void switchCamera(int camera_type) {
        closeCamera(); // 关闭相机
        mCameraType = camera_type;
        openCamera(); // 打开相机
    }

    // 关闭相机
    public void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview(); // 停止预览
            mCamera.lock(); // 锁定相机，即关闭相机（录像要用）
            mCamera.release(); // 释放相机资源
            mCamera = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.release(); // 释放录制器
            mMediaRecorder = null;
        }
    }

    // 打开相机
    public void openCamera() {
        mCamera = Camera.open(mCameraType); // 打开摄像头
        try {
            mCamera.setPreviewDisplay(mHolder); // 设置相机的预览界面
            // 获取所有支持的摄像尺寸列表
            List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
            for (Camera.Size size : sizeList) {
                if (size.height < 800 && 1.0*size.width/size.height==16.0/9.0) {
                    mPreviewSize  = new Size(size.width, size.height);
                    break;
                }
            }
            Log.d(TAG, "width=" + mPreviewSize.getWidth() + ", height=" + mPreviewSize.getHeight());
            // 获取相机的参数信息
            Camera.Parameters parameters = mCamera.getParameters();
            // 设置预览界面的尺寸
            parameters.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            // 设置图片的分辨率
            parameters.setPictureSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            parameters.setPictureFormat(ImageFormat.JPEG); // 设置图片的格式
            // 设置对焦模式为自动对焦。前置摄像头似乎无法自动对焦
            if (mCameraType == CameraInfo.CAMERA_FACING_BACK) {
                // FOCUS_MODE_AUTO只会自动对焦一次，若想连续对焦则需用下面的FOCUS_MODE_CONTINUOUS_PICTURE
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            mCamera.setParameters(parameters); // 设置相机的参数信息
            mCamera.setDisplayOrientation(90); // 设置相机的展示角度
            mCamera.startPreview(); // 开始预览画面
            mCamera.autoFocus(null); // 开始自动对焦
        } catch (Exception e) {
            e.printStackTrace();
            closeCamera(); // 遇到异常要关闭相机
        }
    }

    // 预览画面状态变更时的回调监听器
    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        // 在表面视图创建时触发
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mHolder = holder;
            openCamera(); // 打开相机
            if (mPrepareListener != null) {
                mPrepareListener.onPrepare(mPreviewSize);
            }
        }

        // 在表面视图变更时触发
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

        // 在表面视图销毁时触发
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            closeCamera(); // 关闭相机
        }
    };

    /*************************单拍开始***************************/
    // 执行拍照动作。外部调用该方法完成拍照
    public void takePicture() {
        // 命令相机拍摄一张照片
        mCamera.takePicture(mShutterCallback, null, mPictureCallback);
    }

    private String mPhotoPath; // 照片的保存路径
    // 获取照片的保存路径。外部调用该方法获得相片文件的路径
    public String getPhotoPath() {
        return mPhotoPath;
    }

    // 定义一个快门按下的回调监听器。可在此设置类似播放“咔嚓”声之类的操作，默认就是咔嚓。
    private ShutterCallback mShutterCallback = () -> Log.d(TAG, "onShutter...");

    // 定义一个获得拍照结果的回调监听器。可在此保存图片
    private PictureCallback mPictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken...");
            Bitmap raw = null;
            if (null != data) {
                // 原始图像数据data是字节数组，需要将其解析成位图
                raw = BitmapFactory.decodeByteArray(data, 0, data.length);
                mCamera.stopPreview(); // 停止预览画面
            }
            // 旋转位图
            Bitmap bitmap = BitmapUtil.getRotateBitmap(raw,
                    (mCameraType == CameraInfo.CAMERA_FACING_BACK) ? 90 : -90);
            // 获取本次拍摄的照片保存路径
            mPhotoPath = String.format("%s/%s.jpg",
                    mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                    DateUtil.getNowDateTime());
            // 保存照片文件
            BitmapUtil.saveImage(mPhotoPath, bitmap, mCameraType==CameraInfo.CAMERA_FACING_BACK);
            Log.d(TAG, "bitmap.size=" + (bitmap.getByteCount() / 1024) + "K" + ", path=" + mPhotoPath);
            BitmapUtil.notifyPhotoAlbum(mContext, mPhotoPath); // 通知相册来了张新图片
            mCamera.startPreview(); // 再次进入预览画面
            if (mStopListener != null) {
                mStopListener.onStop("已完成拍摄，照片保存路径为"+mPhotoPath);
            }
        }
    };
    /*************************单拍结束***************************/

    /*************************连拍开始***************************/
    private boolean isShooting = false; // 是否正在连拍
    private ArrayList<String> mShootingList = new ArrayList<>(); // 连拍的相片保存路径列表

    // 执行连拍动作。外部调用该方法完成连拍
    public void takeShooting() {
        // 设置相机的预览监听器。注意这里的setPreviewCallback给连拍功能使用
        mCamera.setPreviewCallback(mPreviewCallback);
        mShootingList = new ArrayList<>();
        isShooting = true;
    }

    // 获取连拍的相片保存路径列表。外部调用该方法获得连拍结果相片的路径列表
    public ArrayList<String> getShootingList() {
        return mShootingList;
    }

    private long mLastTime = 0; // 上次拍摄时间
    // 定义一个画面预览的回调监听器。在此可捕获动态的连续图片
    private PreviewCallback mPreviewCallback = new PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (!isShooting) {
                return;
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime-mLastTime < 500) {
                return;
            }
            Log.d(TAG, "nowTime="+nowTime+", mLastTime="+mLastTime);
            mLastTime = nowTime;
            Log.d(TAG, "isShooting=" + isShooting + ", shooting_num=" + mShootingList.size()+", getPreviewFormat="+camera.getParameters().getPreviewFormat());
            Rect rect = new Rect(0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight());
            // 创建一个YUV格式的图像对象
            YuvImage yuvImg = new YuvImage(data, camera.getParameters().getPreviewFormat(),
                    mPreviewSize.getWidth(), mPreviewSize.getHeight(), null);
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                yuvImg.compressToJpeg(rect, 80, bos);
                // 从字节数组中解析出位图数据
                Bitmap raw = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.size());
                // 旋转位图
                Bitmap bitmap = BitmapUtil.getRotateBitmap(raw,
                        (mCameraType == CameraInfo.CAMERA_FACING_BACK) ? 90 : -90);
                // 获取本次拍摄的照片保存路径
                String path = String.format("%s/%s.jpg",
                        mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                        DateUtil.getFullDateTime());
                // 把位图保存为图片文件
                BitmapUtil.saveImage(path, bitmap, mCameraType==CameraInfo.CAMERA_FACING_BACK);
                Log.d(TAG, "bitmap.size=" + (bitmap.getByteCount() / 1024) + "K" + ", path=" + path);
                BitmapUtil.notifyPhotoAlbum(mContext, path); // 通知相册来了张新图片
                camera.startPreview(); // 再次进入预览画面
                mShootingList.add(path);
                if (mShootingList.size() > 8) {  // 每次连拍9张
                    isShooting = false;
                    mCamera.setPreviewCallback(null); // 将预览监听器置空
                    if (mStopListener != null) {
                        mStopListener.onStop("已完成连拍，完成的照片数量为"+mShootingList.size());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    /*************************连拍结束***************************/

    /*************************录像开始***************************/
    private String mVideoPath; // 视频保存路径
    private static final int MAX_RECORD_TIME = 15; // 最大录制时长，默认15秒
    private MediaRecorder mMediaRecorder; // 声明一个媒体录制器对象
    // 获取视频的保存路径
    public String getVideoPath() {
        return mVideoPath;
    }

    // 开始录像
    public void startRecord() {
        try {
            Log.d(TAG, "prepareMediaRecorder");
            mCamera.unlock(); // 解锁相机，即打开相机（录像需要）
            Log.d(TAG, "getWidth="+mPreviewSize.getWidth()+", getHeight="+mPreviewSize.getHeight());
            mMediaRecorder = new MediaRecorder(); // 创建一个媒体录制器
            mMediaRecorder.setCamera(mCamera); // 设置媒体录制器的摄像头
            mMediaRecorder.setPreviewDisplay(mHolder.getSurface()); // 设置媒体录制器的预览界面
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            // setProfile囊括了setOutputFormat、setVideoEncoder、setAudioEncoder、setVideoSize、setVideoEncodingBitRate等参数设置
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
            // 设置媒体的最大录制时长
            mMediaRecorder.setMaxDuration(MAX_RECORD_TIME * 1000);
            mMediaRecorder.setOnInfoListener((mr, what, extra) -> {
                // 录制达到最大时长，或者达到文件大小限制，都停止录制
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
                        || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                    stopRecord();// 停止录像
                }
            });
            // 判断是不是前置摄像头,是的话需要旋转对应的角度
            int degree = (mCameraType == CameraInfo.CAMERA_FACING_BACK) ? 90 : 270;
            mMediaRecorder.setOrientationHint(degree);
            mVideoPath = String.format("%s/%s.mp4",
                    mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                    DateUtil.getNowDateTime());
            mMediaRecorder.setOutputFile(mVideoPath); // 设置输出媒体的文件路径
            mMediaRecorder.prepare(); // 媒体录制器准备就绪
            mMediaRecorder.start(); // 媒体录制器开始录像
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 停止录像
    public void stopRecord() {
        try {
            mMediaRecorder.setPreviewDisplay(null); // 预览界面置空
            mMediaRecorder.stop(); // 媒体录制器停止录像
            mMediaRecorder.release(); // 释放媒体录制器
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeCamera(); // 关闭相机
        openCamera(); // 重新打开相机
        if (mStopListener != null) {
            mStopListener.onStop("录制完成的视频路径为"+mVideoPath);
        }
    }
    /*************************录像结束***************************/

}