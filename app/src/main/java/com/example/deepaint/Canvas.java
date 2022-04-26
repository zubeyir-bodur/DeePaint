package com.example.deepaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Canvas extends GLSurfaceView {
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private final CanvasRenderer renderer;
    private float previousX;
    private float previousY;
    private int x; // Coordinates of the top left corner of the canvas
    private int y;
    private int width; //
    private int height;

    public Canvas(Context context, int[] coordinates, int width, int height) {
        super(context);
        x = coordinates[0];
        y = coordinates[1];

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        renderer = new CanvasRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    /**
     * Here, drawings will be done
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                // TODO give the radius parametric from UI
                float size = 1;
                // TODO Draw a triangle w/ center previous x,y
                //  with sides = size
                float sqrtThree = 1;
                float[] triangleCoordinates = {x, y,
                        x - size/2, y - (size*sqrtThree)/2,
                        x + size/2, y - (size*sqrtThree)/2};
                // Drawn object should be transparent
                float[] colors = {0.0f, 0.0f, 0.0f, 0.5f};
                Triangle point = new Triangle(triangleCoordinates, colors);
                point.draw();
        }

        previousX = x;
        previousY = y;
        return true;
    }

    public float[][] toRGB(GL10 gl) {
        float[][] img;
        int width, height;
        width = MainActivity.width;
        height = MainActivity.height;
        int x, y;
        // TODO compute the top corner coordinates of the image
        Bitmap bitmap = createBitmapFromGLSurface(0, 0, width, height, gl);
        return img;
    }

    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl)
            throws OutOfMemoryError {
        int[] bitmapBuffer = new int[w * h];
        int[] bitmapSource = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

}
