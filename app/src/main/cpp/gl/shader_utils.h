#ifndef SHADER_UTILS_H
#define SHADER_UTILS_H

#include <GLES2/gl2.h>
#include <android/log.h>
#include <string>

#define GL_LOG_TAG "ShaderUtils"
#define GL_LOGI(...) __android_log_print(ANDROID_LOG_INFO, GL_LOG_TAG, __VA_ARGS__)
#define GL_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, GL_LOG_TAG, __VA_ARGS__)

class ShaderUtils {
public:
    static GLuint loadShader(GLenum shaderType, const char* shaderSource);
    static GLuint createProgram(const char* vertexSource, const char* fragmentSource);
    static void checkGLError(const char* op);
    
    // Vertex shader for texture rendering
    static const char* getDefaultVertexShader();
    
    // Fragment shader for texture rendering
    static const char* getDefaultFragmentShader();
};

#endif // SHADER_UTILS_H
