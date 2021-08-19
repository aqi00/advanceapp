package com.example.threed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_gl_axis).setOnClickListener(this);
        findViewById(R.id.btn_gl_line).setOnClickListener(this);
        findViewById(R.id.btn_gl_globe).setOnClickListener(this);
        findViewById(R.id.btn_es_shader).setOnClickListener(this);
        findViewById(R.id.btn_es_matrix).setOnClickListener(this);
        findViewById(R.id.btn_es_texture).setOnClickListener(this);
        findViewById(R.id.btn_vulkan_cube).setOnClickListener(this);
        findViewById(R.id.btn_vulkan_radar).setOnClickListener(this);
        findViewById(R.id.btn_panorama).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_gl_axis) {
            startActivity(new Intent(this, GlAxisActivity.class));
        } else if (v.getId() == R.id.btn_gl_line) {
            startActivity(new Intent(this, GlLineActivity.class));
        } else if (v.getId() == R.id.btn_gl_globe) {
            startActivity(new Intent(this, GlGlobeActivity.class));
        } else if (v.getId() == R.id.btn_es_shader) {
            startActivity(new Intent(this, EsShaderActivity.class));
        } else if (v.getId() == R.id.btn_es_matrix) {
            startActivity(new Intent(this, EsMatrixActivity.class));
        } else if (v.getId() == R.id.btn_es_texture) {
            startActivity(new Intent(this, EsTextureActivity.class));
        } else if (v.getId() == R.id.btn_vulkan_cube) {
            startActivity(new Intent(this, VulkanCubeActivity.class));
        } else if (v.getId() == R.id.btn_vulkan_radar) {
            startActivity(new Intent(this, VulkanRadarActivity.class));
        } else if (v.getId() == R.id.btn_panorama) {
            startActivity(new Intent(this, PanoramaActivity.class));
        }
    }

}
