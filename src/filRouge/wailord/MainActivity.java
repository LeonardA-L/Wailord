package filRouge.wailord;


import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.Vuforia.UpdateCallbackInterface;

import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class MainActivity extends Activity implements UpdateCallbackInterface, AppControl {

	private static final String LOGTAG = "leMain";
    //private GestureDetector mGestureDetector;
    private Activity m_activity;
    // Vuforia initialization flags:
    private int mVuforiaFlags = 0;
    // Flags
    private boolean m_started = false;
    
    DataSet dataSetUserDef = null;
    

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
   

	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        m_activity = this;
        
        initAR(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        TextView hellotext = (TextView)findViewById(R.id.helloTxt);
        for(int i=0;i<=100;i++){
        	
        	hellotext.setText("lol "+i);
        	hellotext.invalidate();
        	
        	//SystemClock.sleep(500);
        }
       // Supposed to handle autofocus. Not yet.
       // The class is in the UserDefinedTarget sample
       // mGestureDetector = new GestureDetector(this, new GestureListener());
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

	@Override
	public void QCAR_onUpdate(State arg0) {
		// TODO Auto-generated method stub
		
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
        
        /*
        if (refFreeFrame != null)
            refFreeFrame.deInit();
        */
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ImageTracker.getClassType());
        
        return result;
    }

	@Override
    public void onInitARDone(VuforiaException exception)
    {
		 Log.e(LOGTAG, "onInitARDone");
        
    }

	private void initApplicationAR() {
		// TODO Auto-generated method stub
		 Log.e(LOGTAG, "initApplicationAR");
		TextView hellotext = (TextView)findViewById(R.id.helloTxt);
    	hellotext.setText("IAAR");
	}

	@Override
    public void onQCARUpdate(State state)
    {
        
    }
}
