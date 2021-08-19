/*
 * Copyright (C) 2018 by Sascha Willems - www.saschawillems.de
 *
 * This code is licensed under the MIT license (MIT) (http://opensource.org/licenses/MIT)
 */
package com.example.threed;

import android.app.AlertDialog;
import android.app.NativeActivity;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import java.util.concurrent.Semaphore;

public class VulkanRadarActivity extends NativeActivity {

    static {
        System.loadLibrary("native-lib"); // 加载so库
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Use a semaphore to create a modal dialog

    private final Semaphore semaphore = new Semaphore(0, true);

    public void showAlert(final String message) {
        final VulkanRadarActivity activity = this;

        ApplicationInfo applicationInfo = activity.getApplicationInfo();
        final String applicationName = applicationInfo.nonLocalizedLabel.toString();

        this.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
            builder.setTitle(applicationName);
            builder.setMessage(message);
            builder.setPositiveButton("Close", (dialog, id) -> semaphore.release());
            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
        });
        try {
            semaphore.acquire();
        }
        catch (InterruptedException e) { }
    }
}
