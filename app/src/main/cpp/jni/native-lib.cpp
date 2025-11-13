#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include "image_processor.h"

extern "C" {

JNIEXPORT void JNICALL
Java_com_flam_edgedetector_NativeProcessor_processCannyEdge(
        JNIEnv *env,
        jobject /* this */,
        jobject inputBitmap,
        jobject outputBitmap,
        jdouble threshold1,
        jdouble threshold2) {
    
    AndroidBitmapInfo inputInfo;
    void *inputPixels;
    AndroidBitmapInfo outputInfo;
    void *outputPixels;
    
    // Get input bitmap info and pixels
    AndroidBitmap_getInfo(env, inputBitmap, &inputInfo);
    AndroidBitmap_lockPixels(env, inputBitmap, &inputPixels);
    
    // Get output bitmap info and pixels
    AndroidBitmap_getInfo(env, outputBitmap, &outputInfo);
    AndroidBitmap_lockPixels(env, outputBitmap, &outputPixels);
    
    // Create OpenCV mat from input bitmap
    cv::Mat inputMat(inputInfo.height, inputInfo.width, CV_8UC4, inputPixels);
    
    // Process with Canny edge detection
    cv::Mat outputMat = ImageProcessor::processCannyEdge(inputMat, threshold1, threshold2);
    
    // Copy result to output bitmap
    memcpy(outputPixels, outputMat.data, outputInfo.height * outputInfo.stride);
    
    // Unlock bitmaps
    AndroidBitmap_unlockPixels(env, inputBitmap);
    AndroidBitmap_unlockPixels(env, outputBitmap);
}

JNIEXPORT void JNICALL
Java_com_flam_edgedetector_NativeProcessor_processGrayscale(
        JNIEnv *env,
        jobject /* this */,
        jobject inputBitmap,
        jobject outputBitmap) {
    
    AndroidBitmapInfo inputInfo;
    void *inputPixels;
    AndroidBitmapInfo outputInfo;
    void *outputPixels;
    
    AndroidBitmap_getInfo(env, inputBitmap, &inputInfo);
    AndroidBitmap_lockPixels(env, inputBitmap, &inputPixels);
    
    AndroidBitmap_getInfo(env, outputBitmap, &outputInfo);
    AndroidBitmap_lockPixels(env, outputBitmap, &outputPixels);
    
    cv::Mat inputMat(inputInfo.height, inputInfo.width, CV_8UC4, inputPixels);
    cv::Mat outputMat = ImageProcessor::processGrayscale(inputMat);
    
    memcpy(outputPixels, outputMat.data, outputInfo.height * outputInfo.stride);
    
    AndroidBitmap_unlockPixels(env, inputBitmap);
    AndroidBitmap_unlockPixels(env, outputBitmap);
}

JNIEXPORT jstring JNICALL
Java_com_flam_edgedetector_NativeProcessor_getVersionInfo(
        JNIEnv *env,
        jobject /* this */) {
    std::string version = "OpenCV " + std::string(CV_VERSION) + " | C++17 | JNI";
    return env->NewStringUTF(version.c_str());
}

} // extern "C"
