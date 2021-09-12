package com.example.threed;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.threed.util.GlVertexUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GlGlobeActivity extends AppCompatActivity {
    private GLSurfaceView glsv_content; // 声明一个图形库表面视图对象
    private Bitmap mBitmap; // 声明一个位图对象
    private int mType; // 地球仪的类型
    private int mDivide = 40; // 将经纬度等分的面数
    private int mRadius = 4; // 球半径
    private int mAngle = 0; // 旋转角度
    private List<FloatBuffer> mVertexList = new ArrayList<>(); // 顶点列表
    private List<FloatBuffer> mTextureCoords = new ArrayList<>(); // 纹理列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gl_globe);
        initTypeSpinner(); // 初始化类型下拉框
        // 从资源文件中获取平面世界地图的位图对象
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mokatuo);
        // 计算球面顶点坐标
        mVertexList = GlVertexUtil.getBallVertexs(mDivide, mRadius);
        // 计算球面材质坐标
        mTextureCoords = GlVertexUtil.getTextureCoords(mDivide);
        glsv_content = findViewById(R.id.glsv_content);
        // 给OpenGL的表面视图注册三维图形的渲染器
        glsv_content.setRenderer(new GlobeRender());
        // 设置渲染模式。默认的RENDERMODE_CONTINUOUSLY表示持续刷新，RENDERMODE_WHEN_DIRTY表示只有首次创建和调用requestRender方法时才会刷新
        glsv_content.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //glsv_content.requestRender(); // 主动请求渲染操作
    }

    // 初始化类型下拉框
    private void initTypeSpinner() {
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, typeArray);
        Spinner sp_type = findViewById(R.id.sp_type);
        sp_type.setPrompt("请选择球体贴图类型");
        sp_type.setAdapter(typeAdapter);
        sp_type.setOnItemSelectedListener(new TypeSelectedListener());
        sp_type.setSelection(0);
    }

    private String[] typeArray = {"东半球", "西半球", "北半球", "南半球", "转动地球仪"};
    class TypeSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mType = arg2;
            if (mType == 4) {
                glsv_content.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY); // 设置渲染模式
            } else {
                mAngle = 0;
                glsv_content.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // 设置渲染模式
                glsv_content.requestRender(); // 主动请求渲染操作
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    // 定义一个三维图形的渲染器
    private class GlobeRender implements GLSurfaceView.Renderer {
        // 在表面创建时触发
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // 设置白色背景
            gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            gl.glShadeModel(GL10.GL_SMOOTH); // 启用阴影平滑
            // 启用某功能，对应的glDisable是禁用某功能
            // GL_DEPTH_TEST用来开启更新深度缓冲区的功能，也就是，如果通过比较后深度值发生变化了，
            // 会进行更新深度缓冲区的操作。一旦启用它，OpenGL就可以跟踪在Z轴上的像素，
            // 这样，它只会在那个像素前方没有东西时，才会绘制这个像素。
            // 在绘制三维图形时，这个功能最好启动，视觉效果比较真实。
            gl.glEnable(GL10.GL_DEPTH_TEST);
            // 除了深度测试，还可以开启以下功能
            //gl.glEnable(GL10.GL_LIGHTING); // 开启灯照效果
            //gl.glEnable(GL10.GL_LIGHT0); // 启用光源0
            //gl.glEnable(GL10.GL_COLOR_MATERIAL); // 启用颜色追踪
            gl.glEnable(GL10.GL_TEXTURE_2D); // 启用纹理。启用之后才能贴图
            // 使用OpenGL库创建一个材质(Texture)，首先要获取一个材质编号（保存在textures中）
            int[] textures = new int[1];
            gl.glGenTextures(1, textures, 0); // 生成材质编号
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]); // 通知OpenGL绑定这个材质编号
            // 材质纹理的尺寸可能大于或小于渲染区域，所以要设置纹理在放大或缩小时的模式
            // GL_TEXTURE_MAG_FILTER表示放大的情况，GL_TEXTURE_MIN_FILTER表示缩小的情况
            // 常用的两种模式为GL10.GL_LINEAR和GL10.GL_NEAREST。
            // 使用GL_NEAREST会得到较清晰的图像，使用GL_LINEAR会得到较模糊的图像
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            // 当定义的材质坐标点超过UV坐标的区域范围(UV坐标为0,0到1,1)，就要告诉OpenGL如何渲染这些不存在的纹理
            // GL_TEXTURE_WRAP_S表示水平方向，GL_TEXTURE_WRAP_T表示垂直方向
            // 有两种设置：GL_REPEAT表示重复Texture，GL_CLAMP_TO_EDGE表示只靠边线绘制一次
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
            // 将位图Bitmap和纹理Texture绑定起来，即指定一个具体的材质资源
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
        }

        // 在表面变更时触发
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height); // 设置输出屏幕大小
            gl.glMatrixMode(GL10.GL_PROJECTION); // 设置投影矩阵
            gl.glLoadIdentity(); // 重置投影矩阵
            // 设置透视图视窗大小。第二个参数是视角，越大则视野越广；第三个参数是宽高比
            // 第四个参数表示眼睛距离物体最近处的距离；第五个参数表示眼睛距离物体最远处的距离
            // gluPerspective和gluLookAt需要配合使用，才能调节观察到的物体大小
            GLU.gluPerspective(gl, 8, (float) width / (float) height, 0.1f, 100.0f);
            gl.glMatrixMode(GL10.GL_MODELVIEW); // 选择模型观察矩阵
            gl.glLoadIdentity(); // 重置模型矩阵
        }

        // 执行框架绘制动作
        @Override
        public void onDrawFrame(GL10 gl) {
            // 清除屏幕和深度缓存
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity(); // 重置当前的模型观察矩阵
            // 下面根据类型设置不同的观测点
            if (mType == 0 || mType == 4) { // 东半球
                GLU.gluLookAt(gl, 0.0f, 0.0f, 70.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
            } else if (mType == 1) { // 西半球
                GLU.gluLookAt(gl, 0.0f, 0.0f, -70.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
            } else if (mType == 2) { // 北半球
                GLU.gluLookAt(gl, 0.0f, 70.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
            } else if (mType == 3) { // 南半球
                GLU.gluLookAt(gl, 0.0f, -70.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
            }
            gl.glRotatef(mAngle, 0, 1, 0); // 设置旋转角度，转动地球仪
            mAngle++;
            drawGlobe(gl); // 绘制地球仪
        }
    }

    // 绘制地球仪
    private void drawGlobe(GL10 gl) {
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); // 启用材质开关
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); // 启用顶点开关
        for (int i = 0; i <= mDivide; i++) {
            // 将顶点坐标传给 OpenGL 管道
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexList.get(i));
            // 声明纹理点坐标
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureCoords.get(i));
            // GL_LINE_STRIP只绘制线条，GL_TRIANGLE_STRIP才是画三角形的面
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, mDivide * 2 + 2);
        }
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); // 禁用顶点开关
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY); // 禁用材质开关
    }

}
