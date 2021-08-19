package com.example.threed.panorama;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

import com.example.threed.util.GlUtil;
import com.example.threed.util.PanoramaUtil;

import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PanoramaRender implements Renderer {
    private final static String TAG = "PanoramaRender";
    private Context mContext; // 声明一个上下文对象
    private int mProgramId; // 声明glsl小程序的编号
    private int mVertexCount; // 顶点数量
    private FloatBuffer mVertexBuff; // 顶点缓存
    private FloatBuffer mTextureBuff; // 纹理缓存
    private int mLastDrawableId = 0, mThisDrawableId = 0; // 上次的图片id，本次的图片id
    private Bitmap mBitmap; // 全景图片的位图对象
    private float[] mProjectMatrix = new float[16]; // 投影矩阵
    private float[] mModelMatrix = new float[16]; // 模型矩阵
    private float[] mMVPMatrix = new float[16]; // 结果矩阵
    public float xAngle = 0f, yAngle = 90f, zAngle; // 三维空间的三个观测角度

    public PanoramaRender(Context context) {
        mContext = context;
        initData(); // 初始化顶点数据和纹理坐标
    }

    // 初始化顶点数据和纹理坐标
    private void initData() {
        int perVertex = 36;
        double perRadius = 2 * Math.PI / (float) perVertex;
        List<Float> vertexList = PanoramaUtil.getPanoramaVertexList(perVertex, perRadius);
        List<Float> textureList = PanoramaUtil.getPanoramaTextureList(perVertex);
        mVertexCount = vertexList.size() / 3; // 每个顶点都有xyz三坐标，所以顶点数量要除以3
        mVertexBuff = GlUtil.getFloatBuffer(vertexList); // 获取顶点缓存
        mTextureBuff = GlUtil.getFloatBuffer(textureList); // 获取纹理缓存
    }

    // 设置全景图片的资源编号
    public void setDrawableId(int drawableId) {
        mBitmap = BitmapFactory.decodeResource(mContext.getResources(), drawableId);
        mThisDrawableId = drawableId;
    }

    // 在表面创建时触发
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 初始化着色器
        mProgramId = GlUtil.initShaderProgram(mContext, "panorama_vertex.glsl", "panorama_fragment.glsl");
    }

    // 在表面变更时触发
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height); // 设置输出屏幕大小
        GLES30.glEnable(GLES30.GL_CULL_FACE); // 开启剔除操作
        float ratio = width / (float) height;
        // 计算矩阵的透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 1, 20);
        Matrix.translateM(mProjectMatrix, 0, 0, 0, -2); // 平移投影矩阵
        Matrix.scaleM(mProjectMatrix, 0, 4, 4, 4); // 缩放投影矩阵
        // 获取顶点着色器的vPosition位置
        int positionLoc = GLES30.glGetAttribLocation(mProgramId, "vPosition");
        Log.d(TAG, "positionLoc:" + positionLoc);
        // 指定顶点属性数组的信息
        GLES30.glVertexAttribPointer(positionLoc, 3, GLES30.GL_FLOAT, false, 0, mVertexBuff);
        GLES30.glEnableVertexAttribArray(positionLoc); // 启用顶点属性数组
    }

    // 执行框架绘制动作
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClearColor(1, 1, 1, 1); // 设置白色背景
        // 清除屏幕和深度缓存
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        Matrix.setIdentityM(mModelMatrix, 0); // 初始化模型矩阵
        Matrix.rotateM(mModelMatrix, 0, -xAngle, 1, 0, 0); // 旋转模型矩阵
        Matrix.rotateM(mModelMatrix, 0, -yAngle, 0, 1, 0); // 旋转模型矩阵
        Matrix.rotateM(mModelMatrix, 0, -zAngle, 0, 0, 1); // 旋转模型矩阵
        // 把投影矩阵和模型矩阵相乘，得到最终的结果矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mModelMatrix, 0);
        // 获取顶点着色器的unMatrix位置
        int matrixLoc = GLES30.glGetUniformLocation(mProgramId, "unMatrix");
        Log.d(TAG, "matrixLoc:" + matrixLoc);
        // 输入结果矩阵信息
        GLES30.glUniformMatrix4fv(matrixLoc, 1, false, mMVPMatrix, 0);
        if (mLastDrawableId != mThisDrawableId) { // 换了张图片
            // 绑定新的图像纹理
            int textureId = GlUtil.bindImageTexture(mProgramId, mTextureBuff, mBitmap);
            Log.d(TAG, "textureId="+textureId);
            mLastDrawableId = mThisDrawableId;
        }
        // 绘制物体的轮廓表面
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, mVertexCount);
    }

}
