package com.mygistics.routescout.actions;

import java.util.StringTokenizer;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.mygistics.routescout.SessionManager;
import com.mygistics.routescout.vo.PinLoginAPI;

public class UserAction 
{
	private static Activity parent;
	private static SessionManager sm;
	Context context;
	
	public static void userPinLogin(final Activity context, String pin)
	{
		PinLoginAPI plogin = new PinLoginAPI();
		plogin.pin = pin;
		
		parent = context;
		sm = new SessionManager(context);
		Log.d("user action","pin "+pin);

		StringEntity se = NustatsHTTPClient.makeStringEntity("userAccount", "validatePinLogin", plogin.getJSONString());
		
		NustatsHTTPClient.post(context, "MobileSSL", se, "application/json", new JsonHttpResponseHandler()
		{
			@Override
			public void onSuccess(JSONObject response)
			{
				try
            	{
	            	JSONObject resultObject = response.getJSONObject("Result");
					
	            	//Log.d("DATA", resultObject.toString());
	            	
					if(resultObject.getString("Status").contentEquals("OK"))
					{
						JSONObject householdJson = resultObject.getJSONObject("HouseholdParticipant");
						
						String t = resultObject.getString("HouseholdParticipant");
						Log.d("action","participant "+t);
						
						String members = householdJson.getString("Members");
						Log.d("action","Members "+members);
						
						String id = householdJson.getString("AssignmentID");
						Log.d("action","id "+id);
						
						sm.createUserSession(id);
						sm.setHouseholdParticipantData(t);
						
						parent.finish();
					}
					else
					{
						Log.e("Login", "Error "+resultObject);
						String resultObjectToString = resultObject.toString();
						StringTokenizer parseErrorToUserReadable = new StringTokenizer(resultObjectToString, ":");
						//suppresswarnings called on these first 2 strings because we only need them to get the actual
						//warning, boohoo-KK
						@SuppressWarnings("unused")
						String statusString = parseErrorToUserReadable.nextToken();
						@SuppressWarnings("unused")
						String failString = parseErrorToUserReadable.nextToken();
						String thisIsTheErrorToShow = parseErrorToUserReadable.nextToken();
						String trimThisIsTheErrorToShow = thisIsTheErrorToShow.substring(1, thisIsTheErrorToShow.length()-2);
						Toast.makeText(context,"Login error : " + trimThisIsTheErrorToShow, Toast.LENGTH_SHORT).show();
						//model.sendNotify(LOGIN_ERROR);
					}
            	}
            	catch(Exception e)
            	{
            		e.printStackTrace();
            		//model.sendNotify(LOGIN_ERROR);
            	}
			}
			
			@Override
			public void onFailure(Throwable arg0, JSONObject arg1)
			{
				//model.sendNotify(LOGIN_ERROR);
				super.onFailure(arg0, arg1);
			}
		});
	}
	
	public UserAction(){}

}
