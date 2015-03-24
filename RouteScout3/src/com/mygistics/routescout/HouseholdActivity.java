package com.mygistics.routescout;

import org.json.JSONException;
import org.json.JSONObject;

import com.mygistics.routescout.actions.TripActions;
import com.mygistics.routescout.adapters.MembersArrayAdapter;
import com.mygistics.routescout.vo.HouseholdMember;
import com.mygistics.routescout.vo.HouseholdParticipant;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class HouseholdActivity extends SherlockActivity implements OnItemClickListener
{
	private SessionManager sm;
	private ActionBar actionBar;
	private ListView membersList;
	private MembersArrayAdapter membersAdapter;
	private ImageButton startButton;
	private PendingIntent pIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_household);
		
		this.getWindow().setBackgroundDrawableResource(R.drawable.road3_bg);
		
		sm = new SessionManager(getApplicationContext());
		actionBar = getSupportActionBar();
		membersList = (ListView)findViewById(R.id.membersList);
		membersList.setOnItemClickListener(this);
		startButton = (ImageButton)findViewById(R.id.startBtn);
	}
	
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if(sm.isLoggedIn())
		{
			HouseholdParticipant hhp = sm.getHouseholdParticipantData();
			membersAdapter = new MembersArrayAdapter(this, hhp.members);
			membersList.setAdapter(membersAdapter);
			
			// check if user has active trip
			if(sm.isTripActive())
			{
				// launching trip activity
				Intent i = new Intent(this, TripActivity.class);
				startActivity(i);
			}
			else
			{
				// check if user has selected household member
				if(sm.isHouseholdMemberSelected())
				{
					// show start screen
					HouseholdMember member = sm.getHouseHoldMember(); 
					membersList.setVisibility(View.GONE);
					startButton.setVisibility(View.VISIBLE);
					actionBar.setTitle(member.name);
					actionBar.setSubtitle("New Trip");
					actionBar.setDisplayHomeAsUpEnabled(true);
				}
				else
				{
					// show members list
					actionBar.setSubtitle("select household member");
				}
			}
		}
		else
		{
			// launching login activity
			Intent i = new Intent(this, LoginActivity.class);
			startActivity(i);
		}
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.household, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch(item.getItemId())
		{
		case android.R.id.home:
			startButton.setVisibility(View.GONE);
			membersList.setVisibility(View.VISIBLE);
			actionBar.setTitle("ROUTE SCOUT");
			actionBar.setSubtitle("Household members");
			actionBar.setDisplayHomeAsUpEnabled(false);
			sm.clearHouseHoldMember();
			break;
			
		case R.id.action_logout:
			doLogout();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	

	private void doLogout()
	{
		sm.stopUserSession();
		startButton.setVisibility(View.GONE);
		membersList.setVisibility(View.VISIBLE);
		actionBar.setTitle("ROUTE SCOUT");
		actionBar.setSubtitle("Household members");
		actionBar.setDisplayHomeAsUpEnabled(false);

		// launching login activity
		Intent i = new Intent(this, LoginActivity.class);
		startActivity(i);
	}

	public void doStart(View v)
	{
		if(Utils.isNetworkAvailable(this))
			TripActions.createTrip(this);
		else
			Toast.makeText(this, "Network is not available", Toast.LENGTH_SHORT).show();
				
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3)
	{
		HouseholdMember member = (HouseholdMember) adapter.getItemAtPosition(position); 
		view.setSelected(true);
		membersList.setVisibility(View.GONE);
		startButton.setVisibility(View.VISIBLE);
		actionBar.setTitle(member.name);
		actionBar.setSubtitle("New Trip");
		actionBar.setDisplayHomeAsUpEnabled(true);

		sm.setHouseHoldMember(member);
		sm.setParticipantFormatedID(member.sampleNr * 100 + member.personNr);
		
		//model.participant = new ParticipantAPI();
		//model.participant.participantID = model.member.sampleNr * 100 + model.member.personNr;
		//model.participant.internalID = new FormattedID("ID",model.participant.participantID); 
	}
	
	/**
	 * BACK button deactivate
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
	    {
			startButton.setVisibility(View.GONE);
			membersList.setVisibility(View.VISIBLE);
			actionBar.setTitle("ROUTE SCOUT");
			actionBar.setSubtitle("Household members");
			actionBar.setDisplayHomeAsUpEnabled(false);
			sm.clearHouseHoldMember();
	        return true;
	    }
		return super.onKeyDown(keyCode, event);
	}

}