package com.mygistics.routescout;

import java.util.ArrayList;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import com.mygistics.routescout.actions.TripActions;
import com.mygistics.routescout.vo.Model;
import com.mygistics.routescout.vo.TracePoint;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class Calculations extends Service implements Observer
{
	public static String RS_CUSTOM_EVENT = "routescout event";
	
	/*
    // The minimum distance to change Updates in meters
    private final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5F; // meters
 
    // The minimum time between updates in milliseconds
    private final long MIN_TIME_BW_UPDATES = 10000L; // 10 sec
	 */

	private Model model = Model.getInstance();
	private SessionManager sm;
	
	private ArrayList<TracePoint> collectedFromGPS;
	private ArrayList<TracePoint> uploadToServer;
	
	private boolean isUploadingData;
	private boolean isEndingTrip;
	private TracePoint lastPoint;
	
	static LocationManager locationManager;
	static LocationListener locationListener;
	private Context context;
	
	private TracePoint tracePoint;
	
	private int totalCollectedPoints;
	private int totalUploadedPoints;
	private Timer uploadTimer;
	
    private Handler handler;

    private final IBinder mBinder = new MyBinder();
    
	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}
	
	public class MyBinder extends Binder
	{
	    Calculations getService()
	    {
	      return Calculations.this;
	    }
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		context = this;
	    Log.d("SERVICE ON CREATE", "service created");
	    
	    sm = new SessionManager(context);
	    totalCollectedPoints = sm.getCollectedPointsTotal();
	    totalUploadedPoints = sm.getUploadedPointsTotal();
	    
	    collectedFromGPS = new ArrayList<TracePoint>();
	    uploadToServer = new ArrayList<TracePoint>();
	    isUploadingData = false;
	    
	    handler = new Handler()
	    {
	    	@Override
	    	public void handleMessage(Message msg)
	    	{
	    		super.handleMessage(msg);
	            Bundle b = msg.getData();
	            if(b.getString("msg").equals("sent ok"))
	            {
	            	//Toast.makeText(context, "Points were uploaded OK", Toast.LENGTH_SHORT).show();
	            	if(isEndingTrip)
	            	{
	            		// send command to stop trip
	            		TripActions.endTrip(context, uploadToServer.get(uploadToServer.size()-1), handler);
	            	}
	            	totalUploadedPoints += uploadToServer.size();
	            	sm.setUploadedPointsTotal(totalUploadedPoints);
	            	sendEvent("code", 400);
	            	uploadToServer.clear();
	            	isUploadingData = false;
	            }else if(b.getString("msg").equals("sent failed"))
	            {
	            	isUploadingData = false;
	            	//Toast.makeText(context, "Points were NOT uploaded", Toast.LENGTH_SHORT).show();
	            	sendEvent("code", 444);
	            }
	            else if(b.getString("msg").equals("stop ok"))
	            {
	            	//Toast.makeText(context, "Trip stopped fine", Toast.LENGTH_SHORT).show();
	            	sendEvent("code", 100);
	            }
	            else if(b.getString("msg").equals("stop failed"))
	            {
	            	Toast.makeText(context, "Trip was not stopped", Toast.LENGTH_SHORT).show();
	            	sendEvent("code", 200);
	            }
	    	}
	    };
	}

	@Override
	public void onDestroy()
	{
		Log.d("SERVICE ON DESTROY","before destroy");
		
		if(uploadTimer != null)
			uploadTimer.cancel();
		
		if(locationManager != null)
			locationManager.removeUpdates(locationListener);
		locationManager = null;
		
		super.onDestroy();
	}


	public void run()
	{
		Log.d("SERVICE RUN", "service runing");

		  //Acquire a reference to the system Location Manager
	    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	    
	    final Criteria criteria = new Criteria();
	    	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	    	criteria.setSpeedRequired(true);
	    	criteria.setAltitudeRequired(true);
	    	criteria.setBearingRequired(true);
	    	criteria.setCostAllowed(true);
	    	criteria.setPowerRequirement(Criteria.POWER_LOW);
	    	
	    String bestProvider = locationManager.getBestProvider(criteria, true);
	    
	    startTimer(0);
	    	
	    // Define a listener that responds to location updates
	    locationListener = new LocationListener()
	    	{
	    		public void onLocationChanged(Location newLocation) 
	    		{
	    			totalCollectedPoints++;
    				tracePoint = new TracePoint(newLocation);
    				sm.setCollectedPointsTotal(totalCollectedPoints);
    				
    				collectedFromGPS.add(tracePoint);
    				lastPoint = tracePoint;
    				model.lastLocation = tracePoint;
		    		sendEvent("code", 300);
	    		}
	
		        public void onStatusChanged(String provider, int status, Bundle extras) 
		        {
		        	switch (status)
		        	{
		            case LocationProvider.AVAILABLE:
		                break;
		            case LocationProvider.OUT_OF_SERVICE:
		            	sendEvent("code", 900);
		                break;
		            case LocationProvider.TEMPORARILY_UNAVAILABLE:
		            	sendEvent("code", 901);
		                break;
		        	}
		        }
		        public void onProviderEnabled(String provider) 
		        {
		        	Toast.makeText(context, "Location Provider: "+provider+" enabled", Toast.LENGTH_SHORT).show();
		        }
		        public void onProviderDisabled(String provider) 
		        {
		        	Toast.makeText(context, "Location Provider: "+provider+" disabled", Toast.LENGTH_LONG).show();
		        }
	    	};
	
	    // Register the listener with the Location Manager to receive location updates
	    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 1F, locationListener);
	    //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 1F, locationListener);

//	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
//	    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

	    // using criteria
	    locationManager.requestLocationUpdates(bestProvider, 0, 50, locationListener);
	    // 5000L 5 sec, 60F 60m
	}
	
	public void stopTrip()
	{
		stopTimer();
		prepareStopTrip();
	}

	/**
	 * Observer Update
	 * 
	 */
	@Override
	public void update(Observable observable, Object data)
	{
	}

	/**
	 * Send Event to activity
	 * @param type: string code type
	 * @param code: int code 
	 */
	private void sendEvent(String type, int code)
	{
		Bundle bundle = new Bundle();
		bundle.putInt(type, code);
		Intent eventIntent = new Intent(RS_CUSTOM_EVENT);
		eventIntent.putExtras(bundle);
		LocalBroadcastManager.getInstance(context).sendBroadcast(eventIntent);
	}
	
	/**
	 * TIMER for sending trip points
	 */
	protected void startTimer(long delay)
	{
		long period = 60000L; // 60 seconds
		uploadTimer = new Timer();
		uploadTimer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				tickTask();
			}
		}, delay, period);
	}
	protected void stopTimer()
	{
		if(uploadTimer != null)
			uploadTimer.cancel();
	}
	
	protected void tickTask()
	{
		// now 
		Log.d("TIK", "collected size: "+collectedFromGPS.size());
		if(!isUploadingData && Utils.isNetworkAvailable(context) && collectedFromGPS.size() > 0)
		{
			sendEvent("code", 401);
			for(TracePoint tp : collectedFromGPS)
			{
				uploadToServer.add(tp);
			}
			collectedFromGPS.clear();
			isUploadingData = true;
			TripActions.sendTripPoints(context, uploadToServer, handler);
		}
		else
		{
			sendEvent("code", 404);
		}
	}
	
	protected void prepareStopTrip()
	{
		isEndingTrip = true;
		if(Utils.isNetworkAvailable(context))
		{
			// lets check if we have collected points
			 if(collectedFromGPS.size() > 0)
			 {
				for(TracePoint tp : collectedFromGPS)
				{
					uploadToServer.add(tp);
				}
				collectedFromGPS.clear();
				isUploadingData = true;
				TripActions.sendTripPoints(context, uploadToServer, handler);
			 }
			 else
			 {
				 TripActions.endTrip(context, lastPoint, handler);
			 }
			
		}
		
	}
}