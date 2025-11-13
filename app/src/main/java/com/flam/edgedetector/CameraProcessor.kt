package com.flam.edgedetector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicLong

class CameraProcessor(
    private val onFrameProcessed: (Bitmap, Float) -> Unit
) : ImageAnalysis.Analyzer {
    
    enum class ProcessingMode {
        RAW,           // No processing
        CANNY_EDGE,    // Canny edge detection
        GRAYSCALE      // Grayscale filter
    }
    
    var processingMode = ProcessingMode.CANNY_EDGE
    
    private var frameCount = 0L
    private var lastFpsTime = System.currentTimeMillis()
    private var currentFps = 0f
    
    private val lastProcessTime = AtomicLong(0)
    
    companion object {
        private const val TAG = "CameraProcessor"
        private const val FPS_UPDATE_INTERVAL = 1000L // Update FPS every second
    }
    
    override fun analyze(image: ImageProxy) {
        val startTime = System.currentTimeMillis()
        
        try {
            // Convert ImageProxy to Bitmap
            val bitmap = imageProxyToBitmap(image)
            
            // Process based on current mode
            val processedBitmap = when (processingMode) {
                ProcessingMode.RAW -> bitmap
                ProcessingMode.CANNY_EDGE -> processCannyEdge(bitmap)
                ProcessingMode.GRAYSCALE -> processGrayscale(bitmap)
            }
            
            // Calculate FPS
            frameCount++
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastFpsTime >= FPS_UPDATE_INTERVAL) {
                currentFps = (frameCount * 1000f) / (currentTime - lastFpsTime)
                frameCount = 0
                lastFpsTime = currentTime
            }
            
            // Callback with processed frame and FPS
            onFrameProcessed(processedBitmap, currentFps)
            
            val processTime = System.currentTimeMillis() - startTime
            lastProcessTime.set(processTime)
            
            if (frameCount % 30 == 0L) {
                Log.d(TAG, "Processing time: ${processTime}ms, FPS: ${"%.1f".format(currentFps)}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame", e)
        } finally {
            image.close()
        }
    }
    
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        
        val nv21 = ByteArray(ySize + uSize + vSize)
        
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 85, out)
        val imageBytes = out.toByteArray()
        
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
    
    private fun processCannyEdge(inputBitmap: Bitmap): Bitmap {
        val outputBitmap = Bitmap.createBitmap(
            inputBitmap.width,
            inputBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        
        NativeProcessor.processCannyEdge(inputBitmap, outputBitmap, 50.0, 150.0)
        return outputBitmap
    }
    
    private fun processGrayscale(inputBitmap: Bitmap): Bitmap {
        val outputBitmap = Bitmap.createBitmap(
            inputBitmap.width,
            inputBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        
        NativeProcessor.processGrayscale(inputBitmap, outputBitmap)
        return outputBitmap
    }
    
    fun getLastProcessingTime(): Long = lastProcessTime.get()
}
