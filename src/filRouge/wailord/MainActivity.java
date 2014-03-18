package filRouge.wailord;

import java.util.List;

import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity {

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        SurfaceView surface = (SurfaceView)findViewById(R.id.surfaceView);
        SurfaceHolder holder = surface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        Camera camera = Camera.open();
        Camera.Parameters params = camera.getParameters();

        // Pour connaître les modes de flash supportés
        List<String> flashs = params.getSupportedFlashModes();

        takePicture(camera);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private void takePicture(Camera camera) {
    	  // Jouera un son au moment où on prend une photo

    	  // Sera lancée une fois l'image traitée, on enregistre l'image sur le support externe
    	  Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
    	    public void onPictureTaken(byte[] data, Camera camera) {
    	      
    	    }
    	  };
    	    
    	  camera.takePicture(null, null, jpegCallback);
    	}
    
}
