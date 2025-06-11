package com.example.edgedetection.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import java.nio.ByteBuffer;

public class CameraManager {
    private android.hardware.camera2.CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private FrameCallback frameCallback;

    public interface FrameCallback {
        void onFrameAvailable(byte[] data, int width, int height);
    }

    public CameraManager(Context context) {
        cameraManager = (android.hardware.camera2.CameraManager)
                context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void setFrameCallback(FrameCallback callback) {
        this.frameCallback = callback;
    }

    public void startCamera() {
        startBackgroundThread();
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics characteristics =
                    cameraManager.getCameraCharacteristics(cameraId);

            Size[] sizes = characteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.YUV_420_888);

            Size previewSize = sizes[0]; // Use first available size

            imageReader = ImageReader.newInstance(
                    previewSize.getWidth(),
                    previewSize.getHeight(),
                    ImageFormat.YUV_420_888,
                    2
            );

            imageReader.setOnImageAvailableListener(
                    imageAvailableListener,
                    backgroundHandler
            );

            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            createCaptureSession();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private void createCaptureSession() {
        try {
            cameraDevice.createCaptureSession(
                    Arrays.asList(imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            captureSession = session;
                            startRepeatingRequest();
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {}
                    },
                    backgroundHandler
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRepeatingRequest() {
        try {
            CaptureRequest.Builder builder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(imageReader.getSurface());

            captureSession.setRepeatingRequest(
                    builder.build(),
                    null,
                    backgroundHandler
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ImageReader.OnImageAvailableListener imageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    if (image != null && frameCallback != null) {
                        byte[] data = imageToByteArray(image);
                        frameCallback.onFrameAvailable(
                                data,
                                image.getWidth(),
                                image.getHeight()
                        );
                        image.close();
                    }
                }
            };

    private byte[] imageToByteArray(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] data = new byte[ySize + uSize + vSize];
        yBuffer.get(data, 0, ySize);
        uBuffer.get(data, ySize, uSize);
        vBuffer.get(data, ySize + uSize, vSize);

        return data;
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    public void stopCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        stopBackgroundThread();
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}