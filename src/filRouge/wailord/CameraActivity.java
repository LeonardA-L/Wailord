package filRouge.wailord;

import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

//Notre classe impl�mente SurfaceHolder.Callback
public class CameraActivity extends Activity implements SurfaceHolder.Callback {
private Camera mCamera = null;

@Override
public void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 setContentView(R.layout.activity_main);
     
 SurfaceView surface = (SurfaceView)findViewById(R.id.surfaceView);
     
 SurfaceHolder holder = surface.getHolder();
 holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
     
 // On d�clare que la classe actuelle g�rera les callbacks
 holder.addCallback(this);
}

// Se d�clenche quand la surface est cr��e
public void surfaceCreated(SurfaceHolder holder) {
 try {
   mCamera.setPreviewDisplay(holder);
   mCamera.startPreview();
 } catch (IOException e) {
   e.printStackTrace();
 }
}

// Se d�clenche quand la surface est d�truite
public void surfaceDestroyed(SurfaceHolder holder) {
 mCamera.stopPreview();
}

// Se d�clenche quand la surface change de dimensions ou de format
public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
}

@Override
protected void onResume() {
 super.onResume();
 mCamera = Camera.open();
}

@Override
protected void onPause() {
 super.onPause();
 mCamera.release();
}
}
	