#version 300 es
precision mediump float; // 声明浮点数为中等精度（highp高精度，mediump中等精度，lowp低精度）
in vec4 vColor; // 声明一个颜色向量的输入参数
out vec4 fragColor; // 声明一个颜色向量的输出参数
void main() { // 小程序的实现代码
     fragColor = vColor;
}