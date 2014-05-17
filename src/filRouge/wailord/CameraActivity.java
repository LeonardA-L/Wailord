package filRouge.wailord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.opencv.*;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class CameraActivity extends Activity {
	private static final String TAG = "CActivity";
	
	private GestureDetector mGestureDetector;
	
	public static final int CAMERA_WIDTH = 640;
    private Camera mCamera;
    private CameraPreview mPreview;
    
    private ImageView img = null;
    private Bitmap toDisplay = Bitmap.createBitmap(320,240, Bitmap.Config.RGB_565);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

     // Supposed to handle autofocus
        // The class is in the UserDefinedTarget sample
        //mGestureDetector = new GestureDetector(this, new GestureListener());
        
        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
        {
          Log.e(TAG, "Cannot connect to OpenCV Manager");
        }
        
        img = (ImageView)(findViewById(R.id.imageView1));
        // Create an instance of Camera
        mCamera = getCameraInstance();
        if(mCamera == null){
            Log.d(TAG, "Pas de Camera");
        }
        else{
	        // Create our Preview view and set it as the content of our activity.
	        mPreview = new CameraPreview(this, mCamera);
	        Log.d(TAG, "Preview : "+mPreview);
	        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	        Log.d(TAG, "Preview Layout : "+preview);
	        preview.addView(mPreview);
	        
	        int width = mCamera.getParameters().getPictureSize().width;
	         int height = mCamera.getParameters().getPictureSize().height;
	         Log.d(TAG,"Original Camera Size : " +height+" : "+ mCamera.getParameters().getPictureFormat());
	         double fac = (CAMERA_WIDTH/(double)width);
	         //double fac = 1;
	         width = CAMERA_WIDTH;
	         height*=fac;
	         Log.d(TAG,"New Camera Size : " +height);
	        
	         Parameters params = mCamera.getParameters();
	         params.setPictureSize(width, height);
	         //params.set("jpeg-quality", 35);
	         mCamera.setParameters(params);
	        
	         mCamera.startPreview();
	        
	         mCamera.autoFocus(null);
	         
	     // Add a listener to the Capture button
	        Button captureButton = (Button) findViewById(R.id.button_capture);
	        captureButton.setOnClickListener(
	            new View.OnClickListener() {
	                @Override
	                public void onClick(View v) {
	                    // get an image from the camera
	                	Log.d(TAG, "C'est la teuf du bouton");
	                    mCamera.takePicture(null, null, mPicture);
	                }
	            }
	        );
        }
        //mCamera.startPreview();
    }
    
    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    
    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    
    
    
    
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	Log.d(TAG,"Picture taken");
        	Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
        	//Bitmap picture= BitmapFactory.decodeResource(getResources(), R.drawable.ihem4);
        	
        	Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        	Bitmap blankBMP = Bitmap.createBitmap(picture.getWidth(),picture.getHeight(),Bitmap.Config.ARGB_8888);
        	// Find Contours via OpenCV
        	Mat image = new Mat(picture.getWidth(),picture.getHeight(), CvType.CV_8UC4,new Scalar(4));
        	Mat ITimage = new Mat(picture.getWidth(),picture.getHeight(), CvType.CV_8UC4,new Scalar(4));
        	Mat newImg = new Mat(picture.getWidth(),picture.getHeight(), CvType.CV_8UC4,new Scalar(4));
        	
        	Utils.bitmapToMat(picture, image);
        	Utils.bitmapToMat(blankBMP, newImg);

        	
        	Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        	Imgproc.cvtColor(newImg, newImg, Imgproc.COLOR_RGB2GRAY);
        	
        	newImg.setTo(new Scalar(0,0,0));

        	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        	
        	Imgproc.Canny(image, ITimage, 80, 100);
        	Mat hierarchy = new Mat();
        	Imgproc.findContours(ITimage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        	
        	hierarchy.release();
        	
        	Imgproc.cvtColor(image, image, Imgproc.COLOR_GRAY2BGR);
        		
        	// Get a list of contour-related data
        	int[][] v = contoursToLists(contours);
        	// Sort the contour list according to the data
        	contours = sort(contours, v);
        	
        	// Spreading threshold
        	int currentDiag = -1;
        	int tresh = 20;
        	
        	// Find who contains who (define level)
        	int[] levelTab = levels(v, tresh);
        	
        	// Get max level
        	int max = -1;
        	for(int i=0;i<levelTab.length;i++){
        		if(max == -1 || levelTab[i] > max){
        			max = levelTab[i];
        		}
        	}
        	
        	// Draw the contours on the image
        	for(int i=0;i<contours.size();i++){
        		
        		int fac = (int)((levelTab[i])*(255.0/(max))+1);
        		if(currentDiag == -1 || Math.abs(v[i][1] - currentDiag) >= tresh){
        			currentDiag = v[i][1];
        			Imgproc.drawContours(newImg, contours, i, new Scalar(fac,fac,fac), 3); //#4 square (blue)
        		}
        		
        	}
        	
        	// Get back to picture bitmap
        	Utils.matToBitmap(newImg, picture);
        	setImg(picture);
        
        }
    };
    
    public int[] levels(int[][] values, int tresh){
    	int[] levels = new int[values.length];
    	for(int i =0;i<values.length;i++){
    		int level = 1;
    		for(int j=0;j<i;j++){
    			if(values[j][2] < values[i][2] && values[j][3] < values[i][3] && values[j][4] > values[i][4] && values[j][5] > values[i][5] && Math.abs(values[i][1] - values[j][1]) >= tresh){
    				level ++;
    			}
    		}
    		levels[i] = level;
    	}
    	return levels;
    }
    
    public int[][] contoursToLists(List<MatOfPoint> contours){
    	int[][] liste = new int[contours.size()][6];
    	for(int i=0;i<contours.size();i++){
    		List<org.opencv.core.Point> cont = contours.get(i).toList();
    		Point min = new Point(-1,-1);
    		Point max = new Point(-1,-1);
    		for(int j=0;j<cont.size();j++){
    			org.opencv.core.Point p = cont.get(j);
    			if(min.x == -1 || p.x < min.x){
    				min.x = (int)p.x;
    			}
    			if(min.y == -1 || p.y < min.y){
    				min.y = (int)p.y;
    			}
    			
    			if(max.x == -1 || p.x > max.x){
    				max.x = (int)p.x;
    			}
    			if(max.y == -1 || p.y > max.y){
    				max.y = (int)p.y;
    			}
    		}
    		
    		int diag = (int)Math.sqrt(Math.pow(max.x - min.x,2)+ Math.pow(max.y - min.y,2));
    		//Log.d(TAG," "+i+" : "+diag);
    		liste[i][0] = i;
    		liste[i][1] = diag;
    		liste[i][2] = min.x;
    		liste[i][3] = min.y;
    		liste[i][4] = max.x;
    		liste[i][5] = max.y;
    	}
    	
    	return liste;
    }
    
    public List<MatOfPoint> sort(List<MatOfPoint> contours, int[][] values){
    	List<MatOfPoint> sorted = new ArrayList<MatOfPoint>();
    	boolean swapped = true;
    	while(swapped){
    		swapped = false;
    		for(int i=0;i<values.length -1;i++){
    			int[] tmp = new int[6];
    			
    			if(values[i+1][1] > values[i][1]){
    				tmp[0] = values[i+1][0];
    				tmp[1] = values[i+1][1];
    				tmp[2] = values[i+1][2];
    				tmp[3] = values[i+1][3];
    				tmp[4] = values[i+1][4];
    				tmp[5] = values[i+1][5];
    				
    				values[i+1][0] = values[i][0];
    				values[i+1][1] = values[i][1];
    				values[i+1][2] = values[i][2];
    				values[i+1][3] = values[i][3];
    				values[i+1][4] = values[i][4];
    				values[i+1][5] = values[i][5];
    				
    				values[i][0] = tmp[0];
    				values[i][1] = tmp[1];
    				values[i][2] = tmp[2];
    				values[i][3] = tmp[3];
    				values[i][4] = tmp[4];
    				values[i][5] = tmp[5];
    				swapped = true;
    			}
    		}
    	}
    	
    	int tres = 10;
    	
    	for(int i=0;i< values.length;i++){
    		if(values[i][1] > tres){
    			sorted.add(contours.get(values[i][0]));
    		}
    	}
    	
    	return sorted;
    }
    

    
    public void setImg (Bitmap newImage){
    	toDisplay = newImage;
    	//toDisplay = toBinary(toGrayscale(toDisplay));
    	img.setImageBitmap(this.toDisplay);
    }
    
    public Bitmap toBinary(Bitmap bmpOriginal) {
        int width, height, threshold;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        threshold = 127;
        Bitmap bmpBinary = Bitmap.createBitmap(bmpOriginal);

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get one pixel color
                int pixel = bmpOriginal.getPixel(x, y);
                int red = Color.red(pixel);

                //get binary value
                if(red < threshold){
                    bmpBinary.setPixel(x, y, 0xFF000000);
                } else{
                    bmpBinary.setPixel(x, y, 0xFFFFFFFF);
                }

            }
        }
        return bmpBinary;
    }
    
    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {        
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();    

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
    
    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
    	@Override
    	public void onManagerConnected(int status) {
    	   switch (status) {
    	       case LoaderCallbackInterface.SUCCESS:
    	       {
    	      Log.i(TAG, "OpenCV loaded successfully");
    	      // Create and set View
    	      //setContentView(R.layout.activity_camera);
    	       } break;
    	       default:
    	       {
    	      super.onManagerConnected(status);
    	       } break;
    	   }
    	    }
    	};
    	
    	
}
