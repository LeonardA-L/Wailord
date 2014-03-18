package filRouge.wailord;

import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

//Notre classe implémente SurfaceHolder.Callback
public class CameraActivity extends Activity implements SurfaceHolder.Callback {
private Camera mCamera = null;

@Override
public void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 setContentView(R.layout.activity_main);
     
 SurfaceView surface = (SurfaceView)findViewById(R.id.surfaceView);
     
 SurfaceHolder holder = surface.getHolder();
 holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
     
 // On déclare que la classe actuelle gérera les callbacks
 holder.addCallback(this);
}

// Se déclenche quand la surface est créée
public void surfaceCreated(SurfaceHolder holder) {
 try {
   mCamera.setPreviewDisplay(holder);
   mCamera.startPreview();
 } catch (IOException e) {
   e.printStackTrace();
 }
}

// Se déclenche quand la surface est détruite
public void surfaceDestroyed(SurfaceHolder holder) {
 mCamera.stopPreview();
}

// Se déclenche quand la surface change de dimensions ou de format
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
	