package com.example.threed;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.threed.util.GlUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GlAxisActivity extends AppCompatActivity {
    private GLSurfaceView glsv_content; // 声明一个图形库表面视图对象
    private int mRotateAngle = 0; // 旋转角度
    private float mScaleRatio = 1.0f; // 缩放比率

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gl_axis);
        initScaleSpinner(); // 初始化缩放比率下拉框
        initRotateSpinner(); // 初始化旋转角度下拉框
        glsv_content = findViewById(R.id.glsv_content);
        // 给OpenGL的表面视图注册三维图形的渲染器
        glsv_content.setRenderer(new SceneRender());
        // 设置渲染模式。默认的RENDERMODE_CONTINUOUSLY表示持续刷新，RENDERMODE_WHEN_DIRTY表示只有首次创建和调用requestRender方法时才会刷新
        glsv_content.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //glsv_content.requestRender(); // 主动请求渲染操作
    }

    // 初始化缩放比率下拉框
    private void initScaleSpinner() {
        ArrayAdapter<String> scaleAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, scaleArray);
        Spinner sp_scale = findViewById(R.id.sp_scale);
        sp_scale.setPrompt("请选择缩放比率");
        sp_scale.setAdapter(scaleAdapter);
        sp_scale.setOnItemSelectedListener(new ScaleSelectedListener());
        sp_scale.setSelection(3);
    }

    private String[] scaleArray = {"0.25", "0.5", "0.75", "1.0", "1.5", "2.0", "3.0"};
    class ScaleSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mScaleRatio = Float.parseFloat(scaleArray[arg2]);
            glsv_content.requestRender(); // 主动请求渲染操作
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    // 初始化旋转角度下拉框
    private void initRotateSpinner() {
        ArrayAdapter<String> rotateAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, rotateArray);
        Spinner sp_rotate = findViewById(R.id.sp_rotate);
        sp_rotate.setPrompt("请选择旋转角度");
        sp_rotate.setAdapter(rotateAdapter);
        sp_rotate.setOnItemSelectedListener(new RotateSelectedListener());
        sp_rotate.setSelection(0);
    }

    private String[] rotateArray = {"0", "45", "90", "135", "180", "225", "270", "315"};
    class RotateSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mRotateAngle = Integer.parseInt(rotateArray[arg2]);
            glsv_content.requestRender(); // 主动请求渲染操作
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    // 定义一个三维图形的渲染器
    private class SceneRender implements GLSurfaceView.Renderer {
        // 在表面创建时触发
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // 设置白色背景。0.0f相当于00，1.0f相当于FF
            gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            gl.glShadeModel(GL10.GL_SMOOTH); // 启用阴影平滑
        }

        // 在表面变更时触发
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height); // 设置输出屏幕大小
            // 设置投影矩阵，对应gluPerspective（调整相机）、glFrustumf（透视投影）、glOrthof（正交投影）
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity(); // 重置投影矩阵，即去掉所有的平移、缩放、旋转操作
            // 设置透视图视窗大小
            GLU.gluPerspective(gl, 40, (float) width / height, 0.2f, 20.0f);
            // 选择模型观察矩阵，对应gluLookAt（人动）、glTranslatef/glScalef/glRotatef（物动）
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity(); // 重置模型矩阵
        }

        // 执行框架绘制动作
        @Override
        public void onDrawFrame(GL10 gl) {
            // 清除屏幕和深度缓存
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity(); // 重置当前的模型观察矩阵
            gl.glColor4f(0.0f, 0.0f, 1.0f, 1.0f); // 设置画笔颜色
            // 设置观测点。eyeXYZ表示眼睛坐标，centerXYZ表示原点坐标，upX=1表示X轴朝上，upY=1表示Y轴朝上，upZ=1表示Z轴朝上
            GLU.gluLookAt(gl, 10.0f, 8.0f, 6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
            gl.glRotatef(mRotateAngle, 0, 0, -1); // 沿着Z轴旋转图形
            gl.glScalef(mScaleRatio, mScaleRatio, mScaleRatio); // 沿x、y、z方向缩放若干比例
            drawAxis(gl); // 绘制坐标轴
        }
    }

    private static float[] coordinateArray = {
            0f, 2f, 0f,
            0f, 0f, 0f,
            0f, 0f, 2f,
            0f, 0f, 0f,
            2f, 0f, 0f};
    // 绘制坐标轴
    private void drawAxis(GL10 gl) {
        // 启用顶点开关。GL_VERTEX_ARRAY表示顶点数组，GL_COLOR_ARRAY表示颜色数组
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glLineWidth(3); // 指定线宽
        FloatBuffer buffer = GlUtil.getFloatBuffer(coordinateArray);
        // 将顶点坐标传给 OpenGL 管道
        //size: 每个顶点有几个数值描述。必须是2，3 ，4 之一。
        //type: 数组中每个顶点的坐标类型。取值：GL_BYTE,GL_SHORT, GL_FIXED, GL_FLOAT。
        //stride：数组中每个顶点间的间隔，步长（字节位移）。取值若为0，表示数组是连续的
        //pointer：即存储顶点的Buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
        // 用画线的方式将点连接并画出来
        //GL_POINTS ————绘制独立的点
        //GL_LINE_STRIP————绘制连续的线段，不封闭
        //GL_LINE_LOOP————绘制连续的线段，封闭
        //GL_LINES————顶点两两连接，为多条线段构成
        //GL_TRIANGLES————每隔三个顶点构成一个三角形
        //GL_TRIANGLE_STRIP————每相邻三个顶点组成一个三角形
        //GL_TRIANGLE_FAN————以一个点为三角形公共顶点，组成一系列相邻的三角形
        //第二个参数first：填0表示从第一个顶点开始
        //第三个参数count：每个面画的线段-1。如果count=3表示这个面画两条线段
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, 3);
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 2, 3);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); // 禁用顶点开关
    }

}