#version 300 es
layout (location = 0) in vec4 vPosition; // 声明一个位置坐标向量的输入参数
uniform mat4 unMatrix; // 声明一个位置矩阵的全局变量
out vec4 vColor; // 声明一个颜色向量的输出参数
void main() { // 小程序的实现代码
     gl_Position  = unMatrix*vPosition; // 给内置的位置变量赋值
     vColor = vec4(0.0,1.0,1.0,1.0);
}