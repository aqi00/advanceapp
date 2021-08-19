package com.example.threed;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.threed.util.GlUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EsShaderActivity extends AppCompatActivity {
    private final static String TAG = "EsShaderActivity";
    private GLSurfaceView glsv_content; // 声明一个图形库表面视图对象
    private int mProgramId; // 声明glsl小程序的编号
    private int mStyle; // 三角形样式
    // 三角形的顶点坐标数组
    private float mCoordArray[] = { // 默认按逆时针方向顺序绘制
            0.0f, 0.622008459f, 0.0f,   // 顶
            -0.5f, -0.311004243f, 0.0f,   // 左底
            0.5f, -0.311004243f, 0.0f    // 右底
    };
    // 三角形的顶点颜色数组（纯色）
    private float[] mColorPureArray = {
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f
    };
    // 三角形的顶点颜色数组（彩色）
    private float[] mColorFullArray = {
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_es_shader);
        showEsVersion(); // 显示OpenGL ES的版本号
        initStyleSpinner(); // 初始化样式下拉框
        glsv_content = findViewById(R.id.glsv_content);
        // 声明使用OpenGL ES的版本号为3.0。使用ES30方法之前务必指定版本号
        glsv_content.setEGLContextClientVersion(3);
        // 给OpenGL的表面视图注册三维图形的渲染器
        glsv_content.setRenderer(new ShaderRender());
        // 设置渲染模式（关闭自动刷新）
        glsv_content.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    // 显示OpenGL ES的版本号
    private void showEsVersion() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        String versionDesc = String.format("%08X", info.reqGlEsVersion);
        String versionCode = String.format("%d.%d", Integer.parseInt(versionDesc)/10000,
                Integer.parseInt(versionDesc)%10000);
        Toast.makeText(this, "系统的OpenGL ES版本号为"+versionCode, Toast.LENGTH_SHORT).show();
    }

    // 初始化样式下拉框
    private void initStyleSpinner() {
        ArrayAdapter<String> styleAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, styleArray);
        Spinner sp_style = findViewById(R.id.sp_style);
        sp_style.setPrompt("请选择三角形样式");
        sp_style.setAdapter(styleAdapter);
        sp_style.setOnItemSelectedListener(new StyleSelectedListener());
        sp_style.setSelection(0);
    }

    private String[] styleArray = { "只画线条", "绘制纯色表面", "绘制彩色表面" };
    class StyleSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mStyle = arg2;
            glsv_content.requestRender(); // 主动请求渲染操作
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    // 定义一个三维图形的渲染器
    private class ShaderRender implements GLSurfaceView.Renderer {
        // 在表面创建时触发
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // 设置白色背景。0.0f相当于00，1.0f相当于FF
            GLES30.glClearColor(1f, 1f, 1f, 1);
            // 初始化着色器
            mProgramId = GlUtil.initShaderProgram(EsShaderActivity.this, "shader_vertex.glsl", "shader_fragment.glsl");
        }

        // 在表面变更时触发
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES30.glViewport(0, 0, width, height); // 设置输出屏幕大小
            // 开启剔除操作。开启之后才能调用glCullFace方法禁止光照、阴影和颜色等效果，以消除不必要的渲染计算。
            GLES30.glEnable(GLES30.GL_CULL_FACE);
        }

        // 执行框架绘制动作
        @Override
        public void onDrawFrame(GL10 gl) {
            // 清除屏幕和深度缓存
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
            GLES30.glLineWidth(3); // 指定线宽
            drawTriangle(); // 绘制三角形
        }
    }

    // 绘制三角形
    private void drawTriangle() {
        FloatBuffer vertexBuffer = GlUtil.getFloatBuffer(mCoordArray);
        FloatBuffer colorBuffer;
        if (mStyle == 2) { // 绘制彩色表面
            colorBuffer = GlUtil.getFloatBuffer(mColorFullArray);
        } else { // 绘制纯色表面
            colorBuffer = GlUtil.getFloatBuffer(mColorPureArray);
        }
        // 获取顶点着色器的vPosition位置（来自shader_vertex.glsl）
        int positionLoc = GLES30.glGetAttribLocation(mProgramId, "vPosition");
        // 获取片段着色器的vColor位置（来自shader_vertex.glsl）
        int colorLoc = GLES30.glGetAttribLocation(mProgramId, "inColor");
        GLES30.glEnableVertexAttribArray(positionLoc); // 启用顶点属性数组
        GLES30.glEnableVertexAttribArray(colorLoc); // 启用顶点属性数组
        // 指定顶点属性数组的位置信息
        GLES30.glVertexAttribPointer(positionLoc, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer);
        // 指定顶点属性数组的颜色信息
        GLES30.glVertexAttribPointer(colorLoc, 4, GLES30.GL_FLOAT, false, 0, colorBuffer);
        if (mStyle == 0) { // 只绘制线条
            // 绘制物体的轮廓线条
            GLES30.glDrawArrays(GLES30.GL_LINE_LOOP, 0, mCoordArray.length/3);
        } else { // 也绘制表面
            // 绘制物体的轮廓表面
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, mCoordArray.length/3);
        }
        GLES30.glDisableVertexAttribArray(colorLoc); // 禁用顶点属性数组
        GLES30.glDisableVertexAttribArray(positionLoc); // 禁用顶点属性数组
    }

}