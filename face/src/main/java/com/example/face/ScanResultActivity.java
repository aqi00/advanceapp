package com.example.face;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class ScanResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        TextView tv_result = findViewById(R.id.tv_result);
        // 获取扫码页面传来的结果字符串
        String result = getIntent().getStringExtra("result");
        tv_result.setText("扫码结果为：" + result);
    }

}
