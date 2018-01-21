package com.example.bluejoe.ztyt;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by bluejoe on 2018/1/14.
 */

public class MyTDView extends GLSurfaceView {
    final float ANGLE_SPAN = 0.375f;

    RotateThread rthread;
    SceneRenderer mRenderer;//自定义渲染器的引用

    public MyTDView(Context context) {
        super(context);
        this.setEGLContextClientVersion(3); //设置opengl es的版本为3
        mRenderer = new SceneRenderer(context);
        this.setRenderer(mRenderer);
        this.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//持续渲染模式    还有一种模式是当有请求时才渲染
    }

    private class SceneRenderer implements GLSurfaceView.Renderer {
        Context context;
        Triangle tle;

        public SceneRenderer(Context context) {
            this.context = context;
        }

        public void onDrawFrame(GL10 gl)  //绘制
        {
            //清除深度缓冲与颜色缓冲
            GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);
            //绘制三角形对象
            tle.drawSelf();
        }

        public void onSurfaceChanged(GL10 gl, int width, int height)    //当窗口更改的时候调用
        {
            //设置视窗大小及位置
            GLES30.glViewport(0, 0, width, height);
            //计算GLSurfaceView的宽高比
            float ratio = (float) width / height;
            //调用此方法计算产生透视投影矩阵
            Matrix.frustumM(Triangle.mProjMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
            //调用此方法产生摄像机9参数位置矩阵
            Matrix.setLookAtM(Triangle.mVMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config)   //创建窗口
        {
            //设置屏幕背景色RGBA
            GLES30.glClearColor(0, 0, 0, 1.0f);
            //创建三角形对对象
            tle = new Triangle(MyTDView.this, context);
            //打开深度检测
            GLES30.glEnable(GLES30.GL_DEPTH_TEST);
            rthread = new RotateThread();
            rthread.start();
        }
    }

    public class RotateThread extends Thread //自定义的内部类线程  重新发发起一个线程让绕x轴旋转
    {
        public boolean flag = true;

        @Override
        public void run()//重写run方法
        {
            while (flag) {
                mRenderer.tle.xAngle = mRenderer.tle.xAngle + ANGLE_SPAN;
                try {
                    Thread.sleep(20);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}