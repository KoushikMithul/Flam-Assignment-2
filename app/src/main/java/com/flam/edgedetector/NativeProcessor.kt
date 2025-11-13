package com.flam.edgedetector

import android.graphics.Bitmap

object NativeProcessor {
    init {
        System.loadLibrary("native-lib")
    }
    
    /**
     * Process image with Canny edge detection
     * @param inputBitmap Source bitmap
     * @param outputBitmap Destination bitmap (must be same size as input)
     * @param threshold1 First threshold for hysteresis procedure
     * @param threshold2 Second threshold for hysteresis procedure
     */
    external fun processCannyEdge(
        inputBitmap: Bitmap,
        outputBitmap: Bitmap,
        threshold1: Double = 50.0,
        threshold2: Double = 150.0
    )
    
    /**
     * Convert image to grayscale
     * @param inputBitmap Source bitmap
     * @param outputBitmap Destination bitmap (must be same size as input)
     */
    external fun processGrayscale(
        inputBitmap: Bitmap,
        outputBitmap: Bitmap
    )
    
    /**
     * Get OpenCV and native library version information
     * @return Version string
     */
    external fun getVersionInfo(): String
}
