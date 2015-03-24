package com.mygistics.routescout.actions;


import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;




import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class NustatsHTTPClient
{
	private static final String BASE_URL = "http://routescout.nustats.com:8080/MobileSSL/";
	private static AsyncHttpClient client = new AsyncHttpClient();

	public NustatsHTTPClient()
	{
	}

	public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler)
	{
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler)
	{
		client.setTimeout(6*10*1000); // 1 minute
		client.post(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void post(Context context, String url, StringEntity entity, String contentType, AsyncHttpResponseHandler responseHandler)
	{
		client.setTimeout(6*10*1000); // 1 minute
		client.post(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
	}

	private static String getAbsoluteUrl(String relativeUrl)
	{
		return BASE_URL + relativeUrl;
	}

	
	public static StringEntity makeStringEntity(String objectName, String actionName, String data)
	{
		StringEntity se = null;
		
		// make attributes
		JSONObject attributes = new JSONObject();
		String atr = "\"Attributes\":";
		try
		{
			attributes.put("Object",objectName);
			attributes.put("Action",actionName);
			atr += attributes.toString();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		// make debug request string
		//String resultJSON = "{ \"API\":{"+atr+","+data+","+debugKey+"}}";
		// make nustats request string
		String resultJSON = "{ \"API\":{"+atr+","+data+"}}";
		
		try
		{
			se = new StringEntity(resultJSON);
		} catch (UnsupportedEncodingException e1)
		{
			e1.printStackTrace();
		} 
		
		return se;
	}
}