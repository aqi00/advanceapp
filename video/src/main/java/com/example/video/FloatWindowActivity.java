package com.example.video;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.video.service.StockService;
import com.example.video.util.AuthorityUtil;

public class FloatWindowActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float_window);
        findViewById(R.id.btn_stock_open).setOnClickListener(v -> {
            // 下面携带打开类型启动股指服务（打开股指悬浮窗）
            Intent intent = new Intent(this, StockService.class);
            intent.putExtra("type", StockService.OPEN);
            startService(intent);
        });
        findViewById(R.id.btn_stock_close).setOnClickListener(v -> {
            // 下面携带关闭类型启动股指服务（关闭股指悬浮窗）
            Intent intent = new Intent(this, StockService.class);
            intent.putExtra("type", StockService.CLOSE);
            startService(intent);
        });
        // AppOpsManager.OP_SYSTEM_ALERT_WINDOW是隐藏变量（值为24），不能直接引用
        if (!AuthorityUtil.checkOp(this, 24)) { // 未开启悬浮窗权限
            Toast.makeText(this, "请先给该应用开启悬浮窗权限", Toast.LENGTH_SHORT).show();
            // 跳到悬浮窗权限的设置页面
            AuthorityUtil.requestAlertWindowPermission(this);
        }
    }

    // 从悬浮窗权限的设置页面返回时触发
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AuthorityUtil.REQUEST_CODE) {}
    }

}
