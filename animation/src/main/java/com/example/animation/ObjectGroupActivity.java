package com.example.animation;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class ObjectGroupActivity extends AppCompatActivity implements OnClickListener, AnimatorListener {
    private ImageView iv_object_group; // 声明一个图像视图对象
    private AnimatorSet animSet; // 声明一个属性动画组合对象
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_group);
        iv_object_group = findViewById(R.id.iv_object_group);
        iv_object_group.setOnClickListener(this);
        initObjectAnim(); // 初始化属性动画
    }

    // 初始化属性动画
    private void initObjectAnim() {
        // 构造一个在横轴上平移的属性动画
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(iv_object_group, "translationX", 0f, 100f);
        // 构造一个在透明度上变化的属性动画
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(iv_object_group, "alpha", 1f, 0.1f, 1f, 0.5f, 1f);
        // 构造一个围绕中心点旋转的属性动画
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(iv_object_group, "rotation", 0f, 360f);
        // 构造一个在纵轴上缩放的属性动画
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(iv_object_group, "scaleY", 1f, 0.5f, 1f);
        // 构造一个在横轴上平移的属性动画
        ObjectAnimator anim5 = ObjectAnimator.ofFloat(iv_object_group, "translationX", 100f, 0f);
        animSet = new AnimatorSet(); // 创建一个属性动画组合
        // 把指定的属性动画添加到属性动画组合
        AnimatorSet.Builder builder = animSet.play(anim2);
        // 动画播放顺序为：先执行anim1，再一起执行anim2、anim3、anim3，最后执行anim5
        builder.with(anim3).with(anim4).after(anim1).before(anim5);
        animSet.setDuration(4500); // 设置动画的播放时长
        animSet.start(); // 开始播放属性动画
        animSet.addListener(this); // 给属性动画添加动画事件监听器
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_object_group) {
            if (animSet.isStarted()) { // 属性动画已经播放过了
                if (animSet.isRunning()) { // 属性动画正在播放
                    if (!isPaused) {
                        animSet.pause(); // 暂停播放属性动画
                    } else {
                        animSet.resume(); // 恢复播放属性动画
                    }
                    isPaused = !isPaused;
                } else { // 属性动画不在播放
                    animSet.start(); // 开始播放属性动画
                }
            } else { // 属性动画尚未开始播放
                animSet.start(); // 开始播放属性动画
            }
        }
    }

    // 在属性动画开始播放时触发
    @Override
    public void onAnimationStart(Animator animation) {}

    // 在属性动画结束播放时触发
    @Override
    public void onAnimationEnd(Animator animation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            animSet.setCurrentPlayTime(0); // 设置当前播放的时间点
            animSet.reverse(); // 从动画尾巴开始倒播至setCurrentPlayTime设置的时间点
        }
    }

    // 在属性动画取消播放时触发
    @Override
    public void onAnimationCancel(Animator animation) {}

    // 在属性动画重复播放时触发
    @Override
    public void onAnimationRepeat(Animator animation) {}
}
