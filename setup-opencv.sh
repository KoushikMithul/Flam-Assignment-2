#!/bin/bash
# OpenCV Android SDK Setup Script

set -e

OPENCV_VERSION="4.8.0"
OPENCV_URL="https://github.com/opencv/opencv/releases/download/${OPENCV_VERSION}/opencv-${OPENCV_VERSION}-android-sdk.zip"
OPENCV_ZIP="opencv-${OPENCV_VERSION}-android-sdk.zip"
OPENCV_DIR="opencv-${OPENCV_VERSION}-android-sdk"
TARGET_DIR="app/src/main/cpp/opencv-sdk"

echo "🔧 Setting up OpenCV Android SDK v${OPENCV_VERSION}"
echo ""

# Check if already exists
if [ -d "$TARGET_DIR" ]; then
    echo "✅ OpenCV SDK already exists at $TARGET_DIR"
    read -p "Do you want to reinstall? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 0
    fi
    rm -rf "$TARGET_DIR"
fi

# Download OpenCV
echo "📥 Downloading OpenCV Android SDK..."
if [ ! -f "$OPENCV_ZIP" ]; then
    curl -L -o "$OPENCV_ZIP" "$OPENCV_URL"
    echo "✅ Download complete"
else
    echo "⚠️  Using existing download: $OPENCV_ZIP"
fi

# Extract
echo "📦 Extracting..."
unzip -q "$OPENCV_ZIP"

# Move to target directory
echo "📁 Moving to project..."
mkdir -p "app/src/main/cpp"
mv "${OPENCV_DIR}/sdk" "$TARGET_DIR"

# Cleanup
echo "🧹 Cleaning up..."
rm -rf "$OPENCV_DIR"
# Optionally remove zip file
# rm "$OPENCV_ZIP"

echo ""
echo "✨ OpenCV Android SDK setup complete!"
echo "📍 Location: $TARGET_DIR"
echo ""
echo "Next steps:"
echo "1. Open project in Android Studio"
echo "2. Sync Gradle"
echo "3. Build and run the app"
