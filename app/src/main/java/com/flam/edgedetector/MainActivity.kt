package com.flam.edgedetector

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glRenderer: GLRenderer
    private lateinit var btnToggleMode: Button
    private lateinit var tvFps: TextView
    private lateinit var tvMode: TextView
    private lateinit var tvVersion: TextView
    
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProcessor: CameraProcessor
    
    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize views
        glSurfaceView = findViewById(R.id.glSurfaceView)
        btnToggleMode = findViewById(R.id.btnToggleMode)
        tvFps = findViewById(R.id.tvFps)
        tvMode = findViewById(R.id.tvMode)
        tvVersion = findViewById(R.id.tvVersion)
        
        // Setup OpenGL ES 2.0
        glSurfaceView.setEGLContextClientVersion(2)
        glRenderer = GLRenderer()
        glSurfaceView.setRenderer(glRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        
        // Display version info
        try {
            val versionInfo = NativeProcessor.getVersionInfo()
            tvVersion.text = versionInfo
            Log.i(TAG, "Native library info: $versionInfo")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load native library", e)
            tvVersion.text = "Error loading native library"
        }
        
        // Setup camera processor
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProcessor = CameraProcessor { bitmap, fps ->
            runOnUiThread {
                glRenderer.updateBitmap(bitmap)
                glSurfaceView.requestRender()
                tvFps.text = "FPS: ${"%.1f".format(fps)}"
            }
        }
        
        // Toggle button
        updateModeDisplay()
        btnToggleMode.setOnClickListener {
            toggleProcessingMode()
        }
        
        // Check permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            // Preview (not used, but can be added for debugging)
            val preview = Preview.Builder().build()
            
            // Image analysis
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, cameraProcessor)
                }
            
            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()
                
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalyzer
                )
                
                Log.i(TAG, "Camera started successfully")
                
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                Toast.makeText(this, "Camera initialization failed", Toast.LENGTH_SHORT).show()
            }
            
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun toggleProcessingMode() {
        cameraProcessor.processingMode = when (cameraProcessor.processingMode) {
            CameraProcessor.ProcessingMode.RAW -> CameraProcessor.ProcessingMode.CANNY_EDGE
            CameraProcessor.ProcessingMode.CANNY_EDGE -> CameraProcessor.ProcessingMode.GRAYSCALE
            CameraProcessor.ProcessingMode.GRAYSCALE -> CameraProcessor.ProcessingMode.RAW
        }
        updateModeDisplay()
    }
    
    private fun updateModeDisplay() {
        val modeName = when (cameraProcessor.processingMode) {
            CameraProcessor.ProcessingMode.RAW -> "Raw Camera"
            CameraProcessor.ProcessingMode.CANNY_EDGE -> "Edge Detection"
            CameraProcessor.ProcessingMode.GRAYSCALE -> "Grayscale"
        }
        tvMode.text = "Mode: $modeName"
        Log.i(TAG, "Processing mode changed to: $modeName")
    }
    
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    
    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }
    
    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }
}
