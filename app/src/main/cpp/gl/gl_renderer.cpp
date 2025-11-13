#include "gl_renderer.h"

// Vertex coordinates and texture coordinates
static const GLfloat vertices[] = {
    -1.0f, -1.0f,  // bottom left
     1.0f, -1.0f,  // bottom right
    -1.0f,  1.0f,  // top left
     1.0f,  1.0f   // top right
};

static const GLfloat texCoords[] = {
    0.0f, 1.0f,  // bottom left
    1.0f, 1.0f,  // bottom right
    0.0f, 0.0f,  // top left
    1.0f, 0.0f   // top right
};

GLRenderer::GLRenderer() 
    : program(0), texture(0), viewportWidth(0), viewportHeight(0) {
}

GLRenderer::~GLRenderer() {
    if (texture != 0) {
        glDeleteTextures(1, &texture);
    }
    if (program != 0) {
        glDeleteProgram(program);
    }
}

bool GLRenderer::initialize() {
    // Create shader program
    program = ShaderUtils::createProgram(
        ShaderUtils::getDefaultVertexShader(),
        ShaderUtils::getDefaultFragmentShader()
    );
    
    if (program == 0) {
        GL_LOGE("Failed to create shader program");
        return false;
    }
    
    // Get attribute locations
    aPositionLocation = glGetAttribLocation(program, "aPosition");
    aTexCoordLocation = glGetAttribLocation(program, "aTexCoord");
    uTextureLocation = glGetUniformLocation(program, "uTexture");
    
    // Generate texture
    glGenTextures(1, &texture);
    glBindTexture(GL_TEXTURE_2D, texture);
    
    // Set texture parameters
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    
    ShaderUtils::checkGLError("initialize");
    
    GL_LOGI("GLRenderer initialized successfully");
    return true;
}

void GLRenderer::updateTexture(const void* imageData, int width, int height) {
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 
                 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData);
    ShaderUtils::checkGLError("updateTexture");
}

void GLRenderer::render() {
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    
    glUseProgram(program);
    
    // Set vertex positions
    glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, GL_FALSE, 0, vertices);
    glEnableVertexAttribArray(aPositionLocation);
    
    // Set texture coordinates
    glVertexAttribPointer(aTexCoordLocation, 2, GL_FLOAT, GL_FALSE, 0, texCoords);
    glEnableVertexAttribArray(aTexCoordLocation);
    
    // Bind texture
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture);
    glUniform1i(uTextureLocation, 0);
    
    // Draw
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    
    glDisableVertexAttribArray(aPositionLocation);
    glDisableVertexAttribArray(aTexCoordLocation);
    
    ShaderUtils::checkGLError("render");
}

void GLRenderer::setViewport(int width, int height) {
    viewportWidth = width;
    viewportHeight = height;
    glViewport(0, 0, width, height);
    ShaderUtils::checkGLError("setViewport");
}

void GLRenderer::setupVertexData() {
    // Vertex data is static, defined at the top of the file
}
