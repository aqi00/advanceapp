package com.example.threed.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

public class GlUtil {
    private final static String TAG = "GlUtil";

    public static FloatBuffer getFloatBuffer(float[] array) {
        // 初始化字节缓冲区的大小=数组长度*数组元素大小。float类型的元素大小为Float.SIZE，
        // int类型的元素大小为Integer.SIZE，double类型的元素大小为Double.SIZE。
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * Float.SIZE);
        // 以当前设备字节顺序来修改字节缓冲区的字节顺序
        // OpenGL在底层的实现是C语言，与Java默认的数据存储字节顺序可能不同，即大端小端问题。
        // 因此，为了保险起见，在将数据传递给OpenGL之前，需要指明使用当前设备的存储顺序
        byteBuffer.order(ByteOrder.nativeOrder());
        // 根据设置好的参数构造浮点缓冲区
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(array); // 把数组数据写入缓冲区
        floatBuffer.position(0); // 设置浮点缓冲区的初始位置
        return floatBuffer;
    }

    // 根据数据列表获取数据缓存
    public static FloatBuffer getFloatBuffer(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return getFloatBuffer(array);
    }

    public static ByteBuffer getByteBuffer(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.position(0);
        return byteBuffer;
    }

    // 初始化着色器。第二个参数是顶点着色器的文件名称，第三个参数是片段着色器的文件名称
    public static int initShaderProgram(Context context, String vertexFile, String fragmentFile) {
        // 获取顶点着色器的程序内容
        String vertexStr = AssetsUtil.getTxtFromAssets(context, vertexFile);
        // 获取片段着色器的程序内容
        String fragStr = AssetsUtil.getTxtFromAssets(context, fragmentFile);
        int programId = GLES30.glCreateProgram(); // 创建小程序，并返回该程序的编号
        // 创建顶点着色器
        int vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER);
        // 指定顶点着色器的程序内容
        GLES30.glShaderSource(vertexShader, vertexStr);
        GLES30.glCompileShader(vertexShader); // 编译顶点着色器
        // 创建片段着色器
        int fragShader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER);
        GLES30.glShaderSource(fragShader, fragStr); // 指定片段着色器的程序内容
        GLES30.glCompileShader(fragShader); // 编译片段着色器
        // 将顶点着色器的编译结果添加至小程序
        GLES30.glAttachShader(programId, vertexShader);
        // 将片段着色器的编译结果添加至小程序
        GLES30.glAttachShader(programId, fragShader);
        GLES30.glLinkProgram(programId); // 链接着色器的小程序
        GLES30.glUseProgram(programId); // 使用小程序
        return programId;
    }

    // 绑定图像纹理
    public static int bindImageTexture(int programId, FloatBuffer textBuffer, Bitmap bitmap) {
        // 获取顶点着色器的inTextureCoord位置
        int textCoordLoc = GLES30.glGetAttribLocation(programId, "inTextureCoord");
        Log.d(TAG, "textCoordLoc="+textCoordLoc);
        GLES30.glEnableVertexAttribArray(textCoordLoc); // 启用顶点属性数组
        // 指定顶点属性数组的位置信息
        GLES30.glVertexAttribPointer(textCoordLoc, 2, GLES30.GL_FLOAT, false, 0, textBuffer);
        // 下面开始应用纹理
        int[] textures = new int[1];
        GLES30.glGenTextures(1, textures, 0); // 生成材质编号
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0); // 激活某段材质
        Log.d(TAG, "textures[0]="+textures[0]);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]); // 绑定这个材质编号
        // 材质纹理的尺寸可能大于或小于渲染区域，所以要设置纹理在放大或缩小时的模式
        // GL_TEXTURE_MAG_FILTER表示放大的情况，GL_TEXTURE_MIN_FILTER表示缩小的情况
        // 常用的两种模式为GL10.GL_LINEAR和GL10.GL_NEAREST。
        // 使用GL_NEAREST会得到较清晰的图像，使用GL_LINEAR会得到较模糊的图像
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        // 当定义的材质坐标点超过UV坐标的区域范围(UV坐标为0,0到1,1)，就要告诉OpenGL如何渲染这些不存在的纹理
        // GL_TEXTURE_WRAP_S表示水平方向，GL_TEXTURE_WRAP_T表示垂直方向
        // 有两种设置：GL_REPEAT表示重复Texture，GL_CLAMP_TO_EDGE表示只靠边线绘制一次
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        // 将位图Bitmap和纹理Texture绑定起来，即指定一个具体的材质资源
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        // 如果只绘制第一层纹理，那么无需调用下面的材质代码，因为默认就是绘制第一层纹理。
        // 如果同时绘制多层纹理，那么调用glActiveTexture方法的次数就代表有多少层纹理。
//        // 获取片段着色器的usTexture位置
//        int textureLoc = GLES30.glGetUniformLocation(programId, "usTexture");
//        Log.d(TAG, "textureLoc="+textureLoc);
//        GLES30.glUniform1i(textureLoc, 0); // 输入材质信息。0表示第一层纹理
        return textures[0];
    }

}
