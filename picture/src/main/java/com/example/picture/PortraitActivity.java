package com.example.picture;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.picture.util.BitmapUtil;
import com.example.picture.util.DateUtil;
import com.example.picture.widget.BitmapView;
import com.example.picture.widget.CropImageView;

public class PortraitActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private final static String TAG = "PortraitActivity";
    private int COMBINE_CODE = 4; // 既可拍照获得现场图片、也可在相册挑选已有图片的请求码
    private BitmapView bv_photo; // 声明一个位图视图对象
    private CropImageView civ_photo; // 声明一个裁剪视图对象
    private LinearLayout ll_adjust; // 声明一个线性布局对象
    private SeekBar sb_scale; // 声明一个拖动条对象
    private SeekBar sb_horizontal; // 声明一个拖动条对象
    private SeekBar sb_vertical; // 声明一个拖动条对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portrait);
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("采集头像");
        TextView tv_option = findViewById(R.id.tv_option);
        tv_option.setText("确定");
        tv_option.setOnClickListener(v -> finishCollect());
        bv_photo = findViewById(R.id.bv_photo);
        civ_photo = findViewById(R.id.civ_photo);
        ll_adjust = findViewById(R.id.ll_adjust);
        findViewById(R.id.btn_combine).setOnClickListener(v -> openSelectDialog());
        CheckBox ck_flip = findViewById(R.id.ck_flip);
        ck_flip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            bv_photo.flip(); // 左右翻转图像
            civ_photo.flip(); // 左右翻转图像
        });
        sb_scale = findViewById(R.id.sb_scale);
        sb_horizontal = findViewById(R.id.sb_horizontal);
        sb_vertical = findViewById(R.id.sb_vertical);
        sb_scale.setOnSeekBarChangeListener(this);
        sb_horizontal.setOnSeekBarChangeListener(this);
        sb_vertical.setOnSeekBarChangeListener(this);
    }

    // 结束头像采集
    private void finishCollect() {
        if (civ_photo.getCropBitmap() == null) {
            Toast.makeText(this, "请先选好头像图片", Toast.LENGTH_SHORT).show();
            return;
        }
        // 生成图片文件的保存路径
        String path = String.format("%s/%s.jpg",
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                DateUtil.getNowDateTime());
        BitmapUtil.saveImage(path, civ_photo.getCropBitmap()); // 把位图保存为图片文件
        BitmapUtil.notifyPhotoAlbum(this, path); // 通知相册来了张新图片
        Intent intent = new Intent(); // 创建一个新意图
        intent.putExtra("pic_path", path);
        setResult(Activity.RESULT_OK, intent); // 携带意图返回前一个页面
        finish(); // 关闭当前页面
    }

    // 打开选择对话框（要拍照还是去相册）
    private void openSelectDialog() {
        // 声明相机的拍照行为
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent[] intentArray = new Intent[] { photoIntent };
        // 声明相册的打开行为
        Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        albumIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); // 是否允许多选
        albumIntent.setType("image/*"); // 类型为图像
        // 容纳相机和相册在内的选择意图
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "请拍照或选择图片");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, albumIntent);
        // 创建封装好标题的选择器意图
        Intent chooser = Intent.createChooser(chooserIntent, "选择图片");
        // 在页面底部弹出多种选择方式的列表对话框
        startActivityForResult(chooser, COMBINE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && requestCode == COMBINE_CODE) { // 从组合选择返回
            if (intent.getData() != null) { // 从相册选择一张照片
                Uri uri = intent.getData(); // 获得已选择照片的路径对象
                // 根据指定图片的uri，获得自动缩小后的位图对象
                Bitmap bitmap = BitmapUtil.getAutoZoomImage(this, uri);
                showPicture(bitmap); // 显示已选择的图片
            } else if (intent.getExtras() != null) { // 拍照的缩略图
                Object obj = intent.getExtras().get("data");
                if (obj instanceof Bitmap) { // 属于位图类型
                    Bitmap bitmap = (Bitmap) obj; // 强制转成位图对象
                    showPicture(bitmap); // 显示已选择的图片
                }
            }
        }
    }

    // 显示已选择的图片
    private void showPicture(Bitmap origin) {
        bv_photo.setDrawingCacheEnabled(true); // 开启位图视图的绘图缓存
        bv_photo.setImageBitmap(origin); // 设置图像视图的位图对象
        ll_adjust.setVisibility(View.VISIBLE);
        sb_scale.setProgress(30); // 设置拖动条的当前进度
        sb_horizontal.setProgress(50); // 设置拖动条的当前进度
        sb_vertical.setProgress(50); // 设置拖动条的当前进度
        Bitmap bitmap = bv_photo.getDrawingCache(); // 从绘图缓存获取位图对象
        int width = bitmap.getWidth(), height = bitmap.getHeight();
        civ_photo.setOrigBitmap(bitmap); // 设置裁剪视图的原始位图
        // 设置位图的矩形边界
        civ_photo.setBitmapRect(new Rect(width/8, height/8, width/4*3, height/4*3));
        bv_photo.setDrawingCacheEnabled(false); // 关闭位图视图的绘图缓存
    }

    // 刷新图像展示
    private void refreshImage() {
        Bitmap bitmap = bv_photo.getDrawingCache(); // 从绘图缓存获取位图对象
        civ_photo.setOrigBitmap(bitmap); // 设置裁剪视图的原始位图
        civ_photo.setBitmapRect(civ_photo.getBitmapRect()); // 设置裁剪视图的位图边界
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        bv_photo.setDrawingCacheEnabled(true); // 开启位图视图的绘图缓存
        if (seekBar.getId() == R.id.sb_scale) {
            bv_photo.setScaleRatio(progress/33f, true); // 设置位图视图的缩放比率
        } else if (seekBar.getId()==R.id.sb_horizontal || seekBar.getId()==R.id.sb_vertical) {
            int viewWidth = bv_photo.getMeasuredWidth(); // 获取视图的实际宽度
            int viewHeight = bv_photo.getMeasuredHeight(); // 获取视图的实际高度
            int offsetX = (int) ((sb_horizontal.getProgress()-50)/50f*viewWidth);
            int offsetY = (int) ((sb_vertical.getProgress()-50)/50f*viewHeight);
            Log.d(TAG, "viewWidth="+viewWidth+", offsetX="+offsetX+", viewHeight="+viewHeight+", offsetY="+offsetY);
            bv_photo.setOffset(offsetX, offsetY, true); // 设置位图视图的偏移距离
        }
        refreshImage(); // 刷新图像展示
        bv_photo.setDrawingCacheEnabled(false); // 关闭位图视图的绘图缓存
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}
}