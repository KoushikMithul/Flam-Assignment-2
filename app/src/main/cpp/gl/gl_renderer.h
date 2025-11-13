#ifndef GL_RENDERER_H
#define GL_RENDERER_H

#include <GLES2/gl2.h>
#include <android/log.h>
#include "shader_utils.h"

class GLRenderer {
private:
    GLuint program;
    GLuint texture;
    GLint aPositionLocation;
    GLint aTexCoordLocation;
    GLint uTextureLocation;
    
    int viewportWidth;
    int viewportHeight;
    
    void setupVertexData();
    
public:
    GLRenderer();
    ~GLRenderer();
    
    bool initialize();
    void updateTexture(const void* imageData, int width, int height);
    void render();
    void setViewport(int width, int height);
};

#endif // GL_RENDERER_H
