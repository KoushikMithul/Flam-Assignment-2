# 🚀 Quick Start Guide

This guide will help you build and run the Edge Detector project in **under 15 minutes**.

## ⚡ Prerequisites Check

Before starting, verify you have:

```bash
# Check Android Studio
which android-studio || echo "❌ Install Android Studio"

# Check Java
java -version

# Check Node.js (for web viewer)
node --version
npm --version
```

## 📋 Step-by-Step Setup

### 1️⃣ Clone Repository

```bash
git clone https://github.com/KoushikMithul/Flam-Assignment-2.git
cd Flam-Assignment-2
```

### 2️⃣ Setup OpenCV SDK (CRITICAL!)

**Option A: Automated Script (Recommended)**

```bash
chmod +x setup-opencv.sh
./setup-opencv.sh
```

**Option B: Manual Download**

```bash
# Download
wget https://github.com/opencv/opencv/releases/download/4.8.0/opencv-4.8.0-android-sdk.zip

# Extract
unzip opencv-4.8.0-android-sdk.zip

# Move to project
mv opencv-4.8.0-android-sdk/sdk app/src/main/cpp/opencv-sdk
```

**Verify OpenCV Setup:**

```bash
ls app/src/main/cpp/opencv-sdk/sdk/native/jni
# Should show: abi-armeabi-v7a, abi-arm64-v8a, etc.
```

### 3️⃣ Open in Android Studio

```bash
# Option 1: Command line (macOS)
open -a "Android Studio" .

# Option 2: From Android Studio
# File > Open > Select Flam-Assignment-2 folder
```

### 4️⃣ Install NDK and CMake

In Android Studio:

1. **Tools** > **SDK Manager**
2. Go to **SDK Tools** tab
3. Check these boxes:
   - ☑️ **NDK (Side by side)** - version 25.x or later
   - ☑️ **CMake** - version 3.22.1 or later
4. Click **Apply** and wait for installation

### 5️⃣ Sync and Build

```bash
# Option 1: In Android Studio
# Click "Sync Project with Gradle Files" icon (top toolbar)

# Option 2: Command line
./gradlew clean assembleDebug
```

### 6️⃣ Run on Device/Emulator

**Using Physical Device (Recommended for performance):**

1. Enable **Developer Options** on your Android device
2. Enable **USB Debugging**
3. Connect via USB
4. Click **Run** ▶️ in Android Studio
5. Select your device

**Using Emulator:**

1. **Tools** > **Device Manager**
2. Create new device (Pixel 5, API 30+)
3. Click **Run** ▶️
4. Select emulator

### 7️⃣ Test TypeScript Web Viewer

```bash
cd web

# Install dependencies
npm install

# Build TypeScript
npm run build

# Start local server
npm run serve

# Open browser to: http://localhost:8080
```

## ✅ Verification Checklist

After running the app:

- [ ] App launches without crashes
- [ ] Camera permission granted
- [ ] Camera feed visible via OpenGL rendering
- [ ] "Toggle Mode" button switches between modes
- [ ] FPS counter shows ~10-15 FPS
- [ ] Logcat shows processing times
- [ ] Web viewer displays sample frame at `http://localhost:8080`

## 🐛 Common Issues & Fixes

### Issue: "OpenCV not found"

```bash
# Check OpenCV path
ls app/src/main/cpp/opencv-sdk/sdk/native/jni

# If missing, re-run setup script
./setup-opencv.sh
```

### Issue: "NDK not configured"

```bash
# Check NDK installation
ls $ANDROID_HOME/ndk/

# If empty, install via Android Studio SDK Manager
```

### Issue: "Camera permission denied"

- Go to device Settings > Apps > Edge Detector
- Grant Camera permission manually

### Issue: Low FPS / App crashes

- Test on **physical device** (emulators are slow for OpenCV)
- Reduce camera resolution in `CameraProcessor.kt`
- Check Logcat for errors: `adb logcat | grep -i opencv`

### Issue: Web viewer won't load

```bash
# Check if TypeScript compiled
ls web/dist/

# Rebuild if empty
cd web && npm run build

# Try different port
python3 -m http.server 8000
```

## 📱 Expected Behavior

### On Launch:
```
✓ App starts
✓ Camera permission prompt appears
✓ Grant permission
✓ Camera feed starts (black screen initially is OK)
✓ Processing begins
✓ OpenGL surface shows processed output
```

### In Logcat:
```
I/ImageProcessor: Canny processing time: 35 ms
I/CameraProcessor: Processing time: 42ms, FPS: 14.2
I/GLRenderer: GLRenderer initialized successfully
```

### Web Viewer:
- Sample edge detection visualization
- Stats showing 640x480, 15 FPS, Canny Edge, 33ms

## 🎯 Next Steps

Once everything works:

1. **Test all modes**: Raw, Canny Edge, Grayscale
2. **Monitor performance**: Check FPS and frame times
3. **Review code**: Explore JNI bridge, OpenCV processing, OpenGL rendering
4. **Read full README.md**: Architecture details and technical explanations
5. **Commit changes**: Make meaningful commits showing development process

## 📚 Resources

- [Android NDK Guide](https://developer.android.com/ndk/guides)
- [OpenCV Android Tutorial](https://docs.opencv.org/4.x/d5/df8/tutorial_dev_with_OCV_on_Android.html)
- [OpenGL ES 2.0 Reference](https://www.khronos.org/opengles/sdk/docs/man/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/handbook/intro.html)

## 💡 Pro Tips

- Use **physical device** for best performance
- Check **Logcat** for debugging
- Reduce resolution for higher FPS
- Enable **GPU profiling** in Developer Options
- Use Android Studio **Profiler** for performance analysis

---

**Estimated Setup Time**: 10-15 minutes  
**Build Time**: 3-5 minutes (first build)  
**Run Time**: Immediate after build

Good luck! 🚀
