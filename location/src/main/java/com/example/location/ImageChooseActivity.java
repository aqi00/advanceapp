package com.example.location;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.location.util.BitmapUtil;
import com.example.location.util.ExifUtil;

public class ImageChooseActivity extends AppCompatActivity {
    private final static String TAG = "ImageChooseActivity";
    private int CHOOSE_CODE = 3; // 只在相册挑选图片的请求码
    private int COMBINE_CODE = 4; // 既可拍照获得现场图片、也可在相册挑选已有图片的请求码
    private ImageView iv_photo; // 声明一个图像视图对象
    private TextView tv_location; // 声明一个文本视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_choose);
        iv_photo = findViewById(R.id.iv_photo);
        tv_location = findViewById(R.id.tv_location);
        findViewById(R.id.btn_choose).setOnClickListener(v -> {
            // 创建一个内容获取动作的意图（准备跳到系统相册）
            Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
            albumIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 是否允许多选
            albumIntent.setType("image/*"); // 类型为图像
            startActivityForResult(albumIntent, CHOOSE_CODE); // 打开系统相册
        });
        findViewById(R.id.btn_combine).setOnClickListener(v -> openSelectDialog());
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
        tv_location.setText("");
        if (resultCode == RESULT_OK && requestCode == CHOOSE_CODE) { // 从相册返回
            if (intent.getData() != null) { // 从相册选择一张照片
                Uri uri = intent.getData(); // 获得已选择照片的路径对象
                showImageLocation(uri); // 显示图片的位置信息
                // 根据指定图片的uri，获得自动缩小后的位图对象
                Bitmap bitmap = BitmapUtil.getAutoZoomImage(this, uri);
                iv_photo.setImageBitmap(bitmap); // 设置图像视图的位图对象
            } else if (intent.getClipData() != null) { // 从相册选择多张照片
                ClipData images = intent.getClipData(); // 获取剪切板数据
                if (images.getItemCount() > 0) { // 至少选择了一个文件
                    Uri uri = images.getItemAt(0).getUri(); // 取第一张照片
                    showImageLocation(uri); // 显示图片的位置信息
                    // 根据指定图片的uri，获得自动缩小后的位图对象
                    Bitmap bitmap = BitmapUtil.getAutoZoomImage(this, uri);
                    iv_photo.setImageBitmap(bitmap); // 设置图像视图的位图对象
                }
            }
        }
        if (resultCode == RESULT_OK && requestCode == COMBINE_CODE) { // 从组合选择返回
            if (intent.getData() != null) { // 从相册选择一张照片
                Uri uri = intent.getData(); // 获得已选择照片的路径对象
                showImageLocation(uri); // 显示图片的位置信息
                // 根据指定图片的uri，获得自动缩小后的位图对象
                Bitmap bitmap = BitmapUtil.getAutoZoomImage(this, uri);
                iv_photo.setImageBitmap(bitmap); // 设置图像视图的位图对象
            } else if (intent.getExtras() != null) { // 拍照的缩略图
                Object obj = intent.getExtras().get("data");
                if (obj instanceof Bitmap) { // 属于位图类型
                    Bitmap bitmap = (Bitmap) obj; // 强制转成位图对象
                    iv_photo.setImageBitmap(bitmap); // 设置图像视图的位图对象
                }
            }
        }
    }

    // 显示图片的位置信息
    private void showImageLocation(Uri uri) {
        Log.d(TAG, "uri="+uri);
        //content://com.android.providers.media.documents/document/image%3A208421 末尾数字是图片编号
        String location = ExifUtil.getLocationFromImage(this, uri);
        tv_location.setText("相片信息："+location.replace("\n", "，"));
    }

}