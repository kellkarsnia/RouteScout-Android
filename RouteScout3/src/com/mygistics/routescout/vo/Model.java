package com.mygistics.routescout.vo;

import java.util.Observable;

import android.location.Criteria;
import android.location.LocationManager;
import android.net.ConnectivityManager;

public class Model extends Observable
{
	private static Model model;

	public HouseholdParticipant household;
	public HouseholdMember member;
	public ParticipantAPI participant;
	public SurveyTrip trip;
	
	public int locationsCounter;
	public int uploadedPoints;
	
	public boolean isNetworkAvailable;
	
	public TracePoint lastLocation;

	public static Model getInstance()
	{
		if(model == null)
			model = new Model();
		return model;
	}
	
	public void sendNotify(Object obj)
	{
		setChanged();
		notifyObservers(obj);
	}
	
	public Model()
	{}
}