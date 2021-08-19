package com.example.face;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app.zxing.camera.CameraManager;
import com.app.zxing.decoding.CaptureActivityHandler;
import com.app.zxing.decoding.InactivityTimer;
import com.app.zxing.view.ViewfinderView;
import com.example.face.util.QrcodeUtil;
import com.google.zxing.Result;

public class ScanQrcodeActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private final static String TAG = "ScanQrcodeActivity";
    private int CHOOSE_CODE = 3; // 只在相册挑选图片的请求码
    private ViewfinderView vv_finder; // 定义一个扫码视图对象
    private boolean hasSurface = false; // 是否创建了渲染表面
    private CaptureActivityHandler mHandler; // 捕捉图像的处理器
    private InactivityTimer mTimer; // 结束活动的计时器
    private MediaPlayer mPlayer; // 声明一个媒体播放器对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("二维码扫描/条形码扫描");
        TextView tv_option = findViewById(R.id.tv_option);
        tv_option.setVisibility(View.VISIBLE);
        tv_option.setText("相册");
        tv_option.setOnClickListener(v -> {
            // 创建一个内容获取动作的意图（准备跳到系统相册）
            Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
            albumIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); // 是否允许多选
            albumIntent.setType("image/*"); // 类型为图像
            startActivityForResult(albumIntent, CHOOSE_CODE); // 打开系统相册
        });
        CameraManager.init(getApplication(), CameraManager.QR_CODE);
        vv_finder = findViewById(R.id.vv_finder);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimer = new InactivityTimer(this);
        SurfaceView sv_scan = findViewById(R.id.sv_scan);
        // 从表面视图获取表面持有者
        SurfaceHolder surfaceHolder = sv_scan.getHolder();
        if (hasSurface) { // 已创建渲染表面
            initCamera(surfaceHolder); // 初始化相机
        } else { // 未创建渲染表面
            surfaceHolder.addCallback(this);
        }
        initBeepSound(); // 初始化哔哔音效
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.quitSynchronously();
            mHandler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimer.shutdown();
    }

    // 处理实时扫描获得的二维码信息
    public void handleDecode(Result result, Bitmap barcode) {
        mTimer.onActivity();
        beepAndVibrate(); // 震动手机并发出哔的一声
        // 读取二维码分析后的结果字符串
        String resultStr = result.getText();
        if (TextUtils.isEmpty(resultStr)) {
            Toast.makeText(this, "扫码失败或者结果为空", Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(this, "扫码结果为"+resultStr, Toast.LENGTH_SHORT).show();
            gotoResultPage(resultStr); // 跳到扫描结果页面
        }
    }

    // 跳到扫描结果页面
    private void gotoResultPage(String resultStr) {
        Intent intent = new Intent(this, ScanResultActivity.class);
        intent.putExtra("result", resultStr);
        startActivity(intent);
    }

    // 初始化相机
    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            if (mHandler == null) {
                mHandler = new CaptureActivityHandler(this, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 在渲染表面变更时触发
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    // 在渲染表面创建时触发
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    // 在渲染表面销毁时触发
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public ViewfinderView getViewfinderView() {
        return vv_finder;
    }

    public Handler getHandler() {
        return mHandler;
    }

    // 描绘扫码时的动态横杠
    public void drawViewfinder() {
        vv_finder.drawViewfinder();
    }

    // 初始化哔哔音效
    private void initBeepSound() {
        if (mPlayer == null) {
            // 设置当前页面的音频流类型
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mPlayer = new MediaPlayer(); // 创建一个媒体播放器
            // 设置媒体播放器的音频流类型
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 设置媒体播放器的播放结束监听器
            mPlayer.setOnCompletionListener(player -> player.seekTo(0));
            try (AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.beep)) {
                // 设置媒体播放器的媒体数据来源
                mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mPlayer.setVolume(0.1f, 0.1f); // 设置媒体播放器的左右声道音量
                mPlayer.prepare(); // 媒体播放器准备就绪
            } catch (Exception e) {
                e.printStackTrace();
                mPlayer = null;
            }
        }
    }

    // 震动手机并发出哔的一声
    private void beepAndVibrate() {
        if (mPlayer != null) {
            mPlayer.start(); // 媒体播放器开始播放
        }
        // 从系统服务中获取震动器
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200); // 命令震动器震动若干秒
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && requestCode == CHOOSE_CODE) { // 从相册返回
            if (intent.getData() != null) { // 从相册选择一张照片
                // 开启分线程从相册的图片Uri中解析二维码
                new Thread(() -> parserImage(intent.getData())).start();
            }
        }
    }

    // 从相册的图片Uri中解析二维码
    private void parserImage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            Result result = QrcodeUtil.parserQrcode(bitmap); // 从位图中解析二维码
            if (result == null) {
                Looper.prepare();
                Toast.makeText(this, "无法解析图片中的二维码", Toast.LENGTH_SHORT).show();
                Looper.loop();
            } else {
                String resultStr = QrcodeUtil.reEncode(result.toString());
                Log.d(TAG, "resultStr="+resultStr);
                gotoResultPage(resultStr); // 跳到扫描结果页面
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}