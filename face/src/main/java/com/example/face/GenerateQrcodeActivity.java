package com.example.face;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.face.util.BitmapUtil;
import com.example.face.util.DateUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

public class GenerateQrcodeActivity extends AppCompatActivity {
    private final static String TAG = "GenerateQrcodeActivity";
    private EditText et_content; // 声明一个编辑框对象
    private ImageView iv_qrcode; // 声明一个图像视图对象
    private Bitmap mBitmap; // 声明一个位图对象
    private ErrorCorrectionLevel mErrorRate; // 容错率

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qrcode);
        et_content = findViewById(R.id.et_content);
        iv_qrcode = findViewById(R.id.iv_qrcode);
        findViewById(R.id.btn_generate).setOnClickListener(v -> {
            String content = et_content.getText().toString();
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(this, "请先输入原始文本", Toast.LENGTH_SHORT).show();
                return;
            }
            // 生成原始文本对应的二维码位图
            mBitmap = createQrcodeBitmap(content, mErrorRate);
            iv_qrcode.setImageBitmap(mBitmap); // 设置图像视图的位图对象
        });
        findViewById(R.id.btn_save).setOnClickListener(v -> {
            if (mBitmap == null) {
                Toast.makeText(this, "请先生成二维码图片", Toast.LENGTH_SHORT).show();
                return;
            }
            // 生成图片文件的保存路径
            String path = String.format("%s/%s.jpg",
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                    DateUtil.getNowDateTime());
            BitmapUtil.saveImage(path, mBitmap); // 把位图保存为图片文件
            BitmapUtil.notifyPhotoAlbum(this, path); // 通知相册来了张新图片
            Toast.makeText(this, "成功保存二维码图片：" + path, Toast.LENGTH_SHORT).show();
        });
        initErrorSpinner(); // 初始化容错率下拉框
    }

    // 初始化容错率下拉框
    private void initErrorSpinner() {
        ArrayAdapter<String> errorAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, errorNameArray);
        Spinner sp_error = findViewById(R.id.sp_error);
        sp_error.setPrompt("请选择容错率");
        sp_error.setAdapter(errorAdapter);
        sp_error.setOnItemSelectedListener(new ErrorSelectedListener());
        sp_error.setSelection(0);
    }

    private String[] errorNameArray = {"30%", "25%", "15%", "7%"};
    private ErrorCorrectionLevel[] erroLevelArray = {ErrorCorrectionLevel.H,
            ErrorCorrectionLevel.Q, ErrorCorrectionLevel.M, ErrorCorrectionLevel.L};
    class ErrorSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mErrorRate = erroLevelArray[arg2];
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    // 生成原始文本对应的二维码位图
    private Bitmap createQrcodeBitmap(String content, ErrorCorrectionLevel errorRate) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        int width = content.length()*6; // 二维码图片的宽度
        int height = width; // 二维码图片的高度
        int margin = width / 20; // 二维码图片的空白边距
        Log.d(TAG, "content="+content+",width="+width+",height="+height+",margin="+margin+",errorRate="+errorRate.name());
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, margin); // 设置空白边距
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // 设置字符编码格式
        hints.put(EncodeHintType.ERROR_CORRECTION, errorRate); // 设置容错率
        try {
            // 根据配置参数生成位矩阵对象
            BitMatrix bitMatrix = new QRCodeWriter().encode(content,
                    BarcodeFormat.QR_CODE, width, height, hints);
            // 创建像素数组，并根据位矩阵对象为数组元素赋色值
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) { // 返回true表示黑色色块
                        pixels[y * width + x] = Color.BLACK;
                    } else { // 返回false表示白色色块
                        pixels[y * width + x] = Color.WHITE;
                    }
                }
            }
            // 创建位图对象，并根据像素数组设置每个像素的色值
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            if (bitmap.getWidth() < 300) { // 图片太小的话，要放大图片
                bitmap = BitmapUtil.getScaleBitmap(bitmap, 300.0/bitmap.getWidth());
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}