package com.example.face;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.face.util.CodeAnalyzer;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@SuppressLint({"DefaultLocale", "SetTextI18n"})
public class VerifyCodeActivity extends AppCompatActivity {
    private final static String TAG = "VerifyCodeActivity";
    private final static String mCodeUrl = "http://192.168.1.5:8080/HttpServer/generateCode?char_type=%d&disturber_type=%d";
    private CheckBox ck_source; // 声明一个复选框对象
    private LinearLayout ll_local; // 声明一个线性视图对象
    private LinearLayout ll_network; // 声明一个线性视图对象
    private ImageView iv_code; // 声明一个图像视图对象
    private TextView tv_code; // 声明一个文本视图对象
    private boolean isGetting = false; // 是否正在获取验证码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);
        ck_source = findViewById(R.id.ck_source);
        ll_local = findViewById(R.id.ll_local);
        ll_network = findViewById(R.id.ll_network);
        iv_code = findViewById(R.id.iv_code);
        iv_code.setOnClickListener(v -> getImageCode(mCharType, mDisturberType));
        tv_code = findViewById(R.id.tv_code);
        ck_source.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ll_local.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            ll_network.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        initCodeSpinner(); // 初始化验证码图片下拉框
        initCharSpinner(); // 初始化字符类型下拉框
        initDisturbSpinner(); // 初始化干扰类型下拉框
    }

    // 初始化验证码图片下拉框
    private void initCodeSpinner() {
        ArrayAdapter<String> codeAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, codeDescArray);
        Spinner sp_code = findViewById(R.id.sp_code);
        sp_code.setPrompt("请选择验证码图片");
        sp_code.setAdapter(codeAdapter);
        sp_code.setOnItemSelectedListener(new CodeSelectedListener());
        sp_code.setSelection(0);
    }

    private String[] codeDescArray={"第一张验证码", "第二张验证码", "第三张验证码", "第四张验证码", "第五张验证码"};
    private int[] codeResArray={R.drawable.code1, R.drawable.code2, R.drawable.code3, R.drawable.code4, R.drawable.code5 };
    class CodeSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), codeResArray[arg2]);
            showVerifyCode(bitmap); // 识别并显示验证码数字
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    // 初始化字符类型下拉框
    private void initCharSpinner() {
        ArrayAdapter<String> charAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, charDescArray);
        Spinner sp_char = findViewById(R.id.sp_char);
        sp_char.setPrompt("请选择字符类型");
        sp_char.setAdapter(charAdapter);
        sp_char.setOnItemSelectedListener(new CharSelectedListener());
        sp_char.setSelection(0);
    }

    private int mCharType = 0; // 字符类型
    private String[] charDescArray={"纯数字", "字母加数字"};
    class CharSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mCharType = arg2;
            getImageCode(mCharType, mDisturberType); // 从服务器获取验证码图片
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    // 初始化干扰类型下拉框
    private void initDisturbSpinner() {
        ArrayAdapter<String> disturbAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, disturbDescArray);
        Spinner sp_disturb = findViewById(R.id.sp_disturb);
        sp_disturb.setPrompt("请选择干扰类型");
        sp_disturb.setAdapter(disturbAdapter);
        sp_disturb.setOnItemSelectedListener(new DisturbSelectedListener());
        sp_disturb.setSelection(0);
    }

    private int mDisturberType = 0; // 干扰类型
    private String[] disturbDescArray={"干扰点", "干扰线"};
    class DisturbSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mDisturberType = arg2;
            getImageCode(mCharType, mDisturberType); // 从服务器获取验证码图片
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    // 从服务器获取验证码图片
    private void getImageCode(int char_type, int disturber_type) {
        if (!ck_source.isChecked() || isGetting) {
            return;
        }
        isGetting = true;
        String imageUrl = String.format(mCodeUrl, char_type, disturber_type);
        OkHttpClient client = new OkHttpClient(); // 创建一个okhttp客户端对象
        // 创建一个GET方式的请求结构
        Request request = new Request.Builder().url(imageUrl).build();
        Call call = client.newCall(request); // 根据请求结构创建调用对象
        // 加入HTTP请求队列。异步调用，并设置接口应答的回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { // 请求失败
                isGetting = false;
                // 回到主线程操纵界面
                runOnUiThread(() -> tv_code.setText("下载网络图片报错："+e.getMessage()));
            }

            @Override
            public void onResponse(Call call, final Response response) { // 请求成功
                InputStream is = response.body().byteStream();
                // 从返回的输入流中解码获得位图数据
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                isGetting = false;
                // 回到主线程操纵界面
                runOnUiThread(() -> showVerifyCode(bitmap));
            }
        });
    }

    // 识别并显示验证码数字
    private void showVerifyCode(Bitmap bitmap) {
        String number = CodeAnalyzer.getNumber(bitmap); // 从验证码位图获取验证码数字
        iv_code.setImageBitmap(bitmap);
        tv_code.setText("自动识别得到的验证码是："+number);
    }

}