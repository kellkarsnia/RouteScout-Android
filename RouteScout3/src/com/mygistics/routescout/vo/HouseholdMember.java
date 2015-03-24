package com.mygistics.routescout.vo;

import org.json.JSONException;
import org.json.JSONObject;

public class HouseholdMember
{
	public int sampleNr;
	public int personNr;
	public String name;
	public int age;
	
	private String json;

	public HouseholdMember(JSONObject jobj)
	{
		json = jobj.toString();
		try
		{
			name = jobj.getString("Name");
			sampleNr = jobj.getInt("SampleNr");
			personNr = jobj.getInt("PersonNr");
			age = jobj.getInt("Age");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getJSON()
	{
		return json;
	}
}