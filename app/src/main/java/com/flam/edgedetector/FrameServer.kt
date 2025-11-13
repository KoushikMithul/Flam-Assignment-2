package com.flam.edgedetector

import android.graphics.Bitmap
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Simple HTTP server that serves the latest processed frame
 * Runs on port 8080 and provides /frame endpoint
 */
class FrameServer(private val port: Int = 8080) : NanoHTTPD(port) {
    private var latestFrame: ByteArray? = null
    private var frameStats: FrameStats? = null
    private val lock = ReentrantReadWriteLock()
    
    companion object {
        private const val TAG = "FrameServer"
        private const val JPEG_QUALITY = 85
    }
    
    data class FrameStats(
        val width: Int,
        val height: Int,
        val mode: String,
        val fps: Double,
        val timestamp: Long
    )
    
    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        Log.d(TAG, "Request received: $uri")
        
        // Add CORS headers for web access
        val response = when (uri) {
            "/frame" -> serveFrame()
            "/stats" -> serveStats()
            "/" -> serveInfo()
            else -> newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                MIME_PLAINTEXT,
                "Endpoint not found. Available: /frame, /stats, /"
            )
        }
        
        // Add CORS headers
        response.addHeader("Access-Control-Allow-Origin", "*")
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS")
        response.addHeader("Access-Control-Allow-Headers", "Content-Type")
        
        return response
    }
    
    private fun serveFrame(): Response {
        val frame = lock.read { latestFrame }
        
        return if (frame != null) {
            newFixedLengthResponse(
                Response.Status.OK,
                "image/jpeg",
                frame.inputStream(),
                frame.size.toLong()
            ).apply {
                addHeader("Cache-Control", "no-cache, no-store, must-revalidate")
                addHeader("Pragma", "no-cache")
                addHeader("Expires", "0")
            }
        } else {
            newFixedLengthResponse(
                Response.Status.SERVICE_UNAVAILABLE,
                MIME_PLAINTEXT,
                "No frame available yet"
            )
        }
    }
    
    private fun serveStats(): Response {
        val stats = lock.read { frameStats }
        
        return if (stats != null) {
            val json = """
                {
                    "width": ${stats.width},
                    "height": ${stats.height},
                    "mode": "${stats.mode}",
                    "fps": ${stats.fps},
                    "timestamp": ${stats.timestamp}
                }
            """.trimIndent()
            
            newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                json
            )
        } else {
            newFixedLengthResponse(
                Response.Status.SERVICE_UNAVAILABLE,
                "application/json",
                """{"error": "No stats available"}"""
            )
        }
    }
    
    private fun serveInfo(): Response {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Flam Edge Detector Server</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    h1 { color: #3DDC84; }
                    code { background: #f4f4f4; padding: 2px 6px; border-radius: 3px; }
                    .endpoint { margin: 20px 0; padding: 10px; background: #f9f9f9; border-left: 4px solid #3DDC84; }
                </style>
            </head>
            <body>
                <h1>🎥 Flam Edge Detector - HTTP Server</h1>
                <p>Server is running on port $port</p>
                
                <h2>Available Endpoints:</h2>
                
                <div class="endpoint">
                    <h3>GET /frame</h3>
                    <p>Returns the latest processed frame as JPEG image</p>
                    <code>curl http://localhost:$port/frame -o frame.jpg</code>
                </div>
                
                <div class="endpoint">
                    <h3>GET /stats</h3>
                    <p>Returns JSON with frame statistics (width, height, mode, fps, timestamp)</p>
                    <code>curl http://localhost:$port/stats</code>
                </div>
                
                <div class="endpoint">
                    <h3>GET /</h3>
                    <p>This info page</p>
                </div>
                
                <h2>Test the Stream:</h2>
                <img id="stream" style="max-width: 100%; border: 2px solid #3DDC84;" />
                <p id="stats"></p>
                
                <script>
                    const img = document.getElementById('stream');
                    const stats = document.getElementById('stats');
                    
                    function updateFrame() {
                        img.src = '/frame?' + Date.now();
                    }
                    
                    function updateStats() {
                        fetch('/stats')
                            .then(r => r.json())
                            .then(data => {
                                stats.textContent = 'Mode: ' + data.mode + ' | FPS: ' + data.fps.toFixed(1) + ' | Resolution: ' + data.width + 'x' + data.height;
                            })
                            .catch(() => {});
                    }
                    
                    setInterval(updateFrame, 100);
                    setInterval(updateStats, 500);
                    updateFrame();
                </script>
            </body>
            </html>
        """.trimIndent()
        
        return newFixedLengthResponse(
            Response.Status.OK,
            "text/html",
            html
        )
    }
    
    /**
     * Update the latest frame to be served
     * Converts bitmap to JPEG in background to avoid blocking camera thread
     */
    fun updateFrame(bitmap: Bitmap, mode: String, fps: Double) {
        try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            val jpegData = outputStream.toByteArray()
            
            lock.write {
                latestFrame = jpegData
                frameStats = FrameStats(
                    width = bitmap.width,
                    height = bitmap.height,
                    mode = mode,
                    fps = fps,
                    timestamp = System.currentTimeMillis()
                )
            }
            
            Log.d(TAG, "Frame updated: ${jpegData.size / 1024}KB, mode=$mode, fps=$fps")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating frame", e)
        }
    }
    
    /**
     * Start the HTTP server
     */
    fun startServer() {
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            Log.i(TAG, "✅ HTTP Server started on port $port")
            Log.i(TAG, "📱 Access from browser: http://localhost:$port")
            Log.i(TAG, "🖼️  Frame endpoint: http://localhost:$port/frame")
        } catch (e: IOException) {
            Log.e(TAG, "❌ Failed to start HTTP server", e)
        }
    }
    
    /**
     * Stop the HTTP server
     */
    fun stopServer() {
        stop()
        Log.i(TAG, "HTTP Server stopped")
    }
}
