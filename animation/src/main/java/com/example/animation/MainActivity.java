package com.example.animation;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_frame_anim).setOnClickListener(this);
        findViewById(R.id.btn_gif).setOnClickListener(this);
        findViewById(R.id.btn_fade_anim).setOnClickListener(this);
        findViewById(R.id.btn_tween_anim).setOnClickListener(this);
        findViewById(R.id.btn_swing_anim).setOnClickListener(this);
        findViewById(R.id.btn_anim_set).setOnClickListener(this);
        findViewById(R.id.btn_object_anim).setOnClickListener(this);
        findViewById(R.id.btn_object_group).setOnClickListener(this);
        findViewById(R.id.btn_interpolator).setOnClickListener(this);
        findViewById(R.id.btn_barrage).setOnClickListener(this);
        findViewById(R.id.btn_draw_layer).setOnClickListener(this);
        findViewById(R.id.btn_shutter).setOnClickListener(this);
        findViewById(R.id.btn_mosaic).setOnClickListener(this);
        findViewById(R.id.btn_scroller).setOnClickListener(this);
        findViewById(R.id.btn_yingji).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_frame_anim) {
            startActivity(new Intent(this, FrameAnimActivity.class));
        } else if (v.getId() == R.id.btn_gif) {
            startActivity(new Intent(this, GifActivity.class));
        } else if (v.getId() == R.id.btn_fade_anim) {
            startActivity(new Intent(this, FadeAnimActivity.class));
        } else if (v.getId() == R.id.btn_tween_anim) {
            startActivity(new Intent(this, TweenAnimActivity.class));
        } else if (v.getId() == R.id.btn_swing_anim) {
            startActivity(new Intent(this, SwingAnimActivity.class));
        } else if (v.getId() == R.id.btn_anim_set) {
            startActivity(new Intent(this, AnimSetActivity.class));
        } else if (v.getId() == R.id.btn_object_anim) {
            startActivity(new Intent(this, ObjectAnimActivity.class));
        } else if (v.getId() == R.id.btn_object_group) {
            startActivity(new Intent(this, ObjectGroupActivity.class));
        } else if (v.getId() == R.id.btn_interpolator) {
            startActivity(new Intent(this, InterpolatorActivity.class));
        } else if (v.getId() == R.id.btn_barrage) {
            startActivity(new Intent(this, BarrageActivity.class));
        } else if (v.getId() == R.id.btn_draw_layer) {
            startActivity(new Intent(this, DrawLayerActivity.class));
        } else if (v.getId() == R.id.btn_shutter) {
            startActivity(new Intent(this, ShutterActivity.class));
        } else if (v.getId() == R.id.btn_mosaic) {
            startActivity(new Intent(this, MosaicActivity.class));
        } else if (v.getId() == R.id.btn_scroller) {
            startActivity(new Intent(this, ScrollerActivity.class));
        } else if (v.getId() == R.id.btn_yingji) {
            startActivity(new Intent(this, YingjiActivity.class));
        }
    }

}
