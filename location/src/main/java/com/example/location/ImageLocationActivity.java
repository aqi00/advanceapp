package com.example.location;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.location.bean.ImageInfo;
import com.example.location.util.BitmapUtil;
import com.example.location.util.ExifUtil;
import com.example.location.util.FileUtil;
import com.example.location.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ImageLocationActivity extends AppCompatActivity {
    private final static String TAG = "ImageLocationActivity";
    private GridLayout gl_appendix; // 声明一个网格布局对象
    private ProgressDialog mDialog; // 声明一个进度对话框对象
    private List<ImageInfo> mImageList = new ArrayList<>(); // 图片列表
    private Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; // 相册的Uri
    private String[] mImageColumn = new String[]{ // 媒体库的字段名称数组
            MediaStore.Images.Media._ID, // 编号
            MediaStore.Images.Media.TITLE, // 标题
            MediaStore.Images.Media.SIZE, // 文件大小
            MediaStore.Images.Media.DATA}; // 文件路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_location);
        gl_appendix = findViewById(R.id.gl_appendix);
        new Handler().post(() -> showImageLocation()); // // 显示图像的位置信息
    }

    // 显示图像的位置信息
    private void showImageLocation() {
        // 显示进度对话框
        mDialog = ProgressDialog.show(this, "请稍候", "正在加载图片的位置信息");
        new Thread(() -> loadImageList()).start(); // 启动线程加载图片列表
    }

    // 加载图片列表
    private void loadImageList() {
        Log.d(TAG, "loadImageList");
        mImageList.clear(); // 清空图片列表
        // 查询相册媒体库，并返回结果集的游标。“_size asc”表示按照文件大小升序排列
        Cursor cursor = getContentResolver().query(mImageUri, mImageColumn, null, null, "_size desc");
        if (cursor != null) {
            // 下面遍历结果集，并逐个添加到图片列表。简单起见只挑选前六张图片
            for (int i=0; i<6 && cursor.moveToNext(); i++) {
                ImageInfo image = new ImageInfo(); // 创建一个图片信息对象
                image.setId(cursor.getLong(0)); // 设置图片编号
                image.setName(cursor.getString(1)); // 设置图片名称
                image.setSize(cursor.getLong(2)); // 设置图片的文件大小
                image.setPath(cursor.getString(3)); // 设置图片的文件路径
                Log.d(TAG, image.getName() + " " + image.getSize() + " " + image.getPath());
                // 检查该路径是否合法
                if (!FileUtil.checkFileUri(this, image.getPath())) {
                    i--;
                    continue; // 路径非法则再来一次
                }
                // 从指定路径解码得到位图对象
                Bitmap bitmap = BitmapFactory.decodeFile(image.getPath());
                // 给图像视图设置自动缩放的位图对象
                image.setBitmap(BitmapUtil.getAutoZoomImage(bitmap));
                mImageList.add(image); // 添加至图片列表
            }
            cursor.close(); // 关闭数据库游标
        }
        Log.d(TAG, "mImageList.size="+mImageList.size());
        runOnUiThread(() -> showImageGrid()); // 显示图像网格
    }

    // 显示图像网格
    private void showImageGrid() {
        Log.d(TAG, "showImageGrid");
        for (int i=0; i<mImageList.size(); i++) {
            final ImageInfo image = mImageList.get(i);
            LinearLayout ll_grid = new LinearLayout(this); // 创建一个线性布局视图
            ll_grid.setLayoutParams(new LinearLayout.LayoutParams(Utils.getScreenWidth(this)/3,
                    ViewGroup.LayoutParams.WRAP_CONTENT)); // 设置线性布局的布局参数
            View view = LayoutInflater.from(this).inflate(R.layout.item_location, null);
            ImageView iv_photo = view.findViewById(R.id.iv_photo);
            iv_photo.setImageBitmap(image.getBitmap()); // 设置图像视图的位图对象
            TextView tv_latlng = view.findViewById(R.id.tv_latlng);
            // 获取指定图片的位置信息
            String location = ExifUtil.getLocationFromImage(this, image.getId()+"");
            tv_latlng.setText(location); // 设置文本视图的文字内容
            ll_grid.addView(view); // 把视图对象添加至线性布局
            gl_appendix.addView(ll_grid); // 把线性布局添加至网格布局
        }
        mDialog.dismiss(); // 关闭进度对话框
    }

}