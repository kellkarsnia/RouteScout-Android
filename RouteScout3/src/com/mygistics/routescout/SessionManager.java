package com.mygistics.routescout;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.mygistics.routescout.vo.HouseholdMember;
import com.mygistics.routescout.vo.HouseholdParticipant;
import com.mygistics.routescout.vo.SurveyTrip;
import com.mygistics.routescout.vo.TracePoint;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.GeomagneticField;

public class SessionManager
{
	// preferences
	private SharedPreferences preferences;
	// editor
	private Editor editor;
	// context
	private Context context;
	
	public SessionManager(Context context)
	{
		this.context = context;
		preferences = context.getSharedPreferences("RouteScout", 0);
		editor = preferences.edit();
		editor.commit();
	}
	
	// create session
	public void createUserSession(String pin)
	{
		editor.putString("PIN", pin);
		editor.putBoolean("IS LOGIN", true);
		editor.commit();
	}
	
	// get user session
	public String getUserSession()
	{
		return preferences.getString("TRIP DATA", "");
	}
	
	// check session is user logged in
	public boolean isLoggedIn()
	{
		return preferences.getBoolean("IS LOGIN", false);
	}

	// check session is user has active trip
	public boolean isTripActive()
	{
		return preferences.getBoolean("IS TRIP ACTIVE", false);
	}
	// set trip active
	public void setTripActive(boolean flag)
	{
		editor.putBoolean("IS TRIP ACTIVE", flag);
		editor.commit();
	}
	
	// check session is household member selected
	public boolean isHouseholdMemberSelected()
	{
		return preferences.getBoolean("IS MEMBER SELECTED", false);
	}

	// get active household member
	public HouseholdMember getHouseHoldMember()
	{
		String member = preferences.getString("HOUSEHOLD MEMBER", "");
		try
		{
			if(member != "")
			{
				JSONObject jm = new JSONObject(member);
				return new HouseholdMember(jm);
			}
			else
			{
				return null;
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	// set active household member
	public void setHouseHoldMember(HouseholdMember member)
	{
		editor.putString("HOUSEHOLD MEMBER", member.getJSON());
		editor.putBoolean("IS MEMBER SELECTED", true);
		editor.commit();
	}
	
	// clear active household member
	public void clearHouseHoldMember()
	{
		editor.remove("HOUSEHOLD MEMBER");
		editor.putBoolean("IS MEMBER SELECTED", false);
		editor.commit();
	}
	
	// get household participant data
	public HouseholdParticipant getHouseholdParticipantData()
	{
		String participant = preferences.getString("HOUSEHOLD PARTICIPANT", "");
		try
		{
			if(participant != "")
			{
				return new HouseholdParticipant(new JSONObject(participant));
			}
			else
			{
				return null;
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	// get household participant data
	public void setHouseholdParticipantData(String participant)
	{
		editor.putString("HOUSEHOLD PARTICIPANT", participant);
		editor.commit();
	}

	// set trip data
	public void setTripData(SurveyTrip trip)
	{
	}
	// get trip data
	public SurveyTrip getTripData()
	{
		SurveyTrip trip = new SurveyTrip();
		return trip; 
	}
	
	// set Start Point
	public void setStartPoint(TracePoint tp)
	{
		editor.putLong("START POINT TIMESTAMP", tp.timestamp);
		editor.putString("START POINT LATITUDE", Double.toString(tp.latitude));
		editor.putString("START POINT LONGITUDE", Double.toString(tp.longitude));
		editor.putString("START POINT ALTITUDE", Double.toString(tp.altitude));
		editor.putFloat("START POINT ACCURACY", tp.accuracy);
		editor.putString("START POINT TIMESTRING",tp.timestring);
		editor.commit();
	}
	// get Start Point
	public TracePoint getStartPoint()
	{
		TracePoint tp = new TracePoint();
		tp.timestamp =	preferences.getLong("START POINT TIMESTAMP", 0);
		tp.latitude =	Double.parseDouble(preferences.getString("START POINT LATITUDE", "0"));
		tp.longitude =	Double.parseDouble(preferences.getString("START POINT LONGITUDE", "0"));
		tp.altitude =	Double.parseDouble(preferences.getString("START POINT ALTITUDE", "0"));
		tp.accuracy = 	preferences.getFloat("START POINT ACCURACY", 0f);
		tp.speed = 		0;
		tp.heading = 0;
		tp.timestring = preferences.getString("START POINT TIMESTRING", "");
		
		return tp;
	}
	//clear start point
	public void clearStartPoint()
	{
		editor.remove("START POINT TIMESTAMP");
		editor.remove("START POINT LATITUDE");
		editor.remove("START POINT LONGITUDE");
		editor.remove("START POINT ALTITUDE");
		editor.remove("START POINT ACCURACY");
		editor.remove("START POINT TIMESTRING");
		editor.commit();
	}
	

	// get trip formated id
	public String getTripFormatedID()
	{
		return preferences.getString("TRIP FORMATED ID", "");
	}
	// set trip formated id
	public void setTripFormatedID(int id)
	{
		String fid = "\"TripID\":\""+id+"\"";
		editor.putString("TRIP FORMATED ID", fid);
		editor.commit();
	}

	// get participant formated id
	public String getParticipantFormatedID()
	{
		return preferences.getString("PARTICIPANT FORMATED ID", "");
	}
	// set participant formated id
	public void setParticipantFormatedID(int id)
	{
		String fid = "\"ID\":\""+id+"\"";
		editor.putString("PARTICIPANT FORMATED ID", fid);
		editor.commit();
	}
	
	//set amount of collected points
	public void setCollectedPointsTotal(int total)
	{
		editor.putInt("TOTAL POINTS COLLECTED", total);
		editor.commit();
	}
	//get amount of collected points
	public int getCollectedPointsTotal()
	{
		return preferences.getInt("TOTAL POINTS COLLECTED", 0);
	}
	
	//set amount of uploaded points
	public void setUploadedPointsTotal(int total)
	{
		editor.putInt("TOTAL POINTS UPLOADED", total);
		editor.commit();
	}
	//get amount of collected points
	public int getUploadedPointsTotal()
	{
		return preferences.getInt("TOTAL POINTS UPLOADED", 0);
	}

	
	
	// clear session
	public void stopUserSession()
	{
		editor.clear();
		editor.commit();
	}

}
