package com.example.network;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_thread_ui).setOnClickListener(this);
        findViewById(R.id.btn_thread_executor).setOnClickListener(this);
        findViewById(R.id.btn_work_manager).setOnClickListener(this);
        findViewById(R.id.btn_okhttp_call).setOnClickListener(this);
        findViewById(R.id.btn_okhttp_download).setOnClickListener(this);
        findViewById(R.id.btn_okhttp_upload).setOnClickListener(this);
        findViewById(R.id.btn_pull_refresh).setOnClickListener(this);
        findViewById(R.id.btn_web_socket).setOnClickListener(this);
        findViewById(R.id.btn_socketio_text).setOnClickListener(this);
        findViewById(R.id.btn_socketio_image).setOnClickListener(this);
        findViewById(R.id.btn_we_login).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_thread_ui) {
            startActivity(new Intent(this, ThreadUiActivity.class));
        } else if (v.getId() == R.id.btn_thread_executor) {
            startActivity(new Intent(this, ThreadExecutorActivity.class));
        } else if (v.getId() == R.id.btn_work_manager) {
            startActivity(new Intent(this, WorkManagerActivity.class));
        } else if (v.getId() == R.id.btn_okhttp_call) {
            startActivity(new Intent(this, OkhttpCallActivity.class));
        } else if (v.getId() == R.id.btn_okhttp_download) {
            startActivity(new Intent(this, OkhttpDownloadActivity.class));
        } else if (v.getId() == R.id.btn_okhttp_upload) {
            startActivity(new Intent(this, OkhttpUploadActivity.class));
        } else if (v.getId() == R.id.btn_pull_refresh) {
            startActivity(new Intent(this, PullRefreshActivity.class));
        } else if (v.getId() == R.id.btn_web_socket) {
            startActivity(new Intent(this, WebSocketActivity.class));
        } else if (v.getId() == R.id.btn_socketio_text) {
            startActivity(new Intent(this, SocketioTextActivity.class));
        } else if (v.getId() == R.id.btn_socketio_image) {
            startActivity(new Intent(this, SocketioImageActivity.class));
        } else if (v.getId() == R.id.btn_we_login) {
            startActivity(new Intent(this, WeLoginActivity.class));
        }
    }
}
