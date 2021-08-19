package com.example.video.widget;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.core.app.ActivityCompat;

import com.example.video.util.BitmapUtil;
import com.example.video.util.DateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Camera2View extends TextureView {
    private static final String TAG = "Camera2View";
    public static final int TYPE_SINGLE = 0; // 单拍
    public static final int TYPE_SHOOTING = 1; // 连拍
    private Context mContext; // 声明一个上下文对象
    private Handler mHandler; // 声明一个处理器对象
    private CaptureRequest.Builder mPreviewBuilder; // 声明一个拍照请求构建器对象
    private CameraCaptureSession mCameraSession; // 声明一个相机拍照会话对象
    private CameraDevice mCameraDevice; // 声明一个相机设备对象
    private ImageReader mImageReader; // 声明一个图像读取器对象
    private int mTakeType = TYPE_SINGLE; // 拍摄类型。0为单拍，1为连拍
    private Size mPreviewSize; // 预览画面的尺寸
    private int mCameraType = CameraCharacteristics.LENS_FACING_BACK; // 摄像头类型
    private OnStopListener mStopListener; // 停止拍摄监听器
    private OnPrepareListener mPrepareListener; // 准备就绪监听器

    public Camera2View(Context context) {
        this(context, null);
    }

    public Camera2View(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        HandlerThread cameraThread = new HandlerThread("Camera2Thread");
        cameraThread.start();
        mHandler = new Handler(cameraThread.getLooper());
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
        setSurfaceTextureListener(mTextureListener); // 设置表面纹理变更监听器
    }

    // 切换摄像头
    public void switchCamera(int camera_type) {
        closeCamera(); // 关闭相机
        mCameraType = camera_type;
        openCamera(); // 打开相机
    }

    // 关闭相机
    public void closeCamera() {
        if (null != mCameraSession) {
            mCameraSession.close(); // 关闭相机拍摄会话
            mCameraSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close(); // 关闭相机设备
            mCameraDevice = null;
        }
        if (null != mImageReader) {
            mImageReader.close(); // 关闭图像读取器
            mImageReader = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.release(); // 释放录制器
            mMediaRecorder = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    // 打开相机
    public void openCamera() {
        // 从系统服务中获取相机管理器
        CameraManager cm = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdArray = cm.getCameraIdList(); // 获取摄像头数组
            String cameraId = mCameraType==CameraCharacteristics.LENS_FACING_BACK ? cameraIdArray[0] : cameraIdArray[1];
            Log.d(TAG, "mCameraType=" + mCameraType + ", cameraId=" + cameraId);
            // 获取可用相机设备列表
            CameraCharacteristics cc = cm.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    (lhs, rhs) -> Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth()));
            // 获取预览画面的尺寸
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            for (Size size : map.getOutputSizes(SurfaceTexture.class)) {
                Log.d(TAG, "getOutputSizes" + size.getWidth() + "----" + size.getHeight());
                if (size.getHeight() < 800 && 1.0*size.getWidth()/size.getHeight()==16.0/9.0) {
                    mPreviewSize  = size;
                    break;
                }
            }
            // 创建一个JPEG格式的图像读取器
            mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 2);
            // 设置图像读取器的图像可用监听器，一旦捕捉到图像数据就会触发监听器的onImageAvailable方法
            mImageReader.setOnImageAvailableListener(mImageListener, mHandler);
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cm.openCamera(cameraId, mDeviceCallback, mHandler); // 开启摄像头
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 定义一个表面纹理变更监听器。TextureView准备就绪后，立即开启相机
    private SurfaceTextureListener mTextureListener = new SurfaceTextureListener() {
        // 在纹理表面可用时触发
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "width="+width+", height="+height);
            openCamera(); // 打开相机
            if (mPrepareListener != null) {
                mPrepareListener.onPrepare(mPreviewSize);
            }
        }

        // 在纹理表面的尺寸发生改变时触发
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

        // 在纹理表面销毁时触发
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            closeCamera(); // 关闭相机
            return true;
        }

        // 在纹理表面更新时触发
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
    };

    // 创建相机预览会话
    private void createPreviewSession() {
        // 获取纹理视图的表面纹理
        SurfaceTexture texture = getSurfaceTexture();
        // 设置表面纹理的默认缓存尺寸
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture); // 创建一个该表面纹理的表面对象
        try {
            // 创建相机设备的预览捕捉请求
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewBuilder.addTarget(surface); // 把纹理视图添加到预览目标
            // FLASH_MODE_OFF表示关闭闪光灯，FLASH_MODE_TORCH表示开启闪光灯
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            int rotateDegree = mCameraType==CameraCharacteristics.LENS_FACING_BACK ? 90 : 270;
            // 设置照片的方向
            mPreviewBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotateDegree);
            // 创建一个相机捕捉会话。此时预览画面既显示于纹理视图，也输出到图像阅读器
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    mSessionCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 相机准备就绪后，开启捕捉影像的会话
    private CameraDevice.StateCallback mDeviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            createPreviewSession(); // 创建相机预览会话
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close(); // 关闭相机设备
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close(); // 关闭相机设备
            mCameraDevice = null;
        }
    };

    // 影像配置就绪后，将预览画面呈现到手机屏幕上
    private CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                Log.d(TAG, "onConfigured");
                mCameraSession = session;
                // 设置连拍请求。此时预览画面只会发给手机屏幕
                mCameraSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {}
    };

    // 一旦有图像数据生成，立刻触发onImageAvailable事件
    private OnImageAvailableListener mImageListener = new OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Log.d(TAG, "onImageAvailable");
            Image image = imageReader.acquireNextImage(); // 获得下一张图像缓存
            // 获取本次拍摄的照片保存路径
            String path = String.format("%s/%s.jpg",
                    mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                    DateUtil.getFullDateTime());
            if (mTakeType == TYPE_SINGLE) { // 单拍
                BitmapUtil.saveImage(path, image.getPlanes()[0].getBuffer(), mCameraType==CameraCharacteristics.LENS_FACING_BACK); // 保存图片文件
                image.close(); // 关闭图像缓存
                mPhotoPath = path;
                if (mStopListener != null) {
                    mStopListener.onStop("已完成拍摄，照片保存路径为"+mPhotoPath);
                }
            } else if (mTakeType == TYPE_SHOOTING) { // 连拍每张照片的间隔大于900毫秒
                long nowTime = System.currentTimeMillis();
                Log.d(TAG, "nowTime="+nowTime+", mLastTime="+mLastTime);
                if (nowTime-mLastTime < 1500) {
                    image.close(); // 关闭图像缓存
                    return;
                }
                mLastTime = nowTime;
                BitmapUtil.saveImage(path, image.getPlanes()[0].getBuffer(), mCameraType==CameraCharacteristics.LENS_FACING_BACK); // 保存图片文件
                image.close(); // 关闭图像缓存
                mShootingList.add(path);
                if (mShootingList.size() >= 9) { // 已拍摄照片达到9张就停止连拍
                    stopShooting();
                }
            }
            Log.d(TAG, "完成保存图片 path=" + path);
            BitmapUtil.notifyPhotoAlbum(mContext, path); // 通知相册来了张新图片
        }
    };

    /*************************单拍开始***************************/
    private String mPhotoPath; // 单拍的相片保存路径
    // 获取照片的保存路径
    public String getPhotoPath() {
        return mPhotoPath;
    }

    // 执行拍照动作
    public void takePicture() {
        Log.d(TAG, "正在拍照");
        mTakeType = TYPE_SINGLE;
        try {
            // 创建相机设备的预览捕捉请求
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 把图像读取器添加到预览目标
            builder.addTarget(mImageReader.getSurface());
            // FLASH_MODE_OFF表示关闭闪光灯，FLASH_MODE_TORCH表示开启闪光灯
            builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            // 设置照片的方向
            int degree = (mCameraType == CameraCharacteristics.LENS_FACING_BACK) ? 90 : 270;
            builder.set(CaptureRequest.JPEG_ORIENTATION, degree);
            // 拍照会话开始捕捉相片
            mCameraSession.capture(builder.build(), null, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /*************************单拍结束***************************/

    /*************************连拍开始***************************/
    private long mLastTime = 0; // 上次拍摄时间
    private ArrayList<String> mShootingList = new ArrayList<>(); // 连拍的相片路径列表
    // 获取连拍的相片保存路径列表
    public ArrayList<String> getShootingList() {
        Log.d(TAG, "mShootingList.size()=" + mShootingList.size());
        return mShootingList;
    }

    // 开始连拍
    public void startShooting(int duration) {
        Log.d(TAG, "正在连拍");
        mTakeType = TYPE_SHOOTING;
        mShootingList = new ArrayList<>();
        try {
            mCameraSession.stopRepeating(); // 停止连拍
            // 把图像读取器添加到预览目标
            mPreviewBuilder.addTarget(mImageReader.getSurface());
            // 设置连拍请求。此时预览画面会同时发给手机屏幕和图像读取器
            mCameraSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
            // duration小等于0时，表示持续连拍，此时外部要调用stopShooting方法来结束连拍
            if (duration > 0) {
                // 延迟若干秒后启动拍摄停止任务
                mHandler.postDelayed(() -> stopShooting(), duration);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 停止连拍
    public void stopShooting() {
        try {
            mCameraSession.stopRepeating(); // 停止连拍
            // 移除图像读取器的预览目标
            mPreviewBuilder.removeTarget(mImageReader.getSurface());
            // 设置连拍请求。此时预览画面只会发给手机屏幕
            mCameraSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (mStopListener != null) {
            mHandler.postDelayed(() -> {
                mStopListener.onStop("已完成连拍，完成的照片数量为"+mShootingList.size());
            }, 500);
        }
    }
    /*************************连拍结束***************************/

    /*************************录像开始***************************/
    private String mVideoPath; // 视频保存路径
    private int MAX_RECORD_TIME = 15; // 最大录制时长，默认15秒
    private boolean IS_REOPEN = true; // 是否重新打开相机
    private MediaRecorder mMediaRecorder; // 声明一个媒体录制器对象
    // 获取视频的保存路径
    public String getVideoPath() {
        return mVideoPath;
    }

    // 开始录像
    public void startRecord(int max_record_time, boolean is_reopen) {
        MAX_RECORD_TIME = max_record_time;
        IS_REOPEN = is_reopen;
        try {
            Log.d(TAG, "prepareMediaRecorder");
            mMediaRecorder = new MediaRecorder(); // 创建一个媒体录制器
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            // setProfile囊括了setOutputFormat、setVideoEncoder、setAudioEncoder、setVideoSize、setVideoEncodingBitRate等参数设置
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
            // 设置媒体的最大录制时长
            mMediaRecorder.setMaxDuration(MAX_RECORD_TIME * 1000);
            mMediaRecorder.setOnInfoListener((mr, what, extra) -> {
                // 录制达到最大时长，或者达到文件大小限制，都停止录制
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
                        || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                    stopRecord(); // 停止录像
                }
            });

            // 无论前置摄像头还是后置摄像头都要旋转对应的角度
            int degree = (mCameraType == CameraCharacteristics.LENS_FACING_BACK) ? 90 : 270;
            mMediaRecorder.setOrientationHint(degree); // 设置输出视频的播放方向
            mVideoPath = String.format("%s/%s.mp4",
                    mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                    DateUtil.getNowDateTime());
            mMediaRecorder.setOutputFile(mVideoPath); // 设置输出媒体的文件路径
            mMediaRecorder.prepare(); // 媒体录制器准备就绪

            SurfaceTexture texture = getSurfaceTexture(); // 获得纹理视图的表面纹理
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            // 创建相机设备的录像捕捉请求
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            // 根据表面纹理创建表面对象
            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface); // 把纹理视图添加到预览目标
            // 获取媒体录制器的表面对象
            Surface recorderSurface = mMediaRecorder.getSurface();
            // 把媒体录制器添加到预览目标
            mPreviewBuilder.addTarget(recorderSurface);
            // 创建一个相机捕捉会话。此时预览画面既显示于纹理视图，也输出到媒体录制器
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recorderSurface),
                    mSessionCallback, null);

            mMediaRecorder.start(); // 媒体录制器开始录像
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 停止录像
    public void stopRecord() {
        try {
            mMediaRecorder.stop(); // 媒体录制器停止录像
            mMediaRecorder.release(); // 释放媒体录制器
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeCamera(); // 关闭相机
        if (IS_REOPEN) {
            openCamera(); // 重新打开相机
        }
        if (mStopListener != null) {
            mStopListener.onStop("录制完成的视频路径为"+mVideoPath);
        }
    }
    /*************************录像结束***************************/

}