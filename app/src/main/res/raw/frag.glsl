#version 300 es
precision mediump float;
in vec4 vColor;//接受从顶点着色器过来的参数
out vec4 fragColor;//输出到片元这颜色
void main(){
fragColor=vColor;//此片元的颜色值
}