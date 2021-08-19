package com.example.threed;

import android.app.NativeActivity;
import android.os.Bundle;

public class VulkanCubeActivity extends NativeActivity {

    static {
        System.loadLibrary("vulkan_sample"); // 加载so库
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
