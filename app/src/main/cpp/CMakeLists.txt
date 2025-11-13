cmake_minimum_required(VERSION 3.22.1)
project("edgedetector")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17 -frtti -fexceptions")

# OpenCV setup - Using prebuilt OpenCV SDK
set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv-sdk/sdk/native/jni)
find_package(OpenCV REQUIRED)

include_directories(
    ${CMAKE_SOURCE_DIR}/jni
    ${CMAKE_SOURCE_DIR}/gl
    ${OpenCV_INCLUDE_DIRS}
)

# JNI Bridge
add_library(
    native-lib
    SHARED
    jni/native-lib.cpp
    jni/image_processor.cpp
)

# OpenGL ES renderer
add_library(
    gl-renderer
    SHARED
    gl/gl_renderer.cpp
    gl/shader_utils.cpp
)

find_library(log-lib log)
find_library(android-lib android)
find_library(gles-lib GLESv2)
find_library(egl-lib EGL)

target_link_libraries(
    native-lib
    ${OpenCV_LIBS}
    ${log-lib}
    ${android-lib}
)

target_link_libraries(
    gl-renderer
    ${gles-lib}
    ${egl-lib}
    ${log-lib}
    ${android-lib}
)
