#version 300 es
uniform mat4 uMVPMatrix;//总的变换矩阵
layout (location=0) in vec3 aPosition;//顶点的位置
layout (location=1) in vec4 aColor;//顶点颜色
out vec4 vColor;//用于传递给片元着色器的变量
void main()
{
gl_Position=uMVPMatrix*vec4(aPosition,1);

vColor=aColor;
}