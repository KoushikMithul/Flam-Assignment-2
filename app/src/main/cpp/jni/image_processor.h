#ifndef IMAGE_PROCESSOR_H
#define IMAGE_PROCESSOR_H

#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>
#include <android/log.h>

#define LOG_TAG "ImageProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

class ImageProcessor {
public:
    static cv::Mat processCannyEdge(const cv::Mat& input, double threshold1 = 50, double threshold2 = 150);
    static cv::Mat processGrayscale(const cv::Mat& input);
    static cv::Mat convertYUVtoRGBA(const uint8_t* yuvData, int width, int height);
private:
    ImageProcessor() = default;
};

#endif // IMAGE_PROCESSOR_H
