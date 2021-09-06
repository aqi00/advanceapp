#version 300 es
precision mediump float; // 声明浮点数为中等精度（highp高精度，mediump中等精度，lowp低精度）
in vec4 vColor; // 声明一个颜色向量的输入参数
in vec2 vTextCoord; // 声明一个材质坐标向量的输入参数
uniform sampler2D usTexture; // 声明一个二维纹理采样器的全局变量
out vec4 fragColor; // 声明一个颜色向量的输出参数
void main() { // 小程序的实现代码
    fragColor = texture(usTexture,vTextCoord); // 调用内置的texture函数
}