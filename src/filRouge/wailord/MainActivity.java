package filRouge.wailord;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ImageTargetBuilder;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.PIXEL_FORMAT;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2I;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.Vuforia.UpdateCallbackInterface;

import android.view.View.OnClickListener;

public class MainActivity extends Activity implements UpdateCallbackInterface, AppControl {

	private static final String LOGTAG = "leMain";
	
	// Settings (changed in the setting activity)
	public static int LightTreshold = 127;
	public static int SmoothingIterations = 4;
	
    private GestureDetector mGestureDetector;
    private Activity m_activity;
    // Vuforia initialization flags:
    private int mVuforiaFlags = 0;
    // Flags
    private boolean m_started = false;
    
    DataSet dataSetUserDef = null;
    
    private boolean mFlash = false;
    private boolean mContAutofocus = false;
    private boolean mExtendedTracking = false;
    private boolean pictureTaken = false;
    int targetBuilderCounter = 1;
    
    private View mFlashOptionView;
    

	// Stores orientation
    private boolean mIsPortrait = false;
    // Display size of the device:
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    
 // The async tasks to initialize the Vuforia SDK:
    private InitVuforiaTask mInitVuforiaTask;
    private LoadTrackerTask mLoadTrackerTask;

	// An object used for synchronizing Vuforia initialization, dataset loading
    // and the Android onDestroy() life cycle event. If the application is
    // destroyed while a data set is still being loaded, then we wait for the
    // loading operation to finish before shutting down Vuforia:
    public Object mShutdownLock = new Object();
    
    // ----- OpenGL MADAFAKA
    private WailordApplicationGLView mGlView;
    // Stores the projection matrix to use for rendering purposes
    private Matrix44F mProjectionMatrix;
    // The textures we will use for rendering:
    private Vector<Texture> mTextures;
    RefFreeFrame refFreeFrame;
    private WailordTargetRenderer mRenderer;
    
    // View overlays to be displayed in the Augmented View
    private RelativeLayout mUILayout;
    private View mBottomBar;
    private View mCameraButton;
    
 // Holds the camera configuration to use upon resuming
    private int mCamera = CameraDevice.CAMERA.CAMERA_DEFAULT;
    
    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(
            this);
    
   
    boolean mIsDroidDevice = false;
    
    Camera mTheCamera;
    private Bitmap mPictureData;
    private int[][] mProcessedImage;
    public static final int CAMERA_WIDTH = 320;
    public static final int LEO_HIGH = 1;
    public static final int LEO_LOW = 0;

    
    // Time Measurement
    long start;
    long end;
    
    // Layout
    Button btn_launch;
    Button btn_settings;
    
    
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        m_activity = this;
        
        Log.i(LOGTAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
        {
          Log.e(LOGTAG, "Cannot connect to OpenCV Manager");
        }
        
        btn_launch = (Button)findViewById(R.id.btn_launch);
        btn_launch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				launchApp();
			}
		});
        
        btn_settings = (Button)findViewById(R.id.btn_settings);
        btn_settings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent secondeActivite = new Intent(MainActivity.this, SettingsActivity.class);
			        startActivity(secondeActivite);
			}
		});
        
        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        loadTextures();
        
       // Supposed to handle autofocus
       // The class is in the UserDefinedTarget sample
       mGestureDetector = new GestureDetector(this, new GestureListener());
        
        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
                "droid");
    }
	
	public void launchApp(){
		initAR(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	// camera picture retrieving
    private PictureCallback mPicture = new PictureCallback() {
    	//private static final String LOGTAG = "PictureCallBack";
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        //Log.d(LOGTAG,"Picturing...");
        pictureTaken = true;
        mPictureData = BitmapFactory.decodeByteArray(data, 0, data.length);
         //Log.d(LOGTAG,"Ok Picture "+picture.getPixel(10, 10));
         handleCameraVuforia();
        }
    };
	
	// Called when the activity will start interacting with the user.
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();
        
        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        try
        {
            this.resumeAR();
        } catch (VuforiaException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
        
    }
    
 // Resumes Vuforia, restarts the trackers and the camera
    public void resumeAR() throws VuforiaException
    {
        // Vuforia-specific resume operation
        Vuforia.onResume();
        
        if (m_started)
            startAR(mCamera);
    }
    
 // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();
        
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
        
        // Turn off the flash
        if (mFlashOptionView != null && mFlash)
        {
            // OnCheckedChangeListener is called upon changing the checked state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                //((Switch) mFlashOptionView).setChecked(false);
            } else
            {
                ((CheckBox) mFlashOptionView).setChecked(false);
            }
        }
        
        try
        {
            this.pauseAR();
        } catch (VuforiaException e)
        {
            Log.e(LOGTAG, e.getString());
        }
    }
    
    // Pauses Vuforia and stops the camera
    public void pauseAR() throws VuforiaException
    {
        if (m_started)
            stopCamera();
        
        Vuforia.onPause();
    }
    
 // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();
        
        try
        {
            this.stopAR();
        } catch (VuforiaException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        // Unload texture:
        mTextures.clear();
        mTextures = null;
        
        System.gc();
    }
    
    private void stopCamera()
    {
        this.doStopTrackers();
        CameraDevice.getInstance().stop();
        CameraDevice.getInstance().deinit();
    }
    
 // Stops any ongoing initialization, stops Vuforia
    public void stopAR() throws VuforiaException
    {
        // Cancel potentially running tasks
        if (mInitVuforiaTask != null
            && mInitVuforiaTask.getStatus() != InitVuforiaTask.Status.FINISHED)
        {
            mInitVuforiaTask.cancel(true);
            mInitVuforiaTask = null;
        }
        
        if (mLoadTrackerTask != null
            && mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED)
        {
            mLoadTrackerTask.cancel(true);
            mLoadTrackerTask = null;
        }
        
        mInitVuforiaTask = null;
        mLoadTrackerTask = null;
        
        m_started = false;
        
        stopCamera();
        
        // Ensure that all asynchronous operations to initialize Vuforia
        // and loading the tracker datasets do not overlap:
        synchronized (mShutdownLock)
        {
            
            boolean unloadTrackersResult;
            boolean deinitTrackersResult;
            
            // Destroy the tracking data set:
            unloadTrackersResult = this.doUnloadTrackersData();
            
            // Deinitialize the trackers:
            deinitTrackersResult = this.doDeinitTrackers();
            
            // Deinitialize Vuforia SDK:
            Vuforia.deinit();
            
            if (!unloadTrackersResult)
                throw new VuforiaException(
                		VuforiaException.UNLOADING_TRACKERS_FAILURE,
                    "Failed to unload trackers\' data");
            
            if (!deinitTrackersResult)
                throw new VuforiaException(
                		VuforiaException.TRACKERS_DEINITIALIZATION_FAILURE,
                    "Failed to deinitialize trackers");
            
        }
    }
    
 // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        
        this.onConfigurationChanged();
        
        // Removes the current layout and inflates a proper layout
        // for the new screen orientation
        
        if (mUILayout != null)
        {
            mUILayout.removeAllViews();
            ((ViewGroup) mUILayout.getParent()).removeView(mUILayout);
            
        }
        
        addOverlayView(false);
    }
    
 // Manages the configuration changes
    public void onConfigurationChanged()
    {
        updateActivityOrientation();
        
        storeScreenDimensions();
        
        if (isARRunning())
        {
            // configure video background
            configureVideoBackground();
            
            // Update projection matrix:
            setProjectionMatrix();
        }
        
    }
    
 // Returns true if Vuforia is initialized, the trackers started and the
    // tracker data loaded
    private boolean isARRunning()
    {
        return m_started;
    }
    
    public void initAR(int screenOrientation){
    	Log.d(LOGTAG, "initAR");
    	VuforiaException vuforiaException = null;
    	if ((screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
                && (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO))
                screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
    	
    	 // Apply screen orientation
        m_activity.setRequestedOrientation(screenOrientation);
        
        updateActivityOrientation();
        
        // Query display dimensions:
        storeScreenDimensions();
        
        
        // As long as this window is visible to the user, keep the device's
        // screen turned on and bright:
        m_activity.getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        mVuforiaFlags = Vuforia.GL_20;
    	
        
     // Initialize Vuforia SDK asynchronously to avoid blocking the
        // main (UI) thread.
        //
        // NOTE: This task instance must be created and invoked on the
        // UI thread and it can be executed only once!
        if (mInitVuforiaTask != null)
        {
            String logMessage = "Cannot initialize SDK twice";
            vuforiaException = new VuforiaException(
            		VuforiaException.VUFORIA_ALREADY_INITIALIZATED,
                logMessage);
            Log.e(LOGTAG, logMessage);
        }
        
        if (vuforiaException == null)
        {
            try
            {
                mInitVuforiaTask = new InitVuforiaTask(this);
                mInitVuforiaTask.execute();
            } catch (Exception e)
            {
                String logMessage = "Initializing Vuforia SDK failed";
                vuforiaException = new VuforiaException(
                		VuforiaException.INITIALIZATION_FAILURE,
                    logMessage);
                Log.e(LOGTAG, logMessage);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
 // Stores the orientation depending on the current resources configuration
    private void updateActivityOrientation()
    {
        Configuration config = m_activity.getResources().getConfiguration();
        
        switch (config.orientation)
        {
            case Configuration.ORIENTATION_PORTRAIT:
                mIsPortrait = true;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mIsPortrait = false;
                break;
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                break;
        }
        
        Log.i(LOGTAG, "Activity is in "
            + (mIsPortrait ? "PORTRAIT" : "LANDSCAPE"));
    }
    
 // Stores screen dimensions
    private void storeScreenDimensions()
    {
        // Query display dimensions:
        DisplayMetrics metrics = new DisplayMetrics();
        m_activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }
    
	
    public Object getmShutdownLock() {
		return mShutdownLock;
	}

	public void setmShutdownLock(Object mShutdownLock) {
		this.mShutdownLock = mShutdownLock;
	}

	public int getmVuforiaFlags() {
		return mVuforiaFlags;
	}

	public void setmVuforiaFlags(int mVuforiaFlags) {
		this.mVuforiaFlags = mVuforiaFlags;
	}
	
    public LoadTrackerTask getmLoadTrackerTask() {
		return mLoadTrackerTask;
	}

	public void setmLoadTrackerTask(LoadTrackerTask mLoadTrackerTask) {
		this.mLoadTrackerTask = mLoadTrackerTask;
	}

	// Callback called every cycle
    @Override
    public void QCAR_onUpdate(State s)
    {
        this.onQCARUpdate(s);
    }
	
	public boolean isM_started() {
		return m_started;
	}

	public void setM_started(boolean m_started) {
		this.m_started = m_started;
	}

	@Override
    public boolean doInitTrackers()
    {
        // Indicate if the trackers were initialized correctly
        boolean result = true;
        
        // Initialize the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        Tracker tracker = trackerManager.initTracker(ImageTracker
            .getClassType());
        if (tracker == null)
        {
            Log.d(LOGTAG, "Failed to initialize ImageTracker.");
            result = false;
        } else
        {
            Log.d(LOGTAG, "Successfully initialized ImageTracker.");
        }
        
        return result;
    }

	@Override
    public boolean doLoadTrackersData()
    {
        // Get the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) trackerManager
            .getTracker(ImageTracker.getClassType());
        if (imageTracker == null)
        {
            Log.d(
                LOGTAG,
                "Failed to load tracking data set because the ImageTracker has not been initialized.");
            return false;
        }
        
        // Create the data set:
        dataSetUserDef = imageTracker.createDataSet();
        if (dataSetUserDef == null)
        {
            Log.d(LOGTAG, "Failed to create a new tracking data.");
            return false;
        }
        
        if (!imageTracker.activateDataSet(dataSetUserDef))
        {
            Log.d(LOGTAG, "Failed to activate data set.");
            return false;
        }
        
        Log.d(LOGTAG, "Successfully loaded and activated data set.");
        return true;
    }

	@Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;
        
        Tracker imageTracker = TrackerManager.getInstance().getTracker(
            ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.start();
        
        return result;
    }
    
    
    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;
        
        Tracker imageTracker = TrackerManager.getInstance().getTracker(
            ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.stop();
        
        return result;
    }
    
    
    @Override
    public boolean doUnloadTrackersData()
    {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;
        
        // Get the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) trackerManager
            .getTracker(ImageTracker.getClassType());
        if (imageTracker == null)
        {
            result = false;
            Log.d(
                LOGTAG,
                "Failed to destroy the tracking data set because the ImageTracker has not been initialized.");
        }
        
        if (dataSetUserDef != null)
        {
            if (imageTracker.getActiveDataSet() != null
                && !imageTracker.deactivateDataSet(dataSetUserDef))
            {
                Log.d(
                    LOGTAG,
                    "Failed to destroy the tracking data set because the data set could not be deactivated.");
                result = false;
            }
            
            if (!imageTracker.destroyDataSet(dataSetUserDef))
            {
                Log.d(LOGTAG, "Failed to destroy the tracking data set.");
                result = false;
            }
            
            Log.d(LOGTAG, "Successfully destroyed the data set.");
            dataSetUserDef = null;
        }
        
        return result;
    }
    
    
    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;
        
        
        if (refFreeFrame != null)
            refFreeFrame.deInit();
        
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ImageTracker.getClassType());
        
        return result;
    }

	@Override
    public void onInitARDone(VuforiaException exception)
    {
		 Log.d(LOGTAG, "onInitARDone");
		 if (exception == null)
	        {
	            initApplicationAR();
	           
	            // Activate the renderer
	            mRenderer.mIsActive = true;
	            
	            // Now add the GL surface view. It is important
	            // that the OpenGL ES surface view gets added
	            // BEFORE the camera is started and video
	            // background is configured.
	            Log.d(LOGTAG,"GLView : "+mGlView);
	            
	            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
	                LayoutParams.MATCH_PARENT));
	            
	            
	            
	            // Sets the UILayout to be drawn in front of the camera
	            mUILayout.bringToFront();
	            
	            // Hides the Loading Dialog
	            loadingDialogHandler
	                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
	            
	            // Sets the layout background to transparent
	            mUILayout.setBackgroundColor(Color.TRANSPARENT);
	            
	            
	            try
	            {
	                startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
	            } catch (VuforiaException e)
	            {
	                Log.e(LOGTAG, e.getString());
	            }
	            
	            
	            //Log.d(LOGTAG,"CameraDevice : "+CameraDevice);
	            
	            boolean result = CameraDevice.getInstance().setFocusMode(
	                CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
	            
	            if (result){
	                mContAutofocus = true;
	            }
	            else{
	                Log.e(LOGTAG, "Unable to enable continuous autofocus");
	            }
	            
	        } else
	        {
	            Log.e(LOGTAG, exception.getString());
	            finish();
	        }
        
    }

	private void initApplicationAR() {
		 Log.d(LOGTAG, "initApplicationAR");
		
    	
    	// Do application initialization
        
    	//TODO : Je sais.
    	refFreeFrame = new RefFreeFrame(this, this);
        refFreeFrame.init();
        
    	
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();
        
        mGlView = new WailordApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);
        
        
        mRenderer = new WailordTargetRenderer(this, this);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);
        
        addOverlayView(true);
	}

	@Override
    public void onQCARUpdate(State state)
    {
		//Log.d(LOGTAG, "QCAR Update Mofos");
        TrackerManager trackerManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) trackerManager
            .getTracker(ImageTracker.getClassType());
        
        if (refFreeFrame.hasNewTrackableSource())
        {
            Log.d(LOGTAG,
                "Attempting to transfer the trackable source to the dataset");
            
            // Deactivate current dataset
            imageTracker.deactivateDataSet(imageTracker.getActiveDataSet());
            
            // Clear the oldest target if the dataset is full or the dataset
            // already contains five user-defined targets.
            if (dataSetUserDef.hasReachedTrackableLimit()
                || dataSetUserDef.getNumTrackables() >= 5)
                dataSetUserDef.destroy(dataSetUserDef.getTrackable(0));
            
            if (mExtendedTracking && dataSetUserDef.getNumTrackables() > 0)
            {
                // We need to stop the extended tracking for the previous target
                // so we can enable it for the new one
                int previousCreatedTrackableIndex = 
                    dataSetUserDef.getNumTrackables() - 1;
                
                dataSetUserDef.getTrackable(previousCreatedTrackableIndex)
                    .stopExtendedTracking();
            }
            
            // Add new trackable source
            Trackable trackable = dataSetUserDef
                .createTrackable(refFreeFrame.getNewTrackableSource());
            
            /*
            Log.d(LOGTAG,"N : "+dataSetUserDef.getNumTrackables());
            Log.d(LOGTAG,"tr cr "+trackable);
            Log.d(LOGTAG,"tr 0 "+dataSetUserDef.getTrackable(dataSetUserDef.getNumTrackables() - 1));
             */
            
            // Reactivate current dataset
            imageTracker.activateDataSet(dataSetUserDef);
            
            if (mExtendedTracking)
            {
                trackable.startExtendedTracking();
            }
            
        }
    }
	
	// Adds the Overlay view to the GLView
    private void addOverlayView(boolean initLayout)
    {
        // Inflates the Overlay Layout to be displayed above the Camera View
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(
            R.layout.camera_overlay_udt, null, false);
        
        mUILayout.setVisibility(View.VISIBLE);
        
        // If this is the first time that the application runs then the
        // uiLayout background is set to BLACK color, will be set to
        // transparent once the SDK is initialized and camera ready to draw
        if (initLayout)
        {
            mUILayout.setBackgroundColor(Color.BLACK);
        }
        
        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));
        
        // Gets a reference to the bottom navigation bar
        mBottomBar = mUILayout.findViewById(R.id.bottom_bar);
        
        // Gets a reference to the Camera button
        mCameraButton = mUILayout.findViewById(R.id.camera_button);
        
        // Gets a reference to the loading dialog container
        
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
            .findViewById(R.id.loading_layout);
        
        
        startUserDefinedTargets();
        initializeBuildTargetModeViews();
        
        mUILayout.bringToFront();
    }
    
    boolean startUserDefinedTargets()
    {
        Log.d(LOGTAG, "startUserDefinedTargets");
        
        TrackerManager trackerManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) (trackerManager
            .getTracker(ImageTracker.getClassType()));
        if (imageTracker != null)
        {
            ImageTargetBuilder targetBuilder = imageTracker
                .getImageTargetBuilder();
            
            if (targetBuilder != null)
            {
                // if needed, stop the target builder
                if (targetBuilder.getFrameQuality() != ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_NONE)
                    targetBuilder.stopScan();
                
                imageTracker.stop();
                
                targetBuilder.startScan();
                
            }
        } else
            return false;
        
        return true;
    }
    
 // Initialize views
    private void initializeBuildTargetModeViews()
    {
        // Shows the bottom bar
        mBottomBar.setVisibility(View.VISIBLE);
        mCameraButton.setVisibility(View.VISIBLE);
    }
    
 // Creates a texture given the filename
    Texture createTexture(String nName)
    {
        return Texture.loadTextureFromApk(nName, getAssets());
    }
    
 // Callback function called when the target creation finished
    void targetCreated()
    {
        // Hides the loading dialog
        loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
        
        if (refFreeFrame != null)
        {
            refFreeFrame.reset();
        }
        
    }
    

    public void onSurfaceChanged(int width, int height)
    {
        Vuforia.onSurfaceChanged(width, height);
    }
    
    
    public void onSurfaceCreated()
    {
        Vuforia.onSurfaceCreated();
    }
    
    void updateRendering()
    {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        refFreeFrame.initGL(metrics.widthPixels, metrics.heightPixels);
    }
    
 // Gets the projection matrix to be used for rendering
    public Matrix44F getProjectionMatrix()
    {
        return mProjectionMatrix;
    }
 // Method for setting / updating the projection matrix for AR content
    // rendering
    private void setProjectionMatrix()
    {
        CameraCalibration camCal = CameraDevice.getInstance()
            .getCameraCalibration();
        mProjectionMatrix = Tool.getProjectionGL(camCal, 10.0f, 5000.0f);
    }
    
 // We want to load specific textures from the APK, which we will later use
    // for rendering.
    private void loadTextures()
    {
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBlue.png",
            getAssets()));
    }
    
 // Starts Vuforia, initialize and starts the camera and start the trackers
    public void startAR(int camera) throws VuforiaException
    {
        String error;
        mCamera = camera;
        if (!CameraDevice.getInstance().init(camera))
        {
            error = "Unable to open camera device: " + camera;
            Log.e(LOGTAG, error);
            throw new VuforiaException(
            		VuforiaException.CAMERA_INITIALIZATION_FAILURE, error);
        }
        
        configureVideoBackground();
        
        if (!CameraDevice.getInstance().selectVideoMode(
            CameraDevice.MODE.MODE_DEFAULT))
        {
            error = "Unable to set video mode";
            Log.e(LOGTAG, error);
            throw new VuforiaException(
            		VuforiaException.CAMERA_INITIALIZATION_FAILURE, error);
        }
        
        if (!CameraDevice.getInstance().start())
        {
            error = "Unable to start camera device: " + camera;
            Log.e(LOGTAG, error);
            throw new VuforiaException(
            		VuforiaException.CAMERA_INITIALIZATION_FAILURE, error);
        }
        
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);
        
        setProjectionMatrix();
        
        this.doStartTrackers();
        
        try
        {
            setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
        } catch (VuforiaException exceptionTriggerAuto)
        {
            setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
        }
    }
    
 // Configures the video mode and sets offsets for the camera's image
    private void configureVideoBackground()
    {
        CameraDevice cameraDevice = CameraDevice.getInstance();
        VideoMode vm = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);
        
        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setEnabled(true);
        config.setSynchronous(true);
        config.setPosition(new Vec2I(0, 0));
        
        int xSize = 0, ySize = 0;
        if (mIsPortrait)
        {
            xSize = (int) (vm.getHeight() * (mScreenHeight / (float) vm
                .getWidth()));
            ySize = mScreenHeight;
            
            if (xSize < mScreenWidth)
            {
                xSize = mScreenWidth;
                ySize = (int) (mScreenWidth * (vm.getWidth() / (float) vm
                    .getHeight()));
            }
        } else
        {
            xSize = mScreenWidth;
            ySize = (int) (vm.getHeight() * (mScreenWidth / (float) vm
                .getWidth()));
            
            if (ySize < mScreenHeight)
            {
                xSize = (int) (mScreenHeight * (vm.getWidth() / (float) vm
                    .getHeight()));
                ySize = mScreenHeight;
            }
        }
        
        config.setSize(new Vec2I(xSize, ySize));
        
        Log.i(LOGTAG, "Configure Video Background : Video (" + vm.getWidth()
            + " , " + vm.getHeight() + "), Screen (" + mScreenWidth + " , "
            + mScreenHeight + "), mSize (" + xSize + " , " + ySize + ")");
        
        Renderer.getInstance().setVideoBackgroundConfig(config);
        
    }
    
 // Applies auto focus if supported by the current device
    private boolean setFocusMode(int mode) throws VuforiaException
    {
        boolean result = CameraDevice.getInstance().setFocusMode(mode);
        
        if (!result)
            throw new VuforiaException(
            		VuforiaException.SET_FOCUS_MODE_FAILURE,
                "Failed to set focus mode: " + mode);
        
        return result;
    }
    
 // Button Camera clicked
    public void onCameraClick(View v)
    {
    	
    	// -----------------------------
        CameraDevice cameraDevice = CameraDevice.getInstance();
        cameraDevice.stop();
        cameraDevice.deinit();
        
        mTheCamera = null;
        //Log.d(LOGTAG,"Creating Camera");
        try {
        	mTheCamera = Camera.open(); // attempt to get a Camera instance
        	int width = mTheCamera.getParameters().getPictureSize().width;
        	int height = mTheCamera.getParameters().getPictureSize().height;
        	Log.d(LOGTAG,"Original Camera Size : " +height+" : "+ mTheCamera.getParameters().getPictureFormat());
        	double fac = (CAMERA_WIDTH/(double)width);
        	//double fac = 1;
        	width = CAMERA_WIDTH;
        	height*=fac;
        	Log.d(LOGTAG,"New Camera Size : " +height);
        	
        	Parameters params = mTheCamera.getParameters();
        	params.setPictureSize(width, height);
        	//params.set("jpeg-quality", 35);
        	mTheCamera.setParameters(params);
        	
        	mTheCamera.startPreview();
            //Log.d(LOGTAG,"ok Camera");
        	
        	
        	start = System.currentTimeMillis();
        	mTheCamera.takePicture(null, null, mPicture);
        	
            //while(!pictureTaken){
            //Log.d(LOGTAG,"Picture : "+pictureTaken);
            //}
        }
        catch (Exception e){
            e.printStackTrace();
        }
        
    }
    
    public void handleCameraVuforia(){
    	//mTheCamera.stopPreview();
    	//if(mTheCamera!=null){
        mTheCamera.release();
        //}
    	
    	
    	CameraDevice cameraDevice = CameraDevice.getInstance();
    	cameraDevice.init(mCamera);
        cameraDevice.start();
        end = System.currentTimeMillis();
    	Log.d(LOGTAG,"Time between two photos : "+(end-start));
    	// -----------------------------
        if (isUserDefinedTargetsRunning())
        {
            // Shows the loading dialog
            loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
            
            // Builds the new target
            startBuild();
        }
        
        start = System.currentTimeMillis();
        processPicture();
        end = System.currentTimeMillis();
    	Log.d(LOGTAG,"Processing Time : "+(end-start));
    }
    
    boolean isUserDefinedTargetsRunning()
    {
        TrackerManager trackerManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) trackerManager
            .getTracker(ImageTracker.getClassType());
        
        if (imageTracker != null)
        {
            ImageTargetBuilder targetBuilder = imageTracker
                .getImageTargetBuilder();
            if (targetBuilder != null)
            {
                Log.e(LOGTAG, "Quality> " + targetBuilder.getFrameQuality());
                return (targetBuilder.getFrameQuality() != ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_NONE) ? true
                    : false;
            }
        }
        
        return false;
    }
    
    void startBuild()
    {
        TrackerManager trackerManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) trackerManager
            .getTracker(ImageTracker.getClassType());
        
        if (imageTracker != null)
        {
            ImageTargetBuilder targetBuilder = imageTracker
                .getImageTargetBuilder();
            if (targetBuilder != null)
            {
                // Uncomment this block to show and error message if
                // the frame quality is Low
                //if (targetBuilder.getFrameQuality() == ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_LOW)
                //{
                //     showErrorDialogInUIThread();
                //}
                
                String name;
                do
                {
                    name = "UserTarget-" + targetBuilderCounter;
                    Log.d(LOGTAG, "TRYING " + name);
                    targetBuilderCounter++;
                } while (!targetBuilder.build(name, 320.0f));
                
                refFreeFrame.setCreating();
            }
        }
    }
    
    public void processPicture(){
    	// Shows the loading dialog
    	//mPictureData = toBinary(mPictureData);
    	//TODO : Shit 'n' shit
    	//mProcessedImage = toBinary(mPictureData);
    	
    	
    	// Find Contours via OpenCV
    	Bitmap blankBMP = Bitmap.createBitmap(mPictureData.getWidth(),mPictureData.getHeight(),Bitmap.Config.ARGB_8888);
    	Mat image = new Mat(mPictureData.getWidth(),mPictureData.getHeight(), CvType.CV_8UC4,new Scalar(4));
    	Mat ITimage = new Mat(mPictureData.getWidth(),mPictureData.getHeight(), CvType.CV_8UC4,new Scalar(4));
    	Mat newImg = new Mat(mPictureData.getWidth(),mPictureData.getHeight(), CvType.CV_8UC4,new Scalar(4));
    	
    	Utils.bitmapToMat(mPictureData, image);
    	Utils.bitmapToMat(blankBMP, newImg);

    	Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);

    	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    	
    	Imgproc.Canny(image, ITimage, 80, 100);
    	Mat hierarchy = new Mat();
    	Imgproc.findContours(ITimage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
    	
    	hierarchy.release();
    	
    	Imgproc.cvtColor(image, image, Imgproc.COLOR_GRAY2BGR);
    		

    	Log.d(LOGTAG, "Contours : "+contours.size());
    	
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
    	
    	Log.d(LOGTAG, "Contours : "+contours.size());
    	// Draw the contours on the image
    	for(int i=0;i<contours.size();i++){
    		
    		int fac = (int)((levelTab[i])*(255.0/(max))+1);
    		if(currentDiag == -1 || Math.abs(v[i][1] - currentDiag) >= tresh){
    			
    			currentDiag = v[i][1];
    			Imgproc.drawContours(newImg, contours, i, new Scalar(fac,fac,fac), 3); //#4 square (blue)
    		}
    		
    	}
    	
    	// Get back to picture bitmap
    	Utils.matToBitmap(newImg, mPictureData);
    	
    	
    	// Going to int[][]
    	mProcessedImage = toArray(mPictureData);
    }
    
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
    	
    	int tres = 5;
    	
    	for(int i=0;i< values.length;i++){
    		if(values[i][1] > tres){
    			sorted.add(contours.get(values[i][0]));
    		}
    	}
    	
    	return sorted;
    }
    

    public int[][] toArray(Bitmap bmpOriginal) {
        int width, height, threshold;
        height = bmpOriginal.getHeight();
        width = CAMERA_WIDTH;
        threshold = LightTreshold*1000;
       Log.d(LOGTAG, "Treshold : "+threshold);
        int[][] bmpBinary = new int[height][width];

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get one pixel color
                int pixel = bmpOriginal.getPixel(x, y);
                //int value = (int)(((float)Color.red(pixel))*0.2126 + ((float)Color.green(pixel))*0.7152 + ((float)Color.blue(pixel))*0.0722) ;
                int value = (Color.red(pixel)) ;
                if(value > 0){
                	Log.d(LOGTAG,""+value);
                }
                bmpBinary[y][x] = value/1000;
            }
        }
        return bmpBinary;
    }
    
 // Process Single Tap event to trigger autofocus
    private class GestureListener extends
        GestureDetector.SimpleOnGestureListener
    {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();
        
        
        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }
        
        
        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
                    
                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);
            
            return true;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // Process the Gestures
        if (!m_started)
            return true;
        
        return mGestureDetector.onTouchEvent(event);
    }
    
    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
    	@Override
    	public void onManagerConnected(int status) {
    	   switch (status) {
    	       case LoaderCallbackInterface.SUCCESS:
    	       {
    	      Log.i(LOGTAG, "OpenCV loaded successfully");
    	      // Create and set View
    	      //setContentView(R.layout.);
    	       } break;
    	       default:
    	       {
    	      super.onManagerConnected(status);
    	       } break;
    	   }
    	    }
    	};
}
