# Web Viewer Setup Guide

## Quick Start

### 1. Build and Run the Android App

1. Open the project in Android Studio
2. Build the project (Build → Make Project)
3. Run on emulator or device
4. Grant camera permissions when prompted
5. You should see "HTTP Server started on port 8080" toast message

### 2. Set Up Port Forwarding (Emulator Only)

If you're using an Android emulator, forward the port:

```bash
adb forward tcp:8080 tcp:8080
```

**Note:** Skip this step if using a real device on the same network.

### 3. Open the Web Viewer

#### Option A: Simple HTTP Server (Python)

```bash
cd web
python3 -m http.server 3000
```

Then open: http://localhost:3000

#### Option B: Open HTML Directly

Simply open `web/index.html` in your browser (Chrome, Firefox, Safari, etc.)

### 4. Connect to the Stream

1. Click the **"Connect"** button on the web page
2. If successful, you'll see the live camera feed
3. Toggle processing modes on the Android app to see changes:
   - **Raw Camera** - Normal color feed
   - **Canny Edge** - White edges on black background
   - **Grayscale** - Black and white image

## Troubleshooting

### Connection Failed

**Problem:** "Connection failed ✗" message

**Solutions:**
1. Make sure the Android app is running
2. Check if port forwarding is set up (emulator only):
   ```bash
   adb forward tcp:8080 tcp:8080
   ```
3. Verify the server URL is correct (default: http://localhost:8080)
4. Check Android Studio Logcat for HTTP server logs

### No Frames Displayed

**Problem:** Connected but no video appears

**Solutions:**
1. Grant camera permissions in the Android app
2. Check if the camera is working (you should see video on the Android device)
3. Toggle processing modes to verify the app is active
4. Check browser console (F12) for errors

### CORS Errors

**Problem:** Browser console shows CORS errors

**Solution:** The HTTP server includes CORS headers, but if you still see errors:
- Serve the web page through a local HTTP server (Option A above)
- Don't open `index.html` directly with `file://` protocol

### Real Device Connection

If using a real Android device:

1. **Option 1: USB Debugging + Port Forward**
   ```bash
   adb forward tcp:8080 tcp:8080
   ```
   Then connect to http://localhost:8080 from your computer

2. **Option 2: Same WiFi Network**
   - Find your device's IP address in Settings → About Phone → Status
   - Update web viewer URL to `http://<device-ip>:8080`
   - Make sure both devices are on the same network

## Testing Different Modes

1. **Raw Camera Mode**
   - Press "TOGGLE MODE" on Android app until you see "Raw Camera"
   - Web viewer should show normal color camera feed

2. **Canny Edge Detection**
   - Press "TOGGLE MODE" until you see "Edge Detection"
   - Web viewer should show white edges on black background

3. **Grayscale Mode**
   - Press "TOGGLE MODE" until you see "Grayscale"
   - Web viewer should show black and white image

## Performance Notes

- **Android FPS:** ~30 FPS on device/emulator
- **Web Viewer FPS:** ~10 FPS (HTTP polling every 100ms)
- **Latency:** ~100-300ms depending on network and processing

To improve web viewer FPS:
- Edit `web/src/viewer.ts`
- Change `frameInterval: 100` to a lower value (e.g., 50 for 20 FPS)
- Recompile TypeScript: `npx tsc`

## API Endpoints

The Android app HTTP server provides these endpoints:

### GET /frame
Returns the latest processed frame as JPEG image

**Example:**
```bash
curl http://localhost:8080/frame -o frame.jpg
```

### GET /stats
Returns JSON with frame statistics

**Example:**
```bash
curl http://localhost:8080/stats
```

**Response:**
```json
{
  "width": 640,
  "height": 480,
  "mode": "CANNY_EDGE",
  "fps": 30.5,
  "timestamp": 1699876543210
}
```

### GET /
Server info page with live preview

Open http://localhost:8080 in your browser to see a built-in test page.

## Compiling TypeScript (Optional)

If you make changes to `web/src/viewer.ts`:

1. Install Node.js and npm
2. Install TypeScript:
   ```bash
   npm install -g typescript
   ```
3. Compile:
   ```bash
   cd web
   tsc
   ```

The compiled JavaScript will be in `web/dist/viewer.js`.

## Architecture

```
┌─────────────────┐         ┌──────────────────┐
│  Android App    │         │   Web Browser    │
│                 │         │                  │
│  Camera         │         │  Canvas Display  │
│    ↓            │         │        ↑         │
│  OpenCV C++     │  HTTP   │   HTTP Polling   │
│    ↓            │ ←─────→ │   (100ms/frame)  │
│  FrameServer    │ Port    │   TypeScript     │
│  (NanoHTTPD)    │ 8080    │   Viewer         │
│                 │         │                  │
└─────────────────┘         └──────────────────┘
```

## Next Steps

- Test all three processing modes
- Measure end-to-end latency
- Try on a real Android device
- Optimize frame rate if needed
- Document your findings for the assignment submission
