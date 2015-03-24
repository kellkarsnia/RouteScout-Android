package com.mygistics.routescout.actions;

import java.util.ArrayList;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.mygistics.routescout.SessionManager;
import com.mygistics.routescout.TripActivity;
import com.mygistics.routescout.vo.FormattedID;
import com.mygistics.routescout.vo.SurveyTrip;
import com.mygistics.routescout.vo.TracePoint;

public class TripActions
{
	public static final String CREATE_TRIP_SUCCESS = "created trip success";
	public static final String CREATE_TRIP_ERROR = "create trip error";
	public static final String START_TRIP_SUCCESS = "start trip success";
	public static final String START_TRIP_ERROR = "start trip error";
	public static final String UPDATE_TRIP_SUCCESS = "update trip success";
	public static final String UPDATE_TRIP_ERROR = "update trip error";
	public static final String SEND_POINTS_SUCCESS = "send points success";
	public static final String SEND_POINTS_ERROR = "send points error";
	public static final String END_TRIP_SUCCESS = "end trip success";
	public static final String END_TRIP_ERROR = "end trip error";
	
	private static SessionManager sm;
	private static Activity parent;

	/**
	 * CREATE TRIP
	 * @param context
	 */
	public static void createTrip(Activity context)
	{
		parent = context;
		sm = new SessionManager(context);
		
		String pfid = sm.getParticipantFormatedID();
		
		Log.d("TRIP ACTIONS", "pfid "+pfid);
		
		StringEntity se = NustatsHTTPClient.makeStringEntity("survey", "createTrip", pfid);
		
		NustatsHTTPClient.post(context, "MobileSSL", se, "application/json", new JsonHttpResponseHandler()
		{
			@Override
			public void onSuccess(JSONObject response)
			{
				try
            	{
	            	JSONObject resultObject = response.getJSONObject("Result");
					
	            	Log.d("CREATE", resultObject.toString());
	            	
					if(resultObject.getString("Status").contentEquals("OK"))
					{
						int tripId = resultObject.getInt("TripID");
						sm.setTripFormatedID(tripId);
						sm.setCollectedPointsTotal(0);
						sm.setUploadedPointsTotal(0);
						// now lets jump to trip screen
						Intent i = new Intent(parent, TripActivity.class);
						parent.startActivity(i);
					}
					else
					{
						Log.e("Create Trip", "Error "+resultObject);
					}
            	}
            	catch(Exception e)
            	{
            		e.printStackTrace();
            	}
			}
			
			@Override
			public void onFailure(Throwable arg0, JSONObject arg1)
			{
				super.onFailure(arg0, arg1);
			}
		});
	}

	/**
	 * START TRIP
	 * @param context
	 * @param handler
	 */
	public static void startTrip(Context context, final Handler handler)
	{
		String data = "";
		
		data = sm.getParticipantFormatedID() + ",";
		data += sm.getTripFormatedID() + ",";
		
		TracePoint tp = sm.getStartPoint();
		
		data += "\"StartTime\":"+ tp.timestamp+",";
		data += "\"StartLatitude\":"+ tp.latitude+",";
		data += "\"StartLongitude\":"+ tp.longitude;
		
		Log.d("Start Trip:",data);
		
		StringEntity se = NustatsHTTPClient.makeStringEntity("survey", "updateTripSelectively", data);
		
		NustatsHTTPClient.post(context, "MobileSSL", se, "application/json", new JsonHttpResponseHandler()
		{
			@Override
			public void onSuccess(JSONObject response)
			{
				try
            	{
					Message m = Message.obtain(handler);
					
	            	JSONObject resultObject = response.getJSONObject("Result");
	            	Log.d("START TRIP", resultObject.toString());
	            	
					if(resultObject.getString("Status").contentEquals("OK"))
					{
						sm.setTripActive(true);
						m.what = 2;
					}
					else
					{
						sm.setTripActive(false);
						m.what = 3;
						Log.e("Start Trip", "Error "+resultObject);
					}
					
					handler.sendMessage(m); 
            	}
            	catch(Exception e)
            	{
            		e.printStackTrace();
            	}
			}
			
			@Override
			public void onFailure(Throwable arg0, JSONObject response)
			{
				Log.e("Start Trip", "Server Error "+response.toString());
				super.onFailure(arg0, response);
			}
		});
	}

	/**
	 * END TRIP
	 * @param context
	 * @param lastPoint
	 * @param handler
	 */
	public static void endTrip(Context context, TracePoint lastPoint, final Handler handler)
	{
		String data = "";
		
		data = sm.getParticipantFormatedID()+",";
		data += sm.getTripFormatedID() + ",";

		data += "\"EndTime\":"+lastPoint.timestamp+",";
		data += "\"EndLatitude\":"+lastPoint.latitude+",";
		data += "\"EndLongitude\":"+lastPoint.longitude;

		Log.d("END request",data);
		
		StringEntity se = NustatsHTTPClient.makeStringEntity("survey", "updateTripSelectively", data);
		
		NustatsHTTPClient.post(context, "MobileSSL", se, "application/json", new JsonHttpResponseHandler()
		{
			@Override
			public void onSuccess(JSONObject response)
			{
				try
            	{
	            	JSONObject resultObject = response.getJSONObject("Result");
	            	Log.d("END TRIP", resultObject.toString());
	            	
					Message m = Message.obtain();
					Bundle b = new Bundle();
	            	
					if(resultObject.getString("Status").contentEquals("OK"))
					{
						b.putString("msg", "stop ok");
					}
					else
					{
						Log.e("End Trip", "Error "+resultObject);
						b.putString("msg", "stop failed");
					}
					m.setData(b);
					handler.sendMessage(m);
            	}
            	catch(Exception e)
            	{
            		e.printStackTrace();
            	}
			}
			
			@Override
			public void onFailure(Throwable arg0, JSONObject arg1)
			{
				super.onFailure(arg0, arg1);
			}
		});
		
	}

	/**
	 * sendTripPoints
	 * @param context
	 * @param points
	 * @param handler
	 */
	public static void sendTripPoints(Context context, ArrayList<TracePoint> points, final Handler handler)
	{
		if(sm == null)
			sm = new SessionManager(context);
		
		String data =  "\"TripData\":{";
		
		data += sm.getParticipantFormatedID()+",";
		data += sm.getTripFormatedID() + ",";

		// now trace points
		JSONArray tracepoints = new JSONArray();
		
		int total = points.size();
		for(int i=0; i < total; i++)
		{
			tracepoints.put(points.get(i).getJSON());
		}
		data += "\"Trace\":"+tracepoints.toString()+"}";

		StringEntity se = NustatsHTTPClient.makeStringEntity("survey", "addTripTraces", data);

		//Log.d("Add trace","se: "+data);
		
		NustatsHTTPClient.post(context, "MobileSSL", se, "application/json", new JsonHttpResponseHandler()
		{
			@Override
			public void onSuccess(JSONObject response)
			{
				try
            	{
	            	JSONObject resultObject = response.getJSONObject("Result");
					
	            	Log.d("Add points", "result: "+ resultObject.toString());
	            	
					Message m = Message.obtain();
					Bundle b = new Bundle();
	            	
					if(resultObject.getString("Status").contentEquals("OK"))
					{
						b.putString("msg", "sent ok");
					}
					else
					{
						Log.e("Send points result", "Error "+resultObject);
						b.putString("msg", "sent failed");
					}
					m.setData(b);
					handler.sendMessage(m);
            	}
            	catch(Exception e)
            	{
            		e.printStackTrace();
            	}
			}
			
			@Override
			public void onFailure(Throwable arg0, JSONObject arg1)
			{
				Message m = Message.obtain();
				Bundle b = new Bundle();
				b.putString("msg", "sent failed");
				m.setData(b);
				handler.sendMessage(m);
				super.onFailure(arg0, arg1);
			}
		});
		 
	}
	
	public TripActions()
	{}
}