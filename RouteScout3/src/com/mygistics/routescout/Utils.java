package com.mygistics.routescout;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils
{
	public Utils()
	{
	}
	
	public static boolean isNetworkAvailable(Context context)
	{
		boolean available = false;
		
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
		
		final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		
		if (activeNetwork != null && activeNetwork.isConnected())
		{
		    available = true;
		}
		else
		{
			available = false;
		} 
		return available;
	}
}
