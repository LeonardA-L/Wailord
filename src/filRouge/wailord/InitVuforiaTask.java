package filRouge.wailord;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.qualcomm.vuforia.Vuforia;

//An async task to initialize Vuforia asynchronously.
public class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean>
{
	private static final String LOGTAG = "InitVuforiaTask";
    // Initialize with invalid value:
    private int mProgressValue = -1;
    private MainActivity m_activity;
    
    public InitVuforiaTask(MainActivity act){
    	m_activity = act;
    	Log.d(LOGTAG, "A");
    }
    
    protected Boolean doInBackground(Void... params)
    {
    	Log.d(LOGTAG, "B");
        // Prevent the onDestroy() method to overlap with initialization:
        synchronized (m_activity.mShutdownLock)
        {
            Vuforia.setInitParameters(m_activity, m_activity.getmVuforiaFlags());
            Log.d(LOGTAG, "C");
            do
            {
                // Vuforia.init() blocks until an initialization step is
                // complete, then it proceeds to the next step and reports
                // progress in percents (0 ... 100%).
                // If Vuforia.init() returns -1, it indicates an error.
                // Initialization is done when progress has reached 100%.
                mProgressValue = Vuforia.init();
                
                // Publish the progress value:
                publishProgress(mProgressValue);
                Log.d(LOGTAG, "PV :"+mProgressValue);
                // We check whether the task has been canceled in the
                // meantime (by calling AsyncTask.cancel(true)).
                // and bail out if it has, thus stopping this thread.
                // This is necessary as the AsyncTask will run to completion
                // regardless of the status of the component that
                // started is.
            } while (!isCancelled() && mProgressValue >= 0
                && mProgressValue < 100);
            
            return (mProgressValue > 0);
        }
    }
    
    
    protected void onProgressUpdate(Integer... values)
    {
        // Do something with the progress value "values[0]", e.g. update
        // splash screen, progress bar, etc.
    }
    
    
    protected void onPostExecute(Boolean result)
    {
        // Done initializing Vuforia, proceed to next application
        // initialization status:
        
    	VuforiaException vuforiaException = null;
        
        if (result)
        {
            Log.d(LOGTAG, "InitVuforiaTask.onPostExecute: Vuforia "
                + "initialization successful");
            
            boolean initTrackersResult;
            initTrackersResult = m_activity.doInitTrackers();
            
            if (initTrackersResult)
            {
                try
                {
                	m_activity.setmLoadTrackerTask( new LoadTrackerTask(m_activity) );
                	m_activity.getmLoadTrackerTask().execute();
                    
                } catch (Exception e)
                {
                    String logMessage = "Loading tracking data set failed";
                    vuforiaException = new VuforiaException(
                    		VuforiaException.LOADING_TRACKERS_FAILURE,
                        logMessage);
                    Log.e(LOGTAG, logMessage);
                    m_activity.onInitARDone(vuforiaException);
                }
                
            } else
            {
                vuforiaException = new VuforiaException(
                		VuforiaException.TRACKERS_INITIALIZATION_FAILURE,
                    "Failed to initialize trackers");
                m_activity.onInitARDone(vuforiaException);
            }
        } else
        {
            String logMessage;
            
            // NOTE: Check if initialization failed because the device is
            // not supported. At this point the user should be informed
            // with a message.
            if (mProgressValue == Vuforia.INIT_DEVICE_NOT_SUPPORTED)
            {
                logMessage = "Failed to initialize Vuforia because this "
                    + "device is not supported.";
            } else
            {
                logMessage = "Failed to initialize Vuforia.";
            }
            
            // Log error:
            Log.e(LOGTAG, "InitVuforiaTask.onPostExecute: " + logMessage
                + " Exiting.");
            
            // Send Vuforia Exception to the application and call initDone
            // to stop initialization process
            vuforiaException = new VuforiaException(
            		VuforiaException.INITIALIZATION_FAILURE,
                logMessage);
            m_activity.onInitARDone(vuforiaException);
        }
    }
}