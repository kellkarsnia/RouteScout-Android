package com.mygistics.routescout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class SplashScreen extends SherlockActivity
{
	// Set the display time, in milliseconds (or extract it out as a configurable parameter)
    private final int SPLASH_DISPLAY_LENGTH = 3000;
    private PendingIntent pIntent;
    
	private Thread.UncaughtExceptionHandler onRuntimeError= new Thread.UncaughtExceptionHandler()
	{
        public void uncaughtException(Thread thread, Throwable ex)
        {
        	AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        	mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, pIntent);
        	System.exit(2);
        }
    };

 
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ActionBar ab = getSupportActionBar();
        if(ab != null)
        	ab.hide();
        setContentView(R.layout.activity_splash_screen);
        
		Thread.setDefaultUncaughtExceptionHandler(onRuntimeError);
		pIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getIntent()), getIntent().getFlags());
		
		TextView v = (TextView)findViewById(R.id.version);
	    try
	    {
		    String ver = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		    v.setText("Version "+ver);
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
    }
 
    @Override
    protected void onResume()
    {
        super.onResume();
 
        new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				SplashScreen.this.finish();
				Intent i = new Intent(SplashScreen.this, HouseholdActivity.class);
				SplashScreen.this.startActivity(i);
			}
		}, SPLASH_DISPLAY_LENGTH);
        		
    }
    
}