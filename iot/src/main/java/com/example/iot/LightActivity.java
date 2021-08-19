package com.example.iot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.iot.util.DateUtil;
import com.example.iot.util.SwitchUtil;

@SuppressLint(value={"DefaultLocale","SetTextI18n"})
public class LightActivity extends AppCompatActivity implements SensorEventListener {
    private TextView tv_light; // 声明一个文本视图对象
    private SensorManager mSensorMgr; // 声明一个传感管理器对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);
        CheckBox ck_bright = findViewById(R.id.ck_bright);
        // 检查屏幕亮度是否为自动调节
        if (SwitchUtil.getAutoBrightStatus(this)) {
            ck_bright.setChecked(true);
        }
        // Android8.0之后普通应用不允许修改系统设置
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            ck_bright.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // 设置是否开启屏幕亮度的自动调节
                SwitchUtil.setAutoBrightStatus(this, isChecked);
            });
        } else {
            ck_bright.setEnabled(false);
        }
        tv_light = findViewById(R.id.tv_light);
        // 从系统服务中获取传感管理器对象
        mSensorMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorMgr.unregisterListener(this); // 注销当前活动的传感监听器
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 给光线传感器注册传感监听器
        mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) { // 光线强度变更事件
            float light_strength = event.values[0];
            tv_light.setText(DateUtil.getNowTime() + " 当前光线强度为" + light_strength);
        }
    }

    //当传感器精度改变时回调该方法，一般无需处理
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}
