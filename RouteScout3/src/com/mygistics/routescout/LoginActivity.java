package com.mygistics.routescout;


import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.mygistics.routescout.actions.UserAction;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends SherlockActivity
{
	private EditText pin_input;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		pin_input = (EditText) findViewById(R.id.pin);
		
		ActionBar ab = getSupportActionBar();
		ab.setTitle("Route Scout");
		ab.setSubtitle("Use your PIN to login.");

		this.getWindow().setBackgroundDrawableResource(R.drawable.road3_bg);
	    TextView v = (TextView)findViewById(R.id.version);
	    try
	    {
		    String ver = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		    v.setText("Version "+ver);
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }

	}
	
	public void onLoginClick(View v)
	{
		// check if pin correct
		UserAction.userPinLogin(this, pin_input.getText().toString());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}
