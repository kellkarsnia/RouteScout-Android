package com.mygistics.routescout;

import com.mygistics.routescout.actions.TripActions;
import com.mygistics.routescout.vo.Model;
import com.mygistics.routescout.vo.TracePoint;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TripActivity extends SherlockActivity 
{
	private Model model = Model.getInstance();
	private ActionBar actionBar;
	private SessionManager sm;
	private Context context;
	private AlertDialog alertDialog;
	private Handler uiHandler;
	private Handler positionHandler;
	private MenuItem stopTripMenu;
	private LinearLayout progressBar, tripDetail, tripFinish;
	private boolean isInfront;
	private Calculations service;
	private boolean isServiceBinded;
	
	// trip detail data
	//text views to display latitude and longitude
	private TextView latituteField, longitudeField;
	private TextView totalPoints, uploadedPoints;
	private TextView lSpeed, lAccuracy, lAltitude, lHeading;

	
	private ServiceConnection mConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder binder)
		{
			Log.d("TripActivity","service connected");
			Toast.makeText(TripActivity.this, "Service Connected",Toast.LENGTH_SHORT).show();
			service = ((Calculations.MyBinder) binder).getService();
			service.run();
		}
		public void onServiceDisconnected(ComponentName className)
		{
			Toast.makeText(TripActivity.this, "Service disconnected",Toast.LENGTH_SHORT).show();
			Log.d("TripActivity","service disconnected");
			service = null;
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trip);
		
		// background
		this.getWindow().setBackgroundDrawableResource(R.drawable.road3_bg);

		// local broadcast manager to receive messages from service
	    LocalBroadcastManager.getInstance(this)
		.registerReceiver(eventReceiver, new IntentFilter(Calculations.RS_CUSTOM_EVENT));

	    
	    uiHandler = new Handler();
	    
	    positionHandler = new Handler(){

			@Override
			public void handleMessage(Message msg)
			{
				if(msg.what == 1)
				{
					// position found
					Log.d("Trip activity:","what 1 position found");
					actionBar.setSubtitle("start position found");
					TracePoint tp = new TracePoint((Location)msg.obj);
					sm.setStartPoint(tp);

					if(Utils.isNetworkAvailable(context))
						TripActions.startTrip(context, positionHandler);
					else
					{
						actionBar.setSubtitle("Can't start trip - no network.");
						Toast.makeText(context, "Network is not available", Toast.LENGTH_SHORT).show();
						TripActivity.this.finish();
					}
				}
				if(msg.what == 0)
				{
					Log.d("Trip activity:","what 0 providers not available");
					// no active position providers
					Toast.makeText(context, "Location providers are not available.", Toast.LENGTH_SHORT).show();
				}
				if(msg.what == 2)
				{
					Log.d("Trip activity:","what 2 trip started");
	    			stopTripMenu.setVisible(true);
	    			progressBar.setVisibility(View.GONE);
	    			tripDetail.setVisibility(View.VISIBLE);
	    			
	    			actionBar.setSubtitle("Collecting trip points...");
	            	// now start service to collect locations
	    			if(!isServiceBinded)
	    				doBindService();
				}
				if(msg.what == 3)
				{
					Log.d("Trip activity:","what 3 error");
					TripActivity.this.finish();
				}

				super.handleMessage(msg);
			}
	    	
	    };
	    
		context = this;
		sm = new SessionManager(this);
		
		actionBar = getSupportActionBar();
		
		//gps = new Tracker(this, positionHandler);
		
		// start and detail views
		progressBar = (LinearLayout) findViewById(R.id.progress);
		tripDetail = (LinearLayout) findViewById(R.id.tripdetail);
		tripFinish = (LinearLayout) findViewById(R.id.finishtrip);
		
		// detail view
	    totalPoints = (TextView) findViewById(R.id.totalPoints);
	    latituteField = (TextView) findViewById(R.id.lat);
	    longitudeField = (TextView) findViewById(R.id.lon);             
	    uploadedPoints = (TextView) findViewById(R.id.uploaded);
	    lSpeed = (TextView) findViewById(R.id.l_speed);
	    lAltitude = (TextView) findViewById(R.id.l_altitude);
	    lAccuracy = (TextView) findViewById(R.id.l_accuracy);
	    lHeading = (TextView) findViewById(R.id.l_heading);

	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		isInfront = true;
		if(sm.isTripActive())
		{
			actionBar.setSubtitle("collecting points...");
			progressBar.setVisibility(View.GONE);
			tripDetail.setVisibility(View.VISIBLE);
			
			totalPoints.setText("Collected Locations: "+sm.getCollectedPointsTotal());
			uploadedPoints.setText("Uploaded locations: " + sm.getUploadedPointsTotal());
			
			if(!isServiceBinded)
				doBindService();

		}
		else
		{
			actionBar.setSubtitle("Finding current position...");
			tripDetail.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
			Toast.makeText(context, "Finding first position", Toast.LENGTH_SHORT).show();
			
			PositionFinder pf = new PositionFinder(this, positionHandler);
			//	gps.showSettingsAlert();
		}
		
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		isInfront = false;
	}
	
	@Override
	protected void onDestroy()
	{
		LocalBroadcastManager.getInstance(this).unregisterReceiver(eventReceiver);
		
		model.lastLocation = null;
		if(isServiceBinded)
			unbindService(mConnection);
		isServiceBinded = false;
		
		super.onDestroy();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.trip, menu);
		stopTripMenu = menu.findItem(R.id.stop_trip);
		if(sm.isTripActive())
			stopTripMenu.setVisible(true);
		else
			stopTripMenu.setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.action_abort:
			sm.setTripActive(false);
			finish();
			break;
		case R.id.stop_trip:
			showTripDialog();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * BACK button deactivate
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
	    {
	    	showTripDialog();
	        return true;
	    }
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * STOP TRIP DIALOG
	 */
	private void showTripDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Stop current trip?")
               .setPositiveButton("Stop", new DialogInterface.OnClickListener()
               	{
                   public void onClick(DialogInterface dialog, int id)
                   {
                	   //tripStopMenuItem.setEnabled(false);
                	   //tripStopMenuItem.setVisible(false);
                	   dialog.dismiss();
                	   stopTrip();
                   }
               })
               .setNegativeButton("Continue Trip", new DialogInterface.OnClickListener()
               	{
                   public void onClick(DialogInterface dialog, int id)
                   {
                	   dialog.dismiss();
                   }
               });
        builder.create();
		alertDialog = builder.create();
		alertDialog.show();
	}
	
	/**
	 * STOP TRIP
	 */
	private void stopTrip()
	{
		// trip actions stop trip
		if(Utils.isNetworkAvailable(context))
		{
			stopTripMenu.setVisible(false);
			tripFinish.setVisibility(View.VISIBLE);
			tripDetail.setVisibility(View.GONE);
			service.stopTrip();
		}
		else
		{
			Toast.makeText(context, "Network is not available. Can't stop trip now. Try it again when you have network available.", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * BIND SERVICE
	 */
	private void doBindService() 
	{
		isServiceBinded = bindService(new Intent(this, Calculations.class), mConnection, Context.BIND_AUTO_CREATE);
	}

	/**
	 * BROADCAST RECEIVER
	 */
	private BroadcastReceiver eventReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Bundle data = intent.getExtras();
			switch(data.getInt("code"))
			{
		  	case 100:
			  // trip ended successful
		  		if(isInfront)
		  		{	
		  			Toast.makeText(context, "Trip has been finished.", Toast.LENGTH_SHORT).show();
		  		}
		  		if(sm == null)
		  			sm = new SessionManager(context);
		  		
		  		sm.setTripActive(false);
		  		sm.clearStartPoint();
		  		sm.setCollectedPointsTotal(0);
		  		sm.setUploadedPointsTotal(0);
		  		Log.d("TripActivity", "finishing now");
		  		finish();
		  		break;
		  	case 200:
			  // trip not ended successful
		  		if(isInfront)
		  		{	
		  			Toast.makeText(context, "Trip was not finished. Try again.", Toast.LENGTH_SHORT).show();
		  		}
				stopTripMenu.setVisible(true);
				tripFinish.setVisibility(View.GONE);
				tripDetail.setVisibility(View.VISIBLE);
		  		break;
		  		
		  	case 300:
		  	//	Location received
				Log.d("Trip activity:","LOCATION RECEIVED "+data.getInt("code"));
				if(isInfront)
				{
					uiHandler.post(new Runnable()
					{
						@Override
						public void run()
						{
							actionBar.setSubtitle("Collecting trip points...");
							totalPoints.setText("Received locations: " + sm.getCollectedPointsTotal());
							
							latituteField.setText("Latitude: "+model.lastLocation.latitude);
							longitudeField.setText("Longitude: "+model.lastLocation.longitude);
							lSpeed.setText("Speed: "+model.lastLocation.speed);
							lAccuracy.setText("Accuracy: "+model.lastLocation.accuracy);
							lAltitude.setText("Altitude: "+model.lastLocation.altitude);
							lHeading.setText("Heading: "+model.lastLocation.heading);
							
						}
					});
				}
		  		break;
		  
		  	case 400:
		  	//	Points sent to the server
				Log.d("Trip activity:","POINTS SENT TO SERVER "+data.getInt("code"));
		  		
				if(isInfront)
					uiHandler.post(new Runnable()
					{
						@Override
						public void run()
						{
							actionBar.setSubtitle("Points were uploaded successful.");
							uploadedPoints.setText("Uploaded locations: " + sm.getUploadedPointsTotal());
						}
					});
		  		break;
		  	case 401:
				if(isInfront)
				{
					Toast.makeText(TripActivity.this,"Upload Time: Sending data...", Toast.LENGTH_SHORT).show();
				}
		  		break;
		  		
		  	case 404:
				if(isInfront)
				{
					Toast.makeText(TripActivity.this,"Upload Time: Data is not sending.", Toast.LENGTH_SHORT).show();
				}
		  		break;
		  		
		  	case 444:
				if(isInfront)
				{
					Toast.makeText(TripActivity.this,"Data upload failed. Data will be resend next time.", Toast.LENGTH_SHORT).show();
				}
		  		break;
		  		
		  	case 900:
				if(isInfront)
				{
					Toast.makeText(context, "Location provider out of service", Toast.LENGTH_SHORT).show();
				}
		  		break;
		  	case 901:
				if(isInfront)
				{
					Toast.makeText(context, "Location provider temporarely unavailable", Toast.LENGTH_SHORT).show();
				}
		  		break;
		  		
			}
			
		}
	};
	
}