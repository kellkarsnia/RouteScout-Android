package com.mygistics.routescout.vo;

import java.util.ArrayList;

public class SurveyTrip
{
	public FormattedID tripID;
	public FormattedID internalID;
	public int tripid;
	public TracePoint origin;
	public TracePoint destination;
	public int hidden;
	public ArrayList<TracePoint> trace;
	public TripAttributes tripAttributes;
	
	public SurveyTrip()
	{}

	public String getTripId()
	{
		String result = "\"TripID:\""+tripID.tag;
		
		
		return result;
	}
}
