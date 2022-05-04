package com.example.deepaint;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.util.ArrayList;

class CanvasRenderer implements GLSurfaceView.Renderer {

    private ArrayList<Triangle> shapesToDraw;

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color -
        // the background should be completely transparent so v3 = 0
        // Color does not matter
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        shapesToDraw = new ArrayList<Triangle>();
    }

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        drawALl();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        for (Triangle t : shapesToDraw) {
            t.draw();
        }
    }

    public static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void addTriangle(Triangle t) {
        shapesToDraw.add(t);
    }

    public void drawALl() {
        for (Triangle t : shapesToDraw) {
            t.draw();
        }

    }
}