package com.mygistics.routescout;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


public class PositionFinder
{
	private Context context;
	private Handler callback;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Location positionLocation;
	private boolean isGPSEnabled;
	private boolean isNetworkEnabled;
	private boolean isMessageSent;

	public PositionFinder(Context cont, Handler handler)
	{
		context = cont;
		callback = handler;
		
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		locationListener = new LocationListener()
		{
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras)
			{
			}
			
			@Override
			public void onProviderEnabled(String provider)
			{
			}
			
			@Override
			public void onProviderDisabled(String provider)
			{
			}
			
			@Override
			public void onLocationChanged(Location location)
			{
        		Log.d("Position finder:","Location changed");
				positionLocation = location;
				locationManager.removeUpdates(locationListener);
				Message m = Message.obtain(callback);
				m.what = 1;
				m.obj = positionLocation;
				callback.sendMessage(m);
			}
		};

        // getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled)
        {
            // no network provider is enabled
        	Toast.makeText(context, "no network provider is enabled", Toast.LENGTH_SHORT).show();
			Message m = Message.obtain(callback);
			m.what = 0;
			callback.sendMessage(m);
			
        }
        else
        {
        	if(isNetworkEnabled)
        	{
        		Log.d("Position finder:","Network provider is enabled");
        		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                positionLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        		if (positionLocation == null)
                {
        			Toast.makeText(context, "Network position not provided, wait for location", Toast.LENGTH_SHORT).show();
                }
        	}
        	
        	if(isGPSEnabled)
        	{
        		Log.d("Position finder:","GPS provider is enabled");

        		if(positionLocation == null)
        		{
    				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        			positionLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        			if(positionLocation == null)
        			{
        				Toast.makeText(context, "GPS position not provided, wait for location", Toast.LENGTH_SHORT).show();
        			}
        		}
        	}
        	
        	if(positionLocation != null)
        	{
        		Log.d("Position finder:","Last known location");
        		locationListener.onLocationChanged(positionLocation);
        	}
        }
		
	}

}
