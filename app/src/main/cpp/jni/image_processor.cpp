#include "image_processor.h"
#include <chrono>

cv::Mat ImageProcessor::processCannyEdge(const cv::Mat& input, double threshold1, double threshold2) {
    auto start = std::chrono::high_resolution_clock::now();
    
    cv::Mat gray, edges, output;
    
    // Convert to grayscale if needed
    if (input.channels() == 4) {
        cv::cvtColor(input, gray, cv::COLOR_RGBA2GRAY);
    } else if (input.channels() == 3) {
        cv::cvtColor(input, gray, cv::COLOR_RGB2GRAY);
    } else {
        gray = input.clone();
    }
    
    // Apply Gaussian blur to reduce noise
    cv::GaussianBlur(gray, gray, cv::Size(5, 5), 1.4);
    
    // Apply Canny edge detection
    cv::Canny(gray, edges, threshold1, threshold2);
    
    // Convert back to RGBA for display
    cv::cvtColor(edges, output, cv::COLOR_GRAY2RGBA);
    
    auto end = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end - start);
    LOGI("Canny processing time: %ld ms", duration.count());
    
    return output;
}

cv::Mat ImageProcessor::processGrayscale(const cv::Mat& input) {
    cv::Mat gray, output;
    
    if (input.channels() == 4) {
        cv::cvtColor(input, gray, cv::COLOR_RGBA2GRAY);
        cv::cvtColor(gray, output, cv::COLOR_GRAY2RGBA);
    } else if (input.channels() == 3) {
        cv::cvtColor(input, gray, cv::COLOR_RGB2GRAY);
        cv::cvtColor(gray, output, cv::COLOR_GRAY2RGBA);
    } else {
        cv::cvtColor(input, output, cv::COLOR_GRAY2RGBA);
    }
    
    return output;
}

cv::Mat ImageProcessor::convertYUVtoRGBA(const uint8_t* yuvData, int width, int height) {
    cv::Mat yuv(height + height / 2, width, CV_8UC1, (void*)yuvData);
    cv::Mat rgba;
    cv::cvtColor(yuv, rgba, cv::COLOR_YUV2RGBA_NV21);
    return rgba;
}
