package com.example.threed;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.threed.util.GlUtil;
import com.example.threed.util.EsVertexUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EsTextureActivity extends AppCompatActivity {
    private final static String TAG = "EsTextureActivity";
    private GLSurfaceView glsv_content; // 声明一个图形库表面视图对象
    private List<FloatBuffer> mVertexList = new ArrayList<>(); // 顶点列表
    private ByteBuffer mIndicateBuffer; // 示意连线的字节缓存
    private int mAngle = 60; // 旋转角度
    private int mProgramId; // 声明glsl小程序的编号
    private Bitmap mMagicBitmap; // 魔方的位图
    private float[] mProjectionMatrix = new float[16]; // 投影矩阵
    private float[] mModelMatrix = new float[16]; // 模型矩阵
    private float[] mMVPMatrix = new float[16]; // 结果矩阵

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_es_texture);
        initShapeSpinner(); // 初始化形状下拉框
        mVertexList = EsVertexUtil.getCubeVertexs(); // // 获得立方体的顶点列表
        mIndicateBuffer = GlUtil.getByteBuffer(EsVertexUtil.cubeIndicates);
        glsv_content = findViewById(R.id.glsv_content);
        // 声明使用OpenGL ES的版本号为3.0。使用ES30方法之前务必指定版本号
        glsv_content.setEGLContextClientVersion(3);
        // 给OpenGL的表面视图注册三维图形的渲染器
        glsv_content.setRenderer(new TextureRenderer());
        // 设置渲染模式。默认的RENDERMODE_CONTINUOUSLY表示持续刷新，RENDERMODE_WHEN_DIRTY表示只有首次创建和调用requestRender方法时才会刷新
        glsv_content.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    // 初始化形状下拉框
    private void initShapeSpinner() {
        ArrayAdapter<String> shapeAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, shapeArray);
        Spinner sp_shape = findViewById(R.id.sp_shape);
        sp_shape.setPrompt("请选择三维物体形状");
        sp_shape.setAdapter(shapeAdapter);
        sp_shape.setOnItemSelectedListener(new EsTextureActivity.ShapeSelectedListener());
        sp_shape.setSelection(0);
    }

    private String[] shapeArray = { "静止魔方", "旋转魔方" };
    class ShapeSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mMagicBitmap = null;
            if (arg2 == 1) {
                glsv_content.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY); // 设置渲染模式
            } else {
                glsv_content.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // 设置渲染模式
                glsv_content.requestRender(); // 主动请求渲染操作
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    public class TextureRenderer implements GLSurfaceView.Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES30.glClearColor(1f, 1f, 1f, 1f); //设置背景颜色
            // 初始化着色器
            mProgramId = GlUtil.initShaderProgram(EsTextureActivity.this, "texture_vertex.glsl", "texture_fragment.glsl");
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
            // 开启深度测试，这样只绘制靠前的表面，不绘制靠后的表面
            GLES30.glEnable(GLES30.GL_DEPTH_TEST);
            Matrix.setIdentityM(mModelMatrix, 0); // 初始化模型矩阵
            Matrix.rotateM(mModelMatrix, 0, mAngle,1f, 1f, 0.5f); // 旋转模型矩阵
            // 把投影矩阵和模型矩阵相乘，得到最终的变换矩阵
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mModelMatrix, 0);
            // 获取顶点着色器的unMatrix位置
            int matrixLoc = GLES30.glGetUniformLocation(mProgramId, "unMatrix");
            Log.d(TAG, "matrixLoc="+matrixLoc);
            // 输入变换矩阵信息
            GLES30.glUniformMatrix4fv(matrixLoc, 1, false, mMVPMatrix, 0);
            mAngle++;
            drawMagic(); // 绘制魔方
        }
    }

    // 绘制魔方
    private void drawMagic() {
        if (mMagicBitmap == null) {
            mMagicBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.magic);
            FloatBuffer textureBuffer = GlUtil.getFloatBuffer(EsVertexUtil.cubeTextCoord);
            // 绑定魔方的图像纹理
            int textureId = GlUtil.bindImageTexture(mProgramId, textureBuffer, mMagicBitmap);
            Log.d(TAG, "textureId="+textureId);
        }
        // 获取顶点着色器的vPosition位置
        int positionLoc = GLES30.glGetAttribLocation(mProgramId, "vPosition");
        Log.d(TAG, "positionLoc="+positionLoc);
        GLES30.glEnableVertexAttribArray(positionLoc); // 启用顶点属性数组
        for (FloatBuffer buffer : mVertexList) {
            // 指定顶点属性数组的信息
            GLES30.glVertexAttribPointer(positionLoc, 3, GLES30.GL_FLOAT, false, 0, buffer);
            // 假设绘制一个立方体，绘制类型为GL_TRIANGLES，那么六个面各由两个三角形组成，
            // 就得向渲染管线传入36个顶点依次绘制，可实际上一个矩形也只有4个顶点，
            // 为了优化绘制的效率，减少数据的传递，于是有了glDrawElements绘制方法。
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, EsVertexUtil.cubeIndicates.length,
                    GLES30.GL_UNSIGNED_BYTE, mIndicateBuffer);
        }
        GLES30.glDisableVertexAttribArray(positionLoc); // 禁用顶点属性数组
    }

    @Override
    protected void onPause() {
        super.onPause();
        glsv_content.onPause(); // 暂停绘制三维图形
        mMagicBitmap = null;
        mAngle = 60;
    }

    @Override
    protected void onResume() {
        super.onResume();
        glsv_content.onResume(); // 恢复绘制三维图形
    }

}