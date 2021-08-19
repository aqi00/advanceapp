package com.example.ebook;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ebook.widget.RewardView;

public class BezierGiftActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bezier_gift);
        RewardView rv_gift = findViewById(R.id.rv_gift);
        // 每次点击爱心图标，都往打赏视图上面添加礼物的漂移动画
        findViewById(R.id.iv_reward).setOnClickListener(v -> rv_gift.addGiftView());
    }
}