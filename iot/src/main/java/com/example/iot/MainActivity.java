package com.example.iot;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.iot.util.PermissionUtil;
import com.example.iot.util.SwitchUtil;
import com.example.iot.widget.InputDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_sensor).setOnClickListener(this);
        findViewById(R.id.btn_acceleration).setOnClickListener(this);
        findViewById(R.id.btn_direction).setOnClickListener(this);
        findViewById(R.id.btn_step).setOnClickListener(this);
        findViewById(R.id.btn_light).setOnClickListener(this);
        findViewById(R.id.btn_gyroscope).setOnClickListener(this);
        findViewById(R.id.btn_bluetooth_pair).setOnClickListener(this);
        findViewById(R.id.btn_bluetooth_a2dp).setOnClickListener(this);
        findViewById(R.id.btn_bluetooth_trans).setOnClickListener(this);
        findViewById(R.id.btn_ble_scan).setOnClickListener(this);
        findViewById(R.id.btn_ble_advertise).setOnClickListener(this);
        findViewById(R.id.btn_ble_chat).setOnClickListener(this);
        findViewById(R.id.btn_scan_car).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_sensor) {
            startActivity(new Intent(this, SensorActivity.class));
        } else if (v.getId() == R.id.btn_acceleration) {
            startActivity(new Intent(this, AccelerationActivity.class));
        } else if (v.getId() == R.id.btn_direction) {
            startActivity(new Intent(this, DirectionActivity.class));
        } else if (v.getId() == R.id.btn_step) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android10之后使用计步器需要健身运动权限
                if (PermissionUtil.checkPermission(this, Manifest.permission.ACTIVITY_RECOGNITION, (int) v.getId() % 65536)) {
                    startActivity(new Intent(this, StepActivity.class));
                }
            } else {
                startActivity(new Intent(this, StepActivity.class));
            }
        } else if (v.getId() == R.id.btn_light) {
            if (SwitchUtil.checkWriteSettings(this, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, LightActivity.class));
            }
        } else if (v.getId() == R.id.btn_gyroscope) {
            startActivity(new Intent(this, GyroscopeActivity.class));
        } else if (v.getId() == R.id.btn_bluetooth_pair) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android6.0之后使用蓝牙需要定位权限
                if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                    startActivity(new Intent(this, BluetoothPairActivity.class));
                }
            } else {
                startActivity(new Intent(this, BluetoothPairActivity.class));
            }
        } else if (v.getId() == R.id.btn_bluetooth_a2dp) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android6.0之后使用蓝牙需要定位权限
                if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                    startActivity(new Intent(this, BluetoothA2dpActivity.class));
                }
            } else {
                startActivity(new Intent(this, BluetoothPairActivity.class));
            }
        } else if (v.getId() == R.id.btn_bluetooth_trans) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android6.0之后使用蓝牙需要定位权限
                if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                    startActivity(new Intent(this, BluetoothTransActivity.class));
                }
            } else {
                startActivity(new Intent(this, BluetoothTransActivity.class));
            }
        } else if (v.getId() == R.id.btn_ble_scan) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android6.0之后使用蓝牙需要定位权限
                if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                    startActivity(new Intent(this, BleScanActivity.class));
                }
            } else {
                startActivity(new Intent(this, BleScanActivity.class));
            }
        } else if (v.getId() == R.id.btn_ble_advertise) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android6.0之后使用蓝牙需要定位权限
                if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                    startActivity(new Intent(this, BleAdvertiseActivity.class));
                }
            } else {
                startActivity(new Intent(this, BleAdvertiseActivity.class));
            }
        } else if (v.getId() == R.id.btn_ble_chat) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android6.0之后使用蓝牙需要定位权限
                if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                    gotoChat();
                }
            } else {
                gotoChat();
            }
        } else if (v.getId() == R.id.btn_scan_car) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android6.0之后使用蓝牙需要定位权限
                if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                    startActivity(new Intent(this, ScanCarActivity.class));
                }
            } else {
                startActivity(new Intent(this, ScanCarActivity.class));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // requestCode不能为负数，也不能大于2的16次方即65536
        if (requestCode == R.id.btn_step % 65536) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, StepActivity.class));
            } else {
                Toast.makeText(this, "需要允许健身运动权限才能使用计步器噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_light % 65536) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, LightActivity.class));
            } else {
                Toast.makeText(this, "需要允许设置权限才能调节屏幕亮度噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_bluetooth_pair % 65536) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, BluetoothPairActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能使用传统蓝牙噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_bluetooth_a2dp % 65536) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, BluetoothA2dpActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能使用传统蓝牙噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_bluetooth_trans % 65536) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, BluetoothTransActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能使用传统蓝牙噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_ble_scan % 65536) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, BleScanActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能使用低功耗蓝牙噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_ble_advertise % 65536) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, BleAdvertiseActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能使用低功耗蓝牙噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_ble_chat % 65536) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gotoChat(); // 跳到聊天界面
            } else {
                Toast.makeText(this, "需要允许定位权限才能使用低功耗蓝牙噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_scan_car % 65536) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, ScanCarActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能遥控智能小车噢", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 跳到聊天界面
    private void gotoChat() {
        // 弹出服务器输入对话框，以便决定作为BLE客户端还是作为BLE服务端
        InputDialog dialog = new InputDialog(this, "", 0, "请输入服务名，不填则为客户端",
                (idt, content, seq) -> {
                    if (TextUtils.isEmpty(content)) {
                        startActivity(new Intent(this, BleClientActivity.class));
                    } else {
                        Intent intent = new Intent(this, BleServerActivity.class);
                        intent.putExtra("server_name", content);
                        startActivity(intent);
                    }
                });
        dialog.show();
    }

}
