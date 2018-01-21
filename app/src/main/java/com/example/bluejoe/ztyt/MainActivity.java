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
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Utils.supportGlEs20(this)) {
            Toast.makeText(this, "GLES 2.0 not supported!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.surface);

        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView.setRenderer(new MyRenderer());
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    private static class MyRenderer implements GLSurfaceView.Renderer {
        float[] camera1_position = {0.0f,0.0f,0.1f};
        private int[][] viewport={{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
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

        MyRenderer() {
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
            /*viewport[0][0]=0;
            viewport[0][1]=0;
            viewport[0][2]=width/2;
            viewport[0][3]=height;


            viewport[1][0]=width/2;
            viewport[1][1]=0;
            viewport[1][2]=width/2;
            viewport[1][3]=height;*/

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
            /*for(int i=0;i<2;i++){
                unused.glViewport(viewport[i][0], viewport[i][1], viewport[i][2], viewport[i][3]);
                unused.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
                unused.glLoadIdentity();
            }*/
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            //camera1_position[2]= (float) (radius*Math.cos(Math.toRadians(angle))) - 2.5f;
            camera1_position[0]= (float) (radius*Math.sin(Math.toRadians(angle)));
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