package com.example.edgedetection.gl;

import android.opengl.GLES20;

public class TextureShader {
    private int program;
    private int positionHandle;
    private int textureHandle;
    private int textureUniformHandle;

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 vTexCoord;" +
                    "varying vec2 fTexCoord;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "  fTexCoord = vTexCoord;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D uTexture;" +
                    "varying vec2 fTexCoord;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(uTexture, fTexCoord);" +
                    "}";

    public TextureShader() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        textureHandle = GLES20.glGetAttribLocation(program, "vTexCoord");
        textureUniformHandle = GLES20.glGetUniformLocation(program, "uTexture");
    }

    public void use() {
        GLES20.glUseProgram(program);
        GLES20.glUniform1i(textureUniformHandle, 0);
    }

    public int getPositionHandle() { return positionHandle; }
    public int getTextureHandle() { return textureHandle; }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}