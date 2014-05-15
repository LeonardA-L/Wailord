package filRouge.wailord;

import android.os.AsyncTask;
import android.util.Log;

import com.qualcomm.vuforia.Vuforia;

//An async task to load the tracker data asynchronously.
public class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean>
{
	private static final String LOGTAG = "LoadTrackerTask";
    private MainActivity m_activity;
	
	public LoadTrackerTask(MainActivity act){
    	m_activity = act;
    }
	
    protected Boolean doInBackground(Void... params)
    {
        // Prevent the onDestroy() method to overlap:
        synchronized (m_activity.mShutdownLock)
        {
            // Load the tracker data set:
            return m_activity.doLoadTrackersData();
        }
    }
    
    
    protected void onPostExecute(Boolean result)
    {
        
        VuforiaException vuforiaException = null;
        
        Log.d(LOGTAG, "LoadTrackerTask.onPostExecute: execution "
            + (result ? "successful" : "failed"));
        
        if (!result)
        {
            String logMessage = "Failed to load tracker data.";
            // Error loading dataset
            Log.e(LOGTAG, logMessage);
            vuforiaException = new VuforiaException(
            		VuforiaException.LOADING_TRACKERS_FAILURE,
                logMessage);
        } else
        {
            // Hint to the virtual machine that it would be a good time to
            // run the garbage collector:
            //
            // NOTE: This is only a hint. There is no guarantee that the
            // garbage collector will actually be run.
            System.gc();
            
            Vuforia.registerCallback(m_activity);
            
           m_activity.setM_started(true);
        }
        
        // Done loading the tracker, update application status, send the
        // exception to check errors
        m_activity.onInitARDone(vuforiaException);
    }
}
