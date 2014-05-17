package filRouge.wailord;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SettingsActivity extends Activity implements OnSeekBarChangeListener{
	private static final String TAG = "SActivity";
	SeekBar seekBar_tresh;
	SeekBar seekBar_smooth;
	
	private static final int MAXTRESH = 255;
	private static final int MAXSMOOTH = 8;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        seekBar_tresh = (SeekBar)findViewById(R.id.seekBar_tresh);
        seekBar_tresh.setOnSeekBarChangeListener(this);
        
        seekBar_smooth = (SeekBar)findViewById(R.id.seekBar_smooth);
        seekBar_smooth.setOnSeekBarChangeListener(this);
        
        seekBar_tresh.setProgress((int)(MainActivity.LightTreshold*100.0/MAXTRESH));
        seekBar_smooth.setProgress((int)(MainActivity.SmoothingIterations*100.0/MAXSMOOTH));
        
        updateTresholdProgress(MainActivity.LightTreshold);
        updateSmoothingProgress(MainActivity.SmoothingIterations);
    }
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
    		boolean fromUser) {
    	//Log.d(TAG, "Progress Changed");
    	
    	if(seekBar == seekBar_tresh){
    		progress = progress * MAXTRESH/100;
    		updateTresholdProgress(progress);
    	}
    	else{
    		progress = (int)Math.round(((double)progress * MAXSMOOTH/100.0));
    		updateSmoothingProgress(progress);
    	}
    	
    	
    }
    
    public void updateTresholdProgress(int progress){
    	// Warning : scale from 0 to 255
    	MainActivity.LightTreshold = progress;
    	TextView txtTresh = (TextView)findViewById(R.id.textViewTresh);
    	txtTresh.setText(getResources().getString(R.string.sett_tresh)+ " : " + progress);
    }
    
    public void updateSmoothingProgress(int progress){
    	// Warning : scale from 0 to 8
    	MainActivity.SmoothingIterations = progress;
    	TextView txtSmoo = (TextView)findViewById(R.id.textView_smooth);
    	txtSmoo.setText(getResources().getString(R.string.sett_smooth)+ " : " + progress);
    }

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {	
	}
    
}
