/**
 *  You can modify and use this source freely
 *  only for the development of application related Live2D.
 *
 *  (c) Live2D Inc. All rights reserved.
 */
package com.blStudio.belong;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.live2d.framework.L2DViewMatrix;
import jp.live2d.utils.android.FileManager;
import jp.live2d.utils.android.OffscreenImage;
import jp.live2d.utils.android.SimpleImage;



public class LAppRenderer implements GLSurfaceView.Renderer {

    private LAppLive2DManager delegate;

    private SimpleImage bg;

    private float accelX=0;
    private float accelY=0;

    private int w;
    private int h;

    private static boolean doCapture = false;
    private static String mPath;
    private static Activity mApp;

    public LAppRenderer( LAppLive2DManager live2DMgr  ){
        this.delegate = live2DMgr ;
    }


    
    @Override
    public void onSurfaceCreated(GL10 context, EGLConfig arg1) {
        
        setupBackground(context);
    }


    
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        delegate.onSurfaceChanged(gl, width, height);//Live2D Event

        w = width;
        h = height;
        
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        L2DViewMatrix viewMatrix = delegate.getViewMatrix();
        
        gl.glOrthof(
                viewMatrix.getScreenLeft(),
                viewMatrix.getScreenRight(),
                viewMatrix.getScreenBottom(),
                viewMatrix.getScreenTop(),
                0.5f, -0.5f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);


        OffscreenImage.createFrameBuffer(gl, width, height, 0);
        return ;
    }


    
    @Override
    public void onDrawFrame(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        
        delegate.update(gl);

        
        
        gl.glMatrixMode(GL10.GL_MODELVIEW) ;
        gl.glLoadIdentity() ;

        
        gl.glDisable(GL10.GL_DEPTH_TEST) ;
        gl.glDisable(GL10.GL_CULL_FACE) ;
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glEnable(GL10.GL_TEXTURE_2D) ;
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY) ;
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY) ;

        
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        gl.glColor4f(1, 1, 1, 1) ;

        
        gl.glPushMatrix() ;
        {
            
            L2DViewMatrix viewMatrix = delegate.getViewMatrix();
            gl.glMultMatrixf(viewMatrix.getArray(), 0) ;

            
            if(bg!=null){
                gl.glPushMatrix() ;
                {
                    float SCALE_X = 0.25f ;
                    float SCALE_Y = 0.1f ;
                    gl.glTranslatef( -SCALE_X  * accelX , SCALE_Y * accelY , 0 ) ;

                    bg.draw(gl);
                }
                gl.glPopMatrix() ;
            }
            
            for(int i=0;i<delegate.getModelNum();i++)
            {
                LAppModel model = delegate.getModel(i);
                if(model.isInitialized() && ! model.isUpdating())
                {
                    model.update();
                    model.draw(gl);
                }
            }

        }
        gl.glPopMatrix() ;

        if (doCapture){
            doCapture = false;
            Bitmap bitmap = createBitmapFromGLSurface(0,0,w,h,gl);
            openScreenshot(saveBitmap2Storage(bitmap, mPath));
            mPath = "";
        }

    }

    public static void captureFrame(String path, Activity instance){
        doCapture = true;
        mPath = path;
        mApp = instance;
    }

    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl)
            throws OutOfMemoryError {
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
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

    private File saveBitmap2Storage(Bitmap bitmap, String path) {

        FileOutputStream outputStream = null;
        File imageFile = new File(path);
        try {
            outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return imageFile;
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        mApp.startActivity(intent);
    }

    public void setAccel(float x,float y,float z)
    {
        accelX=x;
        accelY=y;
    }


    
    private void setupBackground(GL10 context) {
        try {
            InputStream in = FileManager.open(LAppDefine.BACK_IMAGE_NAME);
            bg=new SimpleImage(context,in);
            
            bg.setDrawRect(
                    LAppDefine.VIEW_LOGICAL_MAX_LEFT,
                    LAppDefine.VIEW_LOGICAL_MAX_RIGHT,
                    LAppDefine.VIEW_LOGICAL_MAX_BOTTOM,
                    LAppDefine.VIEW_LOGICAL_MAX_TOP);

            
            bg.setUVRect(0.0f,1.0f,0.0f,1.0f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
