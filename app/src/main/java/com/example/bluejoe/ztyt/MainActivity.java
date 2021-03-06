package com.example.bluejoe.ztyt;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView1,mGLSurfaceView2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Utils.supportGlEs20(this)) {
            Toast.makeText(this, "GLES 2.0 not supported!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mGLSurfaceView1 = (GLSurfaceView) findViewById(R.id.surface1);

        mGLSurfaceView1.setEGLContextClientVersion(2);
        mGLSurfaceView1.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView1.setRenderer(new MyRenderer());
        mGLSurfaceView1.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);


        mGLSurfaceView2 = (GLSurfaceView) findViewById(R.id.surface2);

        mGLSurfaceView2.setEGLContextClientVersion(2);
        mGLSurfaceView2.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView2.setRenderer(new MyRenderer2());
        mGLSurfaceView2.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView1.onPause();
        mGLSurfaceView2.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView1.onResume();
        mGLSurfaceView2.onResume();
    }

    private static class MyRenderer implements GLSurfaceView.Renderer {
        float[] camera1_position = {0.01f,0.0f,0.1f};
        double angle = 0.0f;
        float radius = 0.1f;


        private static final String VERTEX_SHADER =
                "attribute vec4 vPosition;\n"
                        + "uniform mat4 uMVPMatrix;\n"
                        + "void main() {\n"
                        + "  gl_Position = uMVPMatrix * vPosition;\n"
                        + "}";
        private static final String FRAGMENT_SHADER =
                "precision mediump float;\n"
                        + "void main() {\n"
                        + "  gl_FragColor = vec4(0.5, 0, 0, 1);\n"
                        + "}";
        private static final float[] VERTEX = {   // in counterclockwise order:
                0, 1, -2.5f,  // top
                -0.5f, -1, -2.5f,  // bottom left
                1, -1, -2.5f,  // bottom right
        };
        private float[]  createPositions(){
            ArrayList<Float> data=new ArrayList<>();
            data.add(0.0f);             //设置圆心坐标
            data.add(0.0f);
            data.add(-2.5f);
            float angDegSpan=360f/10000;
            for(float i=0;i<360+angDegSpan;i+=angDegSpan){
                data.add((float) (radius*Math.sin(i*Math.PI/180f)));
                data.add((float)(radius*Math.cos(i*Math.PI/180f)));
                data.add(-2.5f);
            }
            float[] f=new float[data.size()];
            for (int i=0;i<f.length;i++){
                f[i]=data.get(i);
            }
            return f;
        }

        private final FloatBuffer mVertexBuffer;
        private final float[] mMVPMatrix = new float[16];
        private final float[] mViewMatrix = new float[16];

        private int mProgram;
        private int mPositionHandle;
        private int mMatrixHandle;

        MyRenderer() {
            mVertexBuffer = ByteBuffer.allocateDirect(createPositions().length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(createPositions());
            mVertexBuffer.position(0);
        }

        static int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

            mProgram = GLES20.glCreateProgram();
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);

            GLES20.glUseProgram(mProgram);

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                    12, mVertexBuffer);
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {


            GLES20.glViewport(0, 0, width, height);

            // Set the camera position (View matrix)
            Matrix.setLookAtM(mViewMatrix, 0, camera1_position[0], camera1_position[1], camera1_position[2], 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            Matrix.perspectiveM(mMVPMatrix, 0, 45, (float) width / height, 0.1f, 100f);
            // Calculate the projection and view transformation
            Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mViewMatrix, 0);

        }

        @Override
        public void onDrawFrame(GL10 unused) {
            float[] tmpMatrix = new float[16];
            float radius = camera1_position[2] + 2.5f;
            camera1_position[2] = camera1_position[2];
            //camera1_position[2]= (float) (radius*Math.cos(Math.toRadians(angle))) - 2.5f;
            camera1_position[0]= (float) (radius*Math.sin(Math.toRadians(angle)))+0.01f;
            angle +=0.1;
            Matrix.setLookAtM(mViewMatrix, 0, camera1_position[0], camera1_position[1], camera1_position[2], 0f, 0f, -2.5f, 0f, 1.0f, 0.0f);
            System.arraycopy(mMVPMatrix, 0, tmpMatrix, 0, 16);
            Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mViewMatrix, 0);
            GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 10000);
            System.arraycopy(tmpMatrix, 0, mMVPMatrix, 0, 16);
        }
    }
    private static class MyRenderer2 implements GLSurfaceView.Renderer {
        float[] camera1_position = {-0.01f,0.0f,0.1f};
        double angle = 0.0f;


        private static final String VERTEX_SHADER =
                "attribute vec4 vPosition;\n"
                        + "uniform mat4 uMVPMatrix;\n"
                        + "void main() {\n"
                        + "  gl_Position = uMVPMatrix * vPosition;\n"
                        + "}";
        private static final String FRAGMENT_SHADER =
                "precision mediump float;\n"
                        + "void main() {\n"
                        + "  gl_FragColor = vec4(0.5, 0, 0, 1);\n"
                        + "}";
        private static final float[] VERTEX = {   // in counterclockwise order:
                0, 1, -2.5f,  // top
                -0.5f, -1, -2.5f,  // bottom left
                1, -1, -2.5f,  // bottom right
        };

        private final FloatBuffer mVertexBuffer;
        private final float[] mMVPMatrix = new float[16];
        private final float[] mViewMatrix = new float[16];


        private int mProgram;
        private int mPositionHandle;
        private int mMatrixHandle;

        MyRenderer2() {
            mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(VERTEX);
            mVertexBuffer.position(0);
        }

        static int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }



        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            mProgram = GLES20.glCreateProgram();
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);
            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                    12, mVertexBuffer);
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            // Set the camera position (View matrix)
            Matrix.setLookAtM(mViewMatrix, 0, camera1_position[0], camera1_position[1], camera1_position[2], 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            Matrix.perspectiveM(mMVPMatrix, 0, 45, (float) width / height, 0.1f, 100f);
            // Calculate the projection and view transformation
            Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mViewMatrix, 0);

        }

        @Override
        public void onDrawFrame(GL10 unused) {
            float[] tmpMatrix = new float[16];
            float radius = 2.6f;
            camera1_position[2] = camera1_position[2];
            //camera1_position[2]= (float) (radius*Math.cos(Math.toRadians(angle))) - 2.5f;
            camera1_position[0]= (float) (radius*Math.sin(Math.toRadians(angle)))-0.01f;
            angle +=0.1;
            Matrix.setLookAtM(mViewMatrix, 0, camera1_position[0], camera1_position[1], camera1_position[2], 0f, 0f, -2.5f, 0f, 1.0f, 0.0f);
            System.arraycopy(mMVPMatrix, 0, tmpMatrix, 0, 16);
            Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mViewMatrix, 0);
            GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
            System.arraycopy(tmpMatrix, 0, mMVPMatrix, 0, 16);
        }
    }
}
/*public class MainActivity extends Activity {   //创建继承Activity的主控制类

    MyTDView mview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//调用父类
        //设置为竖屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mview=new MyTDView(this);//创建MyTDView类而对象
        mview.requestFocus();
        mview.setFocusableInTouchMode(true);
        setContentView(mview);
    }
    @Override
    public void onResume()//继承Activity后重写的onResume方法
    {
        super.onResume();
        mview.onResume();//通过MyTDView类的对象调用onResume方法
    }
    @Override
    public void onPause()//继承Activity后重写的onPause方法
    {
        super.onPause();
        mview.onPause();//通过MyTDView类的对象调用onPause方法
    }
}*/