package com.example.threed;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
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

public class GlLineActivity extends AppCompatActivity {
    private GLSurfaceView glsv_content; // 声明一个图形库表面视图对象
    private List<FloatBuffer> mVertexList = new ArrayList<>(); // 顶点列表
    private int mType; // 形状的类型
    private int mDivide = 20; // 将经纬度等分的面数
    private float mRadius = 4; // 球半径
    private int mAngle = 0; // 旋转角度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gl_line);
        initShapeSpinner(); // 初始化形状下拉框
        mVertexList = GlVertexUtil.getCubeVertexs();
        glsv_content = findViewById(R.id.glsv_content);
        // 给OpenGL的表面视图注册三维图形的渲染器
        glsv_content.setRenderer(new LineRender());
        // 设置渲染模式。默认的RENDERMODE_CONTINUOUSLY表示持续刷新，RENDERMODE_WHEN_DIRTY表示只有首次创建和调用requestRender方法时才会刷新
        glsv_content.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //glsv_content.requestRender(); // 主动请求渲染操作
    }

    // 初始化形状下拉框
    private void initShapeSpinner() {
        ArrayAdapter<String> shapeAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, shapeArray);
        Spinner sp_shape = findViewById(R.id.sp_shape);
        sp_shape.setPrompt("请选择三维物体形状");
        sp_shape.setAdapter(shapeAdapter);
        sp_shape.setOnItemSelectedListener(new ShapeSelectedListener());
        sp_shape.setSelection(0);
    }

    private String[] shapeArray = { "静止立方体", "静止球体", "旋转立方体", "旋转球体" };
    class ShapeSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mType = arg2;
            mVertexList.clear();
            if (mType == 0 || mType == 2) {
                mVertexList = GlVertexUtil.getCubeVertexs(); // 获得立方体的顶点列表
            } else if (mType == 1 || mType == 3) {
                // 获得球体的顶点列表
                mVertexList = GlVertexUtil.getBallVertexs(mDivide, mRadius);
            }
            if (mType == 2 || mType == 3) {
                glsv_content.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY); // 设置渲染模式
            } else {
                glsv_content.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // 设置渲染模式
                glsv_content.requestRender(); // 主动请求渲染操作
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    // 定义一个三维图形的渲染器
    private class LineRender implements GLSurfaceView.Renderer {
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
            GLU.gluPerspective(gl, 40, (float) width / height, 0.1f, 20.0f);
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
            // 旋转图形。围绕着Z轴与Y轴之间的平分线旋转
            gl.glRotatef(mAngle, 0, 0, -1);
            gl.glRotatef(mAngle, 0, -1, 0);
            mAngle++;
            // gl.glTranslatef(1, 0, 0); // 沿x轴方向移动1个单位
            // gl.glScalef(0.1f, 0.1f, 0.1f); // 沿x、y、z方向缩放0.1倍
            gl.glLineWidth(3); // 指定线宽
            if (mType == 0 || mType == 2) {
                drawCube(gl); // 绘制立方体
            } else if (mType == 1 || mType == 3) {
                drawBall(gl); // 绘制球体
            }
        }
    }

    // 绘制立方体
    private void drawCube(GL10 gl) {
        // 启用顶点开关。GL_VERTEX_ARRAY表示顶点数组，GL_COLOR_ARRAY表示颜色数组
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        for (FloatBuffer buffer : mVertexList) {
            // 将顶点坐标传给 OpenGL 管道
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
            // 用画线的方式将点连接并画出来
            gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, GlVertexUtil.getCubePointCount());
        }
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); // 禁用顶点开关
    }

    // 绘制球体
    private void drawBall(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); // 启用顶点开关
        // 每次画两条相邻的纬度线
        for (int i = 0; i <= mDivide && i < mVertexList.size(); i++) {
            // 将顶点坐标传给 OpenGL 管道
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexList.get(i));
            // 用画线的方式将点连接并画出来
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, mDivide * 2 + 2);
        }
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY); // 禁用顶点开关
    }

    @Override
    protected void onPause() {
        super.onPause();
        glsv_content.onPause(); // 暂停绘制三维图形
    }

    @Override
    protected void onResume() {
        super.onResume();
        glsv_content.onResume(); // 恢复绘制三维图形
    }

}
