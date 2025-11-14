#!/bin/bash

# Web Viewer Quick Start Script
# Sets up port forwarding and opens the web viewer

echo "🚀 Starting Web Viewer..."
echo ""

# Set up port forwarding
echo "📡 Setting up port forwarding..."
~/Library/Android/sdk/platform-tools/adb forward tcp:8080 tcp:8080
echo "✅ Port forwarding: localhost:8080 → emulator:8080"
echo ""

# Check if server is responding
echo "🔍 Checking HTTP server..."
if curl -s http://localhost:8080/stats > /dev/null 2>&1; then
    echo "✅ HTTP server is running!"
    
    # Get current stats
    STATS=$(curl -s http://localhost:8080/stats)
    echo "📊 Current stats:"
    echo "$STATS" | python3 -m json.tool
    echo ""
else
    echo "❌ HTTP server not responding. Make sure the Android app is running!"
    exit 1
fi

# Option 1: Open built-in test page
echo "🌐 Opening built-in test page..."
open http://localhost:8080/
echo ""

# Option 2: Open web viewer
echo "🎨 To open the full web viewer:"
echo "   1. Run: cd web && python3 -m http.server 3000"
echo "   2. Open: http://localhost:3000"
echo ""

echo "✨ Quick tests:"
echo "   • View frame: curl http://localhost:8080/frame -o frame.jpg && open frame.jpg"
echo "   • View stats: curl http://localhost:8080/stats"
echo ""

echo "🎯 Toggle processing modes on your Android app to see changes!"
