package com.wailord.cubegl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import com.wailord.cubegl.Square;

public class SquareRenderer implements GLSurfaceView.Renderer{

	private boolean mTranslucentBackground;
    private Square mSquare;
    private float mTransY = 1;
    private float mTransX = 1;
    private float mAngle;
	
	public SquareRenderer(boolean useTranslucentBackground)
	{
		mTranslucentBackground = useTranslucentBackground;
		mSquare = new Square();
	}
	
	@Override
	public void onDrawFrame(GL10 gl) 
	{
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glClearColor(0.0f,0.5f,0.5f,1.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	
		
		//gl.glTranslatef(0.0f,(float)Math.sin(mTransY), -7.0f);
		
		gl.glTranslatef(0f, 0f, -7.0f);
		
		//gl.glScalef(1f, 1f, 1f);

		
		gl.glRotatef(mAngle, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(mAngle, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(mAngle, 0.0f, 0.0f, 1.0f);
		
		//gl.glScalef(0.5f, 0.5f, 0.5f);
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		
		
		mSquare.draw(gl);
		
		mTransY += .075f;
		mTransX -= .075f;
		mAngle+=.2f;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) 
	{
		gl.glViewport(0, 0, width, height);
		
		float aspectRatio;
		float zNear = 0.1f;
		float zFar = 1000f;
		float fieldOfView = 30.0f/57.3f;
		float size;
		
		gl.glEnable(GL10.GL_NORMALIZE);
		aspectRatio = (float)width/(float)height;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		size = zNear * (float)(Math.tan((double)(fieldOfView/2.0f)));
		gl.glFrustumf(-size, size, -size/aspectRatio, size/aspectRatio, zNear, zFar);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) 
	{
		gl.glDisable(GL10.GL_DITHER);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
		
		if(mTranslucentBackground)
		{
			gl.glClearColor(0, 0, 0, 0);
		}
		else
		{
			gl.glClearColor(1, 1, 1, 1);
		}
		
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);
	}

}
