package com.google.mediapipe.apps.handtrackinggpu;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGlCubeRenderer implements GLSurfaceView.Renderer {

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] MVPMatrix = new float[16];

    private final FloatBuffer cubePositions;
    private final FloatBuffer cubeColors;

    private int MVPMatrixHandle;
    private int MVMatrixHandle;
    private int positionHandle;
    private int colorHandle;

    private final int bytesPerFLoat = 4;
    private final int positionDataSize = 3;
    private final int colorDataSize = 4;

    private int perVertexProgramHandle;

    public MyGlCubeRenderer() {
        final float[] cubePositionData =
                {
                        // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
                        // if the points are counter-clockwise we are looking at the "front". If not we are looking at
                        // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
                        // usually represent the backside of an object and aren't visible anyways.

                        // Front face
                        -1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f,
                        1.0f, -1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f,

                        // Right face
                        1.0f, 1.0f, 1.0f,
                        1.0f, -1.0f, 1.0f,
                        1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f,
                        1.0f, -1.0f, -1.0f,
                        1.0f, 1.0f, -1.0f,

                        // Back face
                        1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f,
                        -1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f, 1.0f, -1.0f,

                        // Left face
                        -1.0f, 1.0f, -1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f, 1.0f,
                        -1.0f, 1.0f, 1.0f,

                        // Top face
                        -1.0f, 1.0f, -1.0f,
                        -1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, -1.0f,
                        -1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, -1.0f,

                        // Bottom face
                        1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f,
                };

        final float[] cubeColorData =
                {
                        // Front face (red)
                        1.0f, 0.0f, 0.0f, 0.5f,
                        1.0f, 0.0f, 0.0f, 0.5f,
                        1.0f, 0.0f, 0.0f, 0.5f,
                        1.0f, 0.0f, 0.0f, 0.5f,
                        1.0f, 0.0f, 0.0f, 0.5f,
                        1.0f, 0.0f, 0.0f, 0.5f,

                        // Right face (green)
                        0.0f, 1.0f, 0.0f, 0.5f,
                        0.0f, 1.0f, 0.0f, 0.5f,
                        0.0f, 1.0f, 0.0f, 0.5f,
                        0.0f, 1.0f, 0.0f, 0.5f,
                        0.0f, 1.0f, 0.0f, 0.5f,
                        0.0f, 1.0f, 0.0f, 0.5f,

                        // Back face (blue)
                        0.0f, 0.0f, 1.0f, 0.5f,
                        0.0f, 0.0f, 1.0f, 0.5f,
                        0.0f, 0.0f, 1.0f, 0.5f,
                        0.0f, 0.0f, 1.0f, 0.5f,
                        0.0f, 0.0f, 1.0f, 0.5f,
                        0.0f, 0.0f, 1.0f, 0.5f,

                        // Left face (yellow)
                        1.0f, 1.0f, 0.0f, 0.5f,
                        1.0f, 1.0f, 0.0f, 0.5f,
                        1.0f, 1.0f, 0.0f, 0.5f,
                        1.0f, 1.0f, 0.0f, 0.5f,
                        1.0f, 1.0f, 0.0f, 0.5f,
                        1.0f, 1.0f, 0.0f, 0.5f,

                        // Top face (cyan)
                        0.0f, 1.0f, 1.0f, 0.5f,
                        0.0f, 1.0f, 1.0f, 0.5f,
                        0.0f, 1.0f, 1.0f, 0.5f,
                        0.0f, 1.0f, 1.0f, 0.5f,
                        0.0f, 1.0f, 1.0f, 0.5f,
                        0.0f, 1.0f, 1.0f, 0.5f,

                        // Bottom face (magenta)
                        1.0f, 0.0f, 1.0f, 0.5f,
                        1.0f, 0.0f, 1.0f, 0.5f,
                        1.0f, 0.0f, 1.0f, 0.5f,
                        1.0f, 0.0f, 1.0f, 0.5f,
                        1.0f, 0.0f, 1.0f, 0.5f,
                        1.0f, 0.0f, 1.0f, 0.5f
                };

        cubePositions = ByteBuffer.allocateDirect(cubePositionData.length * bytesPerFLoat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        cubePositions.put(cubePositionData).position(0);

        cubeColors = ByteBuffer.allocateDirect(cubeColorData.length * bytesPerFLoat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        cubeColors.put(cubeColorData).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        //GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        final String vertexShader =
                "uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.

                        + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color;        \n"		// Per-vertex color information we will pass in.

                        + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.

                        + "void main()                    \n"		// The entry point for our vertex shader.
                        + "{                              \n"
                        + "   v_Color = a_Color;          \n"		// Pass the color through to the fragment shader.
                        // It will be interpolated across the triangle.
                        + "   gl_Position = u_MVPMatrix   \n" 	// gl_Position is a special variable used to store the final position.
                        + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
                        + "}                              \n";    // normalized screen coordinates.

        final String fragmentShader =
                "precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the
                        // triangle per fragment.
                        + "void main()                    \n"		// The entry point for our fragment shader.
                        + "{                              \n"
                        + "   gl_FragColor = v_Color;     \n"		// Pass the color directly through the pipeline.
                        + "}                              \n";

        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        if (vertexShaderHandle != 0) {
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);
            GLES20.glCompileShader(vertexShaderHandle);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }
        if (vertexShaderHandle == 0){
            throw new RuntimeException("error in vertex shader");
        }

        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        if (fragmentShaderHandle != 0) {
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
            GLES20.glCompileShader(fragmentShaderHandle);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }
        if (fragmentShaderHandle == 0){
            throw new RuntimeException("error in fragment shader");
        }

        perVertexProgramHandle = GLES20.glCreateProgram();
        if (perVertexProgramHandle != 0) {
            GLES20.glAttachShader(perVertexProgramHandle, vertexShaderHandle);
            GLES20.glAttachShader(perVertexProgramHandle, fragmentShaderHandle);
            GLES20.glBindAttribLocation(perVertexProgramHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(perVertexProgramHandle, 1, "a_Color");
            GLES20.glLinkProgram(perVertexProgramHandle);
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(perVertexProgramHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(perVertexProgramHandle);
                perVertexProgramHandle = 0;
            }
        }
        if (perVertexProgramHandle == 0) {
            throw new RuntimeException("error in program");
        }

        MVPMatrixHandle = GLES20.glGetUniformLocation(perVertexProgramHandle, "u_MVPMatrix");
        MVMatrixHandle = GLES20.glGetUniformLocation(perVertexProgramHandle, "u_MVMatrix");
        positionHandle = GLES20.glGetAttribLocation(perVertexProgramHandle, "a_Position");
        colorHandle = GLES20.glGetAttribLocation(perVertexProgramHandle, "a_Color");

        GLES20.glUseProgram(perVertexProgramHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        float sizeInDegrees = (1.0f / 10000.0f) * ((int) time);

        //Log.i("Renderer","coords vals - " + arCoordinates.xLoc + ", " + arCoordinates.yLoc + ", " + arCoordinates.zLoc);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, (arCoordinates.xLoc - 0.5f) * 7,-(arCoordinates.yLoc - 0.5f) * 15,-5.0f);
        /*if (sizeInDegrees < 1) {
            Matrix.scaleM(modelMatrix, 0, sizeInDegrees, sizeInDegrees, sizeInDegrees);
        }*/
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.scaleM(modelMatrix, 0, arCoordinates.scale, arCoordinates.scale, arCoordinates.scale);
        Matrix.rotateM(modelMatrix, 0, 45, 0.0f, 0.0f, 1.0f);
        Matrix.rotateM(modelMatrix, 0, 45, 1.0f, 0.0f, 0.0f);

        cubePositions.position(0);
        GLES20.glVertexAttribPointer(positionHandle, positionDataSize, GLES20.GL_FLOAT, false, 0, cubePositions);
        GLES20.glEnableVertexAttribArray(positionHandle);
        cubeColors.position(0);
        GLES20.glVertexAttribPointer(colorHandle, colorDataSize, GLES20.GL_FLOAT, false, 0, cubeColors);
        GLES20.glEnableVertexAttribArray(colorHandle);

        Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        GLES20.glUniformMatrix4fv(MVMatrixHandle, 1, false, MVPMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVPMatrix, 0);
        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, MVPMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    }
}
