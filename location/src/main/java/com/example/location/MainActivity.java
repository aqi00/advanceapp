package com.example.location;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.location.util.PermissionUtil;
import com.example.location.util.SharedUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_location_setting).setOnClickListener(this);
        findViewById(R.id.btn_location_begin).setOnClickListener(this);
        findViewById(R.id.btn_location_address).setOnClickListener(this);
        findViewById(R.id.btn_image_location).setOnClickListener(this);
        findViewById(R.id.btn_image_choose).setOnClickListener(this);
        findViewById(R.id.btn_satellite_sphere).setOnClickListener(this);
        findViewById(R.id.btn_wifi_info).setOnClickListener(this);
        findViewById(R.id.btn_wifi_scan).setOnClickListener(this);
        findViewById(R.id.btn_wifi_rtt).setOnClickListener(this);
        findViewById(R.id.btn_map_location).setOnClickListener(this);
        findViewById(R.id.btn_map_basic).setOnClickListener(this);
        findViewById(R.id.btn_map_search).setOnClickListener(this);
        findViewById(R.id.btn_map_navigation).setOnClickListener(this);
        findViewById(R.id.btn_nearby).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_location_setting) {
            startActivity(new Intent(this, LocationSettingActivity.class));
        } else if (v.getId() == R.id.btn_location_begin) {
            if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, LocationBeginActivity.class));
            }
        } else if (v.getId() == R.id.btn_location_address) {
            if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, LocationAddressActivity.class));
            }
        } else if (v.getId() == R.id.btn_image_location) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, ImageLocationActivity.class));
            }
        } else if (v.getId() == R.id.btn_image_choose) {
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, ImageChooseActivity.class));
            }
        } else if (v.getId() == R.id.btn_wifi_info) {
            startActivity(new Intent(this, WifiInfoActivity.class));
        } else if (v.getId() == R.id.btn_wifi_scan) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Toast.makeText(this, "扫描周边WiFi需要Android6或更高版本", Toast.LENGTH_SHORT).show();
                return;
            }
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, WifiScanActivity.class));
            }
        } else if (v.getId() == R.id.btn_wifi_rtt) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                Toast.makeText(this, "室内WiFi定位需要Android9或更高版本", Toast.LENGTH_SHORT).show();
                return;
            }
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, WifiRttActivity.class));
            }
        } else if (v.getId() == R.id.btn_satellite_sphere) {
            if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, SatelliteSphereActivity.class));
            }
        } else if (v.getId() == R.id.btn_map_location) {
            if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, MapLocationActivity.class));
            }
        } else if (v.getId() == R.id.btn_map_basic) {
            if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, MapBasicActivity.class));
            }
        } else if (v.getId() == R.id.btn_map_search) {
            if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, MapSearchActivity.class));
            }
        } else if (v.getId() == R.id.btn_map_navigation) {
            if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                startActivity(new Intent(this, MapNavigationActivity.class));
            }
        } else if (v.getId() == R.id.btn_nearby) {
            if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, (int) v.getId() % 65536)) {
                gotoNearby();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // requestCode不能为负数，也不能大于2的16次方即65536
        if (requestCode == R.id.btn_location_begin % 65536) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, LocationBeginActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能开始定位噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_location_address % 65536) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, LocationAddressActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能获取当前地址噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_image_location % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, ImageLocationActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储卡权限才能读取图像位置噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_image_choose % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, ImageChooseActivity.class));
            } else {
                Toast.makeText(this, "需要允许存储卡权限才能读取图像位置噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_satellite_sphere % 65536) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, SatelliteSphereActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能查看卫星浑天仪噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_wifi_scan % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, WifiScanActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能扫描周边的WiFi噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_wifi_rtt % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, WifiRttActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能进行室内WiFi定位噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_map_location % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, MapLocationActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能进行地图定位噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_map_basic % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, MapBasicActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能显示地图类型噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_map_search % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, MapSearchActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能搜索地点信息噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_map_navigation % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                startActivity(new Intent(this, MapNavigationActivity.class));
            } else {
                Toast.makeText(this, "需要允许定位权限才能规划导航路线噢", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == R.id.btn_nearby % 65536) {
            if (PermissionUtil.checkGrant(grantResults)) { // 用户选择了同意授权
                gotoNearby();
            } else {
                Toast.makeText(this, "需要允许定位权限才能规划导航路线噢", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void gotoNearby() {
        String commitMyInfo = SharedUtil.getIntance(this).readString("commitMyInfo", "false");
        if ("false".equals(commitMyInfo)) {
            startActivity(new Intent(this, ChooseLocationActivity.class));
        } else {
            startActivity(new Intent(this, NearbyActivity.class));
        }
    }
}
