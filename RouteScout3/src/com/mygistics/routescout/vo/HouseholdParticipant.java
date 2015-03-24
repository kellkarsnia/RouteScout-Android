package com.mygistics.routescout.vo;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HouseholdParticipant
{
	public int sampleNr;
	public int assignmentID;
	public String tDay1; // Time Stamp Participation Begins
	public String tDay2; // Time Stamp Participation Ends
	public ArrayList<HouseholdMember> members;

	private String jstring;
	
	public String getJSONString()
	{
		return jstring;
	}
	
	public HouseholdParticipant(JSONObject jobj)
	{
		jstring = jobj.toString();
		try
		{
			assignmentID = jobj.getInt("AssignmentID");
			members = new ArrayList<HouseholdMember>();
			HouseholdMember member;
			Object item = jobj.getJSONObject("Members").get("HouseholdMember");
		    if (item instanceof JSONArray)
		    {
		        // it's an array
		        JSONArray jmembers = (JSONArray) item;
				JSONObject jmember;
				for(int i=0; i<jmembers.length(); i++)
				{
					jmember = (JSONObject)jmembers.get(i);
					member = new HouseholdMember(jmember);
					members.add(member);
				}
		    }
		    else
		    {
		    	// it's an object
		        JSONObject jmember = (JSONObject) item;
				member = new HouseholdMember(jmember);
				members.add(member);
		    }
			sampleNr = jobj.getInt("SampleNr");
			tDay1 = jobj.getString("TDay1");
			tDay2 = jobj.getString("TDay2");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

}
