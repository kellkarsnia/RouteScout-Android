package com.mygistics.routescout.vo;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.hardware.GeomagneticField;
import android.location.Location;

public class TracePoint
{
	public int traceID;
	public int tripID;
	public long timestamp;
	
	public double latitude;
	public double longitude;
	public double altitude;
	
	public float accuracy;
	public float speed;
	public float heading;
	
	public String timestring; // format "MM-dd-yyy, HH:mm:ss a"

	public Location location;
	
	private Model model = Model.getInstance();
	
	public JSONObject getJSON()
	{
		JSONObject point = new JSONObject();
		try
		{
			point.put("Latitude", latitude);
			point.put("Longitude", longitude);
			point.put("Timestamp", timestamp);
			point.put("Altitude", altitude);
			point.put("Accuracy", accuracy);
			point.put("Speed", speed);
			point.put("Heading", heading); 
			point.put("Timestring", timestring);
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
		return point;
	}
	
	public TracePoint()
	{}
	
	public TracePoint(Location loc)
	{
		this.location = loc;
		
		timestamp = new Date().getTime()/1000;
			
		latitude = loc.getLatitude();
		longitude = loc.getLongitude();
		altitude = loc.getAltitude();
		accuracy = loc.getAccuracy();
		speed = loc.getSpeed();
		
		/*
        GeomagneticField gmField = new GeomagneticField(
                Double.valueOf(loc.getLatitude()).floatValue(),
                Double.valueOf(loc.getLongitude()).floatValue(),
                Double.valueOf(loc.getAltitude()).floatValue(),
                System.currentTimeMillis()
             );
        */
		
        if(model.lastLocation != null)
        {
        	heading = model.lastLocation.location.bearingTo(location);
        }
        else
        {
        	heading = 0;
        }
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy, HH:mm:ss a");
		timestring = sdf.format(new Date().getTime());
	}
	
}
