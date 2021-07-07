package com.google.mediapipe.apps.basic;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.glutil.EglManager;

import java.util.HashMap;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {
    private static final String TAG = "MainActivity2";

    private SurfaceTexture previewFrameTexture;
    private SurfaceView previewSurfaceView;
    private CameraXPreviewHelper cameraHelper;
    private ApplicationInfo applicationInfo;
    private EglManager eglManager;
    private ExternalTextureConverter converter;
    private FrameProcessor processor;

    private static final String INPUT_NUM_HANDS_SIDE_PACKET_NAME = "num_hands";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks";
    // Max number of hands to detect/process.
    private static final int NUM_HANDS = 2;

    private static final boolean FLIP_FRAMES_VERTICALLY = true;

    static {
        System.loadLibrary("mediapipe_jni");
        try {
            System.loadLibrary("opencv_java3");
        } catch (java.lang.UnsatisfiedLinkError e) {
            // Some example apps (e.g. template matching) require OpenCV 4.
            System.loadLibrary("opencv_java4");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "inside onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewSurfaceView = new SurfaceView(this);
        setupSurfaceView();

        try {
            applicationInfo = getPackageManager().getApplicationInfo(getPackageName(),
                    PackageManager.GET_META_DATA);
        } catch (Exception e) {
            Log.e(TAG, "Cannot find application Info: " + e);
        }

        AndroidAssetUtil.initializeNativeAssetManager(this);

        eglManager = new EglManager(null);
        processor = new FrameProcessor(
                this,
                eglManager.getNativeContext(),
                applicationInfo.metaData.getString("binaryGraphName"),
                applicationInfo.metaData.getString("inputVideoStreamName"),
                applicationInfo.metaData.getString("outputVideoStreamName")
        );

        AndroidPacketCreator packetCreator = processor.getPacketCreator();
        Map<String, Packet> inputSidePackets = new HashMap<>();
        inputSidePackets.put(INPUT_NUM_HANDS_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_HANDS));
        processor.setInputSidePackets(inputSidePackets);

        PermissionHelper.checkAndRequestCameraPermissions(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        converter = new ExternalTextureConverter(eglManager.getContext());
        converter.setConsumer(processor);

        if (PermissionHelper.cameraPermissionsGranted(this)){
            startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "inside onPause");
        if (converter != null) {
            converter.close();
        }
    }

    public void startCamera() {
        cameraHelper = new CameraXPreviewHelper();
        cameraHelper.setOnCameraStartedListener(surfaceTexture -> {
            previewFrameTexture = surfaceTexture;
            previewSurfaceView.setVisibility(View.VISIBLE);
        });

        CameraHelper.CameraFacing cameraFacing = applicationInfo.metaData.getBoolean(
                "cameraFacingFront", false)
                ? CameraHelper.CameraFacing.FRONT : CameraHelper.CameraFacing.BACK;
        cameraHelper.startCamera(this, cameraFacing, null, null);
    }

    private void setupSurfaceView() {
        previewSurfaceView.setVisibility(View.GONE);
        ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
        viewGroup.addView(previewSurfaceView);

        previewSurfaceView
                .getHolder()
                .addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder surfaceHolder) {
                        processor.getVideoSurfaceOutput().setSurface(surfaceHolder.getSurface());
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                        Log.i(TAG, "Sizes : i X i1 X i2 ::: "+ i + " X " + i1 + " X " + i2);
                        Size viewSize = new Size(i1, i2);
                        Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
                        boolean isCameraRotated = cameraHelper.isCameraRotated();
                        converter.setSurfaceTextureAndAttachToGLContext(
                                previewFrameTexture,
                                isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
                                isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                        processor.getVideoSurfaceOutput().setSurface(null);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "inside onDestroy");
    }
}
