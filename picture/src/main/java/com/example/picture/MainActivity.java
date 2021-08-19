package com.example.picture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_card_view).setOnClickListener(this);
        findViewById(R.id.btn_image_decorate).setOnClickListener(this);
        findViewById(R.id.btn_image_ripple).setOnClickListener(this);
        findViewById(R.id.btn_bitmap_pixel).setOnClickListener(this);
        findViewById(R.id.btn_bitmap_cut).setOnClickListener(this);
        findViewById(R.id.btn_bitmap_change).setOnClickListener(this);
        findViewById(R.id.btn_drawable_convert).setOnClickListener(this);
        findViewById(R.id.btn_drawable_cut).setOnClickListener(this);
        findViewById(R.id.btn_drawable_text).setOnClickListener(this);
        findViewById(R.id.btn_drawable_icon).setOnClickListener(this);
        findViewById(R.id.btn_purikura).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_card_view) {
            startActivity(new Intent(this, CardViewActivity.class));
        } else if (v.getId() == R.id.btn_image_decorate) {
            startActivity(new Intent(this, ImageDecorateActivity.class));
        } else if (v.getId() == R.id.btn_image_ripple) {
            startActivity(new Intent(this, ImageRippleActivity.class));
        } else if (v.getId() == R.id.btn_bitmap_pixel) {
            startActivity(new Intent(this, BitmapPixelActivity.class));
        } else if (v.getId() == R.id.btn_bitmap_cut) {
            startActivity(new Intent(this, BitmapCutActivity.class));
        } else if (v.getId() == R.id.btn_bitmap_change) {
            startActivity(new Intent(this, BitmapChangeActivity.class));
        } else if (v.getId() == R.id.btn_drawable_convert) {
            startActivity(new Intent(this, DrawableConvertActivity.class));
        } else if (v.getId() == R.id.btn_drawable_cut) {
            startActivity(new Intent(this, DrawableCutActivity.class));
        } else if (v.getId() == R.id.btn_drawable_text) {
            startActivity(new Intent(this, DrawableTextActivity.class));
        } else if (v.getId() == R.id.btn_drawable_icon) {
            startActivity(new Intent(this, DrawableIconActivity.class));
        } else if (v.getId() == R.id.btn_purikura) {
            startActivity(new Intent(this, PurikuraActivity.class));
        }
    }

}
