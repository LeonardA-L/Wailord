package com.android.airhockey;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.Menu;

public class AirHockeyActivity extends Activity {

	private GLSurfaceView glSurfaceView;
	private boolean rendererSet = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		glSurfaceView = new GLSurfaceView(this);
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final boolean supportEs2 = true; // bug here with "ConfigurationInfo.reqGlEsVersion >= 0x20000;
		
		if(supportEs2)
		{
			glSurfaceView.setEGLContextClientVersion(2);
			glSurfaceView.setRenderer(new AirHockeyRenderer(this));
			rendererSet = true;
		}	
		setContentView(glSurfaceView);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(rendererSet)
		{
			glSurfaceView.onPause();
		}
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(rendererSet)
		{
			glSurfaceView.onResume();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
