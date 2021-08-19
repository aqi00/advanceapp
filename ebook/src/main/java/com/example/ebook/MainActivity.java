package com.example.ebook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_bezier_curve).setOnClickListener(this);
        findViewById(R.id.btn_bezier_wave).setOnClickListener(this);
        findViewById(R.id.btn_bezier_gift).setOnClickListener(this);
        findViewById(R.id.btn_pdf_render).setOnClickListener(this);
        findViewById(R.id.btn_pdf_slide).setOnClickListener(this);
        findViewById(R.id.btn_pdf_curve).setOnClickListener(this);
        findViewById(R.id.btn_jni_cpu).setOnClickListener(this);
        findViewById(R.id.btn_jni_secret).setOnClickListener(this);
        findViewById(R.id.btn_ebook_reader).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_bezier_curve) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                Toast.makeText(this, "播放WebP动图需要Android9及更高版本", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, BezierCurveActivity.class));
        } else if (v.getId() == R.id.btn_bezier_wave) {
            startActivity(new Intent(this, BezierWaveActivity.class));
        } else if (v.getId() == R.id.btn_bezier_gift) {
            startActivity(new Intent(this, BezierGiftActivity.class));
        } else if (v.getId() == R.id.btn_pdf_render) {
            startActivity(new Intent(this, PdfRenderActivity.class));
        } else if (v.getId() == R.id.btn_pdf_slide) {
            startActivity(new Intent(this, PdfSlideActivity.class));
        } else if (v.getId() == R.id.btn_pdf_curve) {
            startActivity(new Intent(this, PdfCurveActivity.class));
        } else if (v.getId() == R.id.btn_jni_cpu) {
            startActivity(new Intent(this, JniCpuActivity.class));
        } else if (v.getId() == R.id.btn_jni_secret) {
            startActivity(new Intent(this, JniSecretActivity.class));
        } else if (v.getId() == R.id.btn_ebook_reader) {
            startActivity(new Intent(this, EbookReaderActivity.class));
        }
    }
}
