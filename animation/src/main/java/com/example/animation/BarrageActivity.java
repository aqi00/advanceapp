package com.example.animation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.animation.widget.BarrageView;

import java.util.Random;

public class BarrageActivity extends AppCompatActivity {
    private String[] mCommentArray = {"武夷山", "仙霞岭", "阿里山", "白云山", "九华山",
            "长白山", "峨眉山", "五台山", "太白山", "昆仑山",
            "六盘山", "乌蒙山", "井冈山", "武当山", "普陀山",
            "祁连山", "贺兰山", "太行山", "双鸭山", "五指山"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barrage);
        BarrageView bv_comment = findViewById(R.id.bv_comment);
        findViewById(R.id.btn_comment).setOnClickListener(v -> {
            String comment = mCommentArray[new Random().nextInt(20)];
            bv_comment.addComment(comment); // 给弹幕视图添加评论
        });
    }
}