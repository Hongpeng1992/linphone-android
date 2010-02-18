/*
LinphoneActivity.java
Copyright (C) 2010  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.linphone;


import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

public class LinphoneActivity extends TabActivity {
	public static String DIALER_TAB = "dialer";
	private AudioManager mAudioManager;
	private static LinphoneActivity theLinphoneActivity;

	protected static LinphoneActivity instance()
	  {
		if (theLinphoneActivity == null) {
			throw new RuntimeException("LinphoneActivity not instanciated yet");
		} else {
			return theLinphoneActivity;
		}
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		theLinphoneActivity = this;

		
		mAudioManager = ((AudioManager)getSystemService(Context.AUDIO_SERVICE));
		
		TabHost lTabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Reusable TabSpec for each tab
	   

	    //Call History
	    Intent lHistoryItent =  new Intent().setClass(this, HistoryActivity.class);
	    
	    spec = lTabHost.newTabSpec("history").setIndicator(getString(R.string.tab_history),
	                      null)
	                  .setContent(lHistoryItent);
	    lTabHost.addTab(spec);
	    
	    // dialer
	    Intent lDialerIntent = new Intent().setClass(this, DialerActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = lTabHost.newTabSpec("dialer").setIndicator(getString(R.string.tab_dialer),
	                      getResources().getDrawable(android.R.drawable.ic_menu_call))
	                  .setContent(lDialerIntent);
	    lTabHost.addTab(spec);
	    
	    // contact pick
	    Intent lContactItent =  new Intent().setClass(this, ContactPickerActivity.class);
	    
	    spec = lTabHost.newTabSpec("contact").setIndicator(getString(R.string.tab_contact),
	                      null)
	                  .setContent(lContactItent);
	    lTabHost.addTab(spec);

	    lTabHost.setCurrentTabByTag("dialer");
		// start linphone as background
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClass(this, LinphoneService.class);
		startService(intent);
	    
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if  (isFinishing())  {
			//restaure audio settings
			mAudioManager.setSpeakerphoneOn(true); 
			mAudioManager.setMode(AudioManager.MODE_NORMAL); 
			mAudioManager.setRouting(AudioManager.MODE_NORMAL, 
			AudioManager.ROUTE_SPEAKER, AudioManager.ROUTE_ALL);
			
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setClass(this, LinphoneService.class);
			stopService(intent);
		}
		theLinphoneActivity = null;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the currently selected menu XML resource.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.linphone_activity_menu, menu);


		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startprefActivity();
			return true;
		case R.id.menu_exit:
			finish();
			break;
		default:
			Log.e(LinphoneService.TAG, "Unknown menu item ["+item+"]");
			break;
		}

		return false;
	}
	private void startprefActivity() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClass(this, LinphonePreferencesActivity.class);
		startActivity(intent);
	}
	public void initFromConf() throws LinphoneException {
		
		try {
			LinphoneService.instance().initFromConf();
		} catch (LinphoneConfigException e) {
			handleBadConfig(e.getMessage());
		}
		
	}
	private void handleBadConfig(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(String.format(getString(R.string.config_error),message))
		       .setCancelable(false)
		       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   startprefActivity();
		           }
		       })
		       .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		builder.create().show();
	}
	
}
