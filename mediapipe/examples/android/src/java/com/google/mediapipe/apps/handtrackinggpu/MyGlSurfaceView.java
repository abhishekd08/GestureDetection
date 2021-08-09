package com.google.mediapipe.apps.handtrackinggpu;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

import com.google.mediapipe.apps.handtrackinggpu.MyGlCubeRenderer;

public class MyGlSurfaceView extends GLSurfaceView {

    private final MyGlCubeRenderer renderer;

    public MyGlSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        // put GLSurfaceView on top
        setZOrderOnTop(true);
        renderer = new MyGlCubeRenderer();
        setRenderer(renderer);
    }
}
