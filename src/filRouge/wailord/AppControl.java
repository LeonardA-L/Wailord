package filRouge.wailord;

/*==============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc.
All Rights Reserved.
==============================================================================*/


import com.qualcomm.vuforia.State;


// Interface to be implemented by the activity which uses SampleApplicationSession
public interface AppControl
{
   
   // To be called to initialize the trackers
   boolean doInitTrackers();
   
   
   // To be called to load the trackers' data
   boolean doLoadTrackersData();
   
   
   // To be called to start tracking with the initialized trackers and their
   // loaded data
   boolean doStartTrackers();
   
   
   // To be called to stop the trackers
   boolean doStopTrackers();
   
   
   // To be called to destroy the trackers' data
   boolean doUnloadTrackersData();
   
   
   // To be called to deinitialize the trackers
   boolean doDeinitTrackers();
   
   
   // This callback is called after the Vuforia initialization is complete,
   // the trackers are initialized, their data loaded and
   // tracking is ready to start
   void onInitARDone(VuforiaException e);
   
   
   // This callback is called every cycle
   void onQCARUpdate(State state);
   
}
