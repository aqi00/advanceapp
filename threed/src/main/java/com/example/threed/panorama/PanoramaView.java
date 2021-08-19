package com.example.threed.panorama;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.example.threed.R;

@SuppressLint("ClickableViewAccessibility")
public class PanoramaView extends RelativeLayout {
    private Context mContext; // 声明一个上下文对象
    private GLSurfaceView glsv_panorama; // 声明一个图形库表面视图对象
    private PanoramaRender mRender; // 声明一个全景渲染器
    private PointF mPreviousPos; // 记录上一次的横纵坐标位置

    public PanoramaView(Context context) {
        this(context, null);
    }

    public PanoramaView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanoramaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView(); // 初始化视图
    }

    // 初始化视图
    private void initView() {
        // 根据布局文件layout_panorama.xml生成转换视图对象
        LayoutInflater.from(mContext).inflate(R.layout.layout_panorama, this);
        glsv_panorama = findViewById(R.id.glsv_panorama);
        // 声明使用OpenGL ES的版本号为3.0
        glsv_panorama.setEGLContextClientVersion(3);
    }

    // 在发生触摸事件时触发
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) { // 移动手指
            // 移动手势，则令全景照片旋转相应的角度
            float dx = event.getX() - mPreviousPos.x;
            float dy = event.getY() - mPreviousPos.y;
            mRender.yAngle += dx * 0.3f;
            mRender.xAngle += dy * 0.3f;
        }
        // 保存本次的触摸坐标数值
        mPreviousPos = new PointF(event.getX(), event.getY());
        return true;
    }

    // 传入全景照片的资源编号
    public void initRender(int drawableId) {
        mRender = new PanoramaRender(mContext); // 创建一个全新的全景渲染器
        setDrawableId(drawableId); // 传入全景图片的资源编号
        glsv_panorama.setRenderer(mRender); // 设置全景照片的渲染器
    }

    // 传入全景图片的资源编号
    public void setDrawableId(int drawableId) {
        mRender.setDrawableId(drawableId);
    }

}
