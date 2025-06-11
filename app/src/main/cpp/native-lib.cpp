#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>

#define LOG_TAG "EdgeDetection"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

using namespace cv;

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_edgedetection_MainActivity_initializeOpenCV(JNIEnv *env, jobject thiz) {
    LOGI("OpenCV initialized successfully");
}

JNIEXPORT jbyteArray JNICALL
Java_com_example_edgedetection_MainActivity_processImageNative(
        JNIEnv *env,
        jobject thiz,
        jbyteArray data,
        jint width,
        jint height) {

    // Convert Java byte array to native
    jbyte* imageData = env->GetByteArrayElements(data, nullptr);
    jsize dataSize = env->GetArrayLength(data);

    // Create OpenCV Mat from YUV data (assuming YUV_420_888)
    Mat yuvMat(height + height/2, width, CV_8UC1, (unsigned char*)imageData);
    Mat rgbMat;
    Mat grayMat;
    Mat edgeMat;

    // Convert YUV to RGB
    cvtColor(yuvMat, rgbMat, COLOR_YUV2RGB_I420);

    // Convert to grayscale
    cvtColor(rgbMat, grayMat, COLOR_RGB2GRAY);

    // Apply Gaussian blur to reduce noise
    Mat blurredMat;
    GaussianBlur(grayMat, blurredMat, Size(5, 5), 1.5);

    // Apply Canny edge detection
    Canny(blurredMat, edgeMat, 50, 150);

    // Create output byte array
    int outputSize = edgeMat.total() * edgeMat.elemSize();
    jbyteArray result = env->NewByteArray(outputSize);

    // Copy processed data back
    env->SetByteArrayRegion(result, 0, outputSize, (jbyte*)edgeMat.data);

    // Release input array
    env->ReleaseByteArrayElements(data, imageData, JNI_ABORT);

    return result;
}

// Alternative processing function for different filters
JNIEXPORT jbyteArray JNICALL
Java_com_example_edgedetection_MainActivity_processGrayscale(
        JNIEnv *env,
        jobject thiz,
        jbyteArray data,
        jint width,
        jint height) {

    jbyte* imageData = env->GetByteArrayElements(data, nullptr);

    Mat yuvMat(height + height/2, width, CV_8UC1, (unsigned char*)imageData);
    Mat rgbMat;
    Mat grayMat;

    cvtColor(yuvMat, rgbMat, COLOR_YUV2RGB_I420);
    cvtColor(rgbMat, grayMat, COLOR_RGB2GRAY);

    int outputSize = grayMat.total() * grayMat.elemSize();
    jbyteArray result = env->NewByteArray(outputSize);

    env->SetByteArrayRegion(result, 0, outputSize, (jbyte*)grayMat.data);
    env->ReleaseByteArrayElements(data, imageData, JNI_ABORT);

    return result;
}

}