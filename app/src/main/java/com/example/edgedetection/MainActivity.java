package com.example.edgedetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }

    private GLSurfaceView glSurfaceView;
    private GLRenderer glRenderer;
    private CameraManager cameraManager;
    private TextView fpsText;
    private Button toggleButton;
    private boolean isProcessingEnabled = true;

    private BaseLoaderCallback openCVCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                initializeOpenCV();
                setupCamera();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkCameraPermission()) {
            initializeComponents();
        } else {
            requestCameraPermission();
        }
    }

    private void initializeComponents() {
        glSurfaceView = findViewById(R.id.gl_surface_view);
        fpsText = findViewById(R.id.fps_text);
        toggleButton = findViewById(R.id.toggle_button);

        glRenderer = new GLRenderer();
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(glRenderer);

        toggleButton.setOnClickListener(v -> {
            isProcessingEnabled = !isProcessingEnabled;
            toggleButton.setText(isProcessingEnabled ? "Show Raw" : "Show Processed");
        });
    }

    private void setupCamera() {
        cameraManager = new CameraManager(this);
        cameraManager.setFrameCallback(this::processFrame);
        cameraManager.startCamera();
    }

    private void processFrame(byte[] data, int width, int height) {
        long startTime = System.currentTimeMillis();

        byte[] processedData;
        if (isProcessingEnabled) {
            processedData = processImageNative(data, width, height);
        } else {
            processedData = data;
        }

        glRenderer.updateTexture(processedData, width, height);

        long processingTime = System.currentTimeMillis() - startTime;
        updateFPS(processingTime);
    }

    private void updateFPS(long processingTime) {
        runOnUiThread(() -> {
            int fps = processingTime > 0 ? (int)(1000 / processingTime) : 0;
            fpsText.setText("FPS: " + fps);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initAsync()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, openCVCallback);
        } else {
            openCVCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    // Native methods
    public native void initializeOpenCV();
    public native byte[] processImageNative(byte[] data, int width, int height);

    // Permission handling methods...
}