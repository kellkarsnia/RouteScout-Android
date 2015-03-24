package com.mygistics.routescout.vo;

import org.json.JSONException;
import org.json.JSONObject;

public class PinLoginAPI
{
	public String pin;

	public PinLoginAPI()
	{}
	
	public JSONObject getJSON()
	{
		JSONObject pinAPI = new JSONObject();
		
		try
		{
			pinAPI.put("Pin", pin);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		return pinAPI;
	}

	public String getJSONString()
	{
		String pinAPI = "\"Pin\":\""+pin+"\"";
		return pinAPI;
	}

}
