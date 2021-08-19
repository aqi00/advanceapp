package com.example.threed;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.threed.util.EsVertexUtil;
import com.example.threed.util.GlUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EsMatrixActivity extends AppCompatActivity {
    private final static String TAG = "EsMatrixActivity";
    private GLSurfaceView glsv_content; // 声明一个图形库表面视图对象
    private int mType; // 形状的类型
    private int mDivide = 20; // 将经纬度等分的面数
    private float mRadius = 4; // 球半径
    private int mAngle = 60; // 旋转角度
    private int mProgramId; // 声明glsl小程序的编号
    private float[] mProjectionMatrix = new float[16]; // 声明投影矩阵
    private float[] mModelMatrix = new float[16]; // 声明模型矩阵
    private float[] mMVPMatrix = new float[16]; // 声明结果矩阵

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_es_matrix);
        initVertexList(); // 初始化立方体的顶点列表
        initShapeSpinner(); // 初始化形状下拉框
        glsv_content = findViewById(R.id.glsv_content);
        // 声明使用OpenGL ES的版本号为3.0。使用ES30方法之前务必指定版本号
        glsv_content.setEGLContextClientVersion(3);
        // 给OpenGL的表面视图注册三维图形的渲染器
        glsv_content.setRenderer(new MatrixRenderer());
        // 设置渲染模式。默认的RENDERMODE_CONTINUOUSLY表示持续刷新，RENDERMODE_WHEN_DIRTY表示只有首次创建和调用requestRender方法时才会刷新
        glsv_content.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //glsv_content.requestRender(); // 主动请求渲染操作
    }

    private List<FloatBuffer> mVertexList = new ArrayList<>(); // 顶点列表
    // 以下定义了立方体六个面的顶点坐标数组（每个坐标点都由三个浮点数组成）
    private static float[] vertexsFront = {0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f};
    private static float[] vertexsBack = {0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f};
    private static float[] vertexsTop = {0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f};
    private static float[] vertexsBottom = {0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f};
    private static float[] vertexsLeft = {-0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f};
    private static float[] vertexsRight = {0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f};
    // 初始化立方体的顶点列表
    private void initVertexList() {
        mVertexList.add(GlUtil.getFloatBuffer(vertexsFront));
        mVertexList.add(GlUtil.getFloatBuffer(vertexsBack));
        mVertexList.add(GlUtil.getFloatBuffer(vertexsTop));
        mVertexList.add(GlUtil.getFloatBuffer(vertexsBottom));
        mVertexList.add(GlUtil.getFloatBuffer(vertexsLeft));
        mVertexList.add(GlUtil.getFloatBuffer(vertexsRight));
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
                initVertexList(); // 初始化立方体的顶点列表
            } else if (mType == 1 || mType == 3) {
                // 获取球体的顶点列表
                mVertexList = EsVertexUtil.getBallVertexs(mDivide, mRadius);
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

    public class MatrixRenderer implements GLSurfaceView.Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES30.glClearColor(1f, 1f, 1f, 1f); //设置背景颜色
            // 初始化着色器
            mProgramId = GlUtil.initShaderProgram(EsMatrixActivity.this, "matrix_vertex.glsl", "matrix_fragment.glsl");
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES30.glViewport(0, 0, width, height); // 设置输出屏幕大小
            float aspectRatio = width>height ? 1.0f*width/height : 1.0f*height/width;
            Matrix.setIdentityM(mProjectionMatrix, 0); // 初始化投影矩阵
            // 计算矩阵的正交投影
            Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            // 清除屏幕和深度缓存
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
            Matrix.setIdentityM(mModelMatrix, 0); // 初始化模型矩阵
            Matrix.rotateM(mModelMatrix, 0, mAngle,1f, 1f, 0.5f); // 旋转模型矩阵
            // 把投影矩阵和模型矩阵相乘，得到最终的变换矩阵
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelMatrix, 0);
            // 获取顶点着色器的unMatrix位置
            int matrixLoc = GLES30.glGetUniformLocation(mProgramId, "unMatrix");
            // 输入变换矩阵信息
            GLES30.glUniformMatrix4fv(matrixLoc, 1, false, mMVPMatrix, 0);
            mAngle++;
            GLES30.glLineWidth(3); // 指定线宽
            if (mType == 0 || mType == 2) {
                drawCube(); // 绘制立方体
            } else if (mType == 1 || mType == 3) {
                drawBall(); // 绘制球体
            }
        }
    }

    // 绘制立方体
    private void drawCube() {
        // 获取顶点着色器的vPosition位置
        int positionLoc = GLES30.glGetAttribLocation(mProgramId, "vPosition");
        GLES30.glEnableVertexAttribArray(positionLoc); // 启用顶点属性数组
        for (FloatBuffer buffer : mVertexList) {
            // 指定顶点属性数组的信息
            GLES30.glVertexAttribPointer(positionLoc, 3, GLES30.GL_FLOAT, false, 0, buffer);
            // 绘制物体的轮廓线条
            GLES30.glDrawArrays(GLES30.GL_LINE_LOOP, 0, EsVertexUtil.getCubePointCount());
        }
        GLES30.glDisableVertexAttribArray(positionLoc); // 禁用顶点属性数组
    }

    // 绘制球体
    private void drawBall() {
        // 获取顶点着色器的vPosition位置
        int positionLoc = GLES30.glGetAttribLocation(mProgramId, "vPosition");
        GLES30.glEnableVertexAttribArray(positionLoc); // 启用顶点属性数组
        // 每次画两条相邻的纬度线
        for (int i = 0; i <= mDivide && i < mVertexList.size(); i++) {
            // 指定顶点属性数组的信息
            GLES30.glVertexAttribPointer(positionLoc, 3, GLES30.GL_FLOAT, false, 0, mVertexList.get(i));
            // 绘制物体的轮廓线条
            GLES30.glDrawArrays(GLES30.GL_LINE_STRIP, 0, mDivide * 2 + 2);
        }
        GLES30.glDisableVertexAttribArray(positionLoc); // 禁用顶点属性数组
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