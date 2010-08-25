package com.qualcomm.wifi.softap;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.qualcomm.wifi.softap.ns.NetworkSettings;
import com.qualcomm.wifi.softap.ss.StationStatus;
import com.qualcomm.wifi.softap.ws.BasicWirelessSettings;
import com.qualcomm.wifi.softap.ws.WirelessSettings;

public class MainMenuSettings extends PreferenceActivity implements
OnPreferenceClickListener, OnPreferenceChangeListener, OnClickListener, QWiFiSoftApEvent {
	public static QWiFiSoftApCfg mSoftAPCfg;
	public static StationStatus ssrefToMMS;
	public static APEventMonitor apmonitor;
	public boolean SSRUNNING=false;
	private ProgressDialog dialOn,dialOff,dialSave,dialReset;
	private int EVENT_ID=1;
	private final int DIALOG_OFF=0,DIALOG_ON=1,DIALOG_RESET=2,DIALOG_SAVE=3;	
	public static Context appcontext;
	public static String TAG;
	public static final String setCmdPrefix = "set ";
	public static final String getCmdPrefix = "get ";	
	private Handler handleLclThrd;
	private Preference wsPref, nsPref, statusPref;
	private CheckBoxPreference wifiCheckEnable;
	private SharedPreferences.Editor defaultEditor,oldconfigeditor;	
	private SharedPreferences preferences, oldconfigs;
	private String KeyVal;
	private int MAX_LENGTH = 15,index;
	private String ALLOW = "allow",DENY = "deny",response;
	private NotificationManager nm;
	public static String wpaResponse,commitResponse;
	private String allowAddList=new String("");
	private String allowRemoveList=new String("");
	private String denyAddList=new String("");
	private String denyRemoveList=new String("");
	private String port_filter = new String("");
	private String port_forward = new String("");

	private ArrayList<String> changes=new ArrayList<String>();	
	public static boolean status = false,preferenceChanged;
	private Button save, reset;
	private String CheckKeyValue;
	private String[] br_opt;
	private String ENABLE_SOFTAP = "enable_softap";
	private String[] internalKey ;

	public void EventHandler(String evt)
	{
		Log.e("EVTHLR", evt);		
		showNotification(evt);
		Intent i = new Intent(MainMenuSettings.this,StationStatus.class);
		
		if(evt.contains("102")){			
			Log.d("EventHandler","Asso StationStatus:"+SSRUNNING);
			if(SSRUNNING){
				if(ssrefToMMS!=null){
					Log.d("EventHandler","ASSOCIATE REF NOT NULL StationStatus:"+SSRUNNING);					
					ssrefToMMS.finish();
					startActivityForResult(i, 0);
					SSRUNNING = true;
				}

			}
			Log.d(TAG,"Recieved 102 Station");
			if(BasicWirelessSettings.wpsAlertDialog!=null)
			{				
				Log.d(TAG,"dismissing wpsAlertDialogue");
				BasicWirelessSettings.wpsAlertDialog.dismiss();
			}
		}
		else if(evt.contains("103")){
			Log.d("EventHandler","Disasso StationStatus:"+SSRUNNING);
			if(SSRUNNING){
				if(ssrefToMMS!=null){
					Log.d("EventHandler","DISASSOCIATE REF NOT NULL StationStatus:"+SSRUNNING);
					ssrefToMMS.finish();
					startActivityForResult(i, 0);
					SSRUNNING = true;
				}

			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		appcontext=getApplicationContext();		
		addPreferencesFromResource(R.xml.main_settings_pref);
		setContentView(R.layout.mms_layout);
		TAG = getString(R.string.tag)+"MMS";
		mSoftAPCfg = new QWiFiSoftApCfg(this);		
		internalKey = getResources().getStringArray(R.array.internalKeys);
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		defaultEditor = preferences.edit();		
		oldconfigs = getSharedPreferences("oldconfig", MODE_PRIVATE);
		oldconfigeditor = oldconfigs.edit();

		wsPref = (Preference) findPreference(getString(R.string.ws_key));
		nsPref = (Preference) findPreference(getString(R.string.ns_key));
		statusPref = (Preference) findPreference(getString(R.string.station_status_key));
		wifiCheckEnable = (CheckBoxPreference) findPreference(getString(R.string.wifi_status_key));
		wsPref.setOnPreferenceClickListener(this);
		nsPref.setOnPreferenceClickListener(this);	
		statusPref.setOnPreferenceClickListener(this);

		wifiCheckEnable.setOnPreferenceChangeListener(this);
		wifiCheckEnable.setOnPreferenceClickListener(this);

		save = (Button) findViewById(R.id.save);
		reset = (Button) findViewById(R.id.reset);
		save.setOnClickListener(this);
		reset.setOnClickListener(this);		
		save.setEnabled(false);
		preferenceChanged = false;

		// ********************** Get initial Key-Value // ****************************


		Log.d(TAG,"Getting Command "+getCmdPrefix +ENABLE_SOFTAP);
		KeyVal = mSoftAPCfg.SapSendCommand(getCmdPrefix +ENABLE_SOFTAP);
		Log.d(TAG,"Received response "+KeyVal);
		if(KeyVal.contains("success")){
			if(KeyVal.contains("1")){				
				enableTrue();
			} else if(KeyVal.contains("0")){			
				enableFalse();
			}
		}else {
			wifiCheckEnable.setEnabled(false);
		}

		oldconfigeditor.putString("wpsKey", "");
		oldconfigeditor.commit();

		if(!wifiCheckEnable.isChecked()){
			save.setEnabled(false);
			reset.setEnabled(false);
		}
		handleLclThrd=new Handler()
		{
			public void handleMessage(Message m) {
				Log.d(TAG+"Handler","LclThr Mes:"+m.getData());
			}
		};
	}

	public void onClick(View v) {
		int id = v.getId();
		switch(id){
		case R.id.save:
			new dialogThr(DIALOG_SAVE);
			saveChanges(preferences,oldconfigs,internalKey,getApplicationContext());
			save.setEnabled(false);
			if(dialSave!=null)dialSave.cancel();			
			break;
		case R.id.reset:
			new dialogThr(DIALOG_RESET);
			mSoftAPCfg.SapSendCommand(setCmdPrefix +"reset_to_default");			
			getDataFromServer();
			Toast.makeText(getApplicationContext(),"SoftAP Reset",Toast.LENGTH_LONG).show();
			save.setEnabled(false);	
			if(dialReset!=null) dialReset.cancel();
			break;
		}		
	}
	public void getDataFromServer(){
		final String[] defaultKey = getResources().getStringArray(R.array.defaultProfileKeys);
		for(int j=0;j<defaultKey.length;j++){
			Log.d(TAG,"Getting Command "+getCmdPrefix +defaultKey[j]);			
			KeyVal = mSoftAPCfg.SapSendCommand(getCmdPrefix +defaultKey[j]);
			Log.d(TAG,"Received response "+KeyVal);	
			index = KeyVal.indexOf("=");
			if(defaultKey[j].equals("allow_list")){
				defaultEditor.putString("allow_list",KeyVal.substring(index+1,KeyVal.length()));
				oldconfigeditor.putString("allow_list",KeyVal.substring(index+1,KeyVal.length()));
				GetAllowDenyValue(defaultKey[j]);
			}else if(defaultKey[j].equals("deny_list")){
				defaultEditor.putString("deny_list",KeyVal.substring(index+1,KeyVal.length()));
				oldconfigeditor.putString("deny_list",KeyVal.substring(index+1,KeyVal.length()));
				GetAllowDenyValue(defaultKey[j]);
			} else {
				if(KeyVal.contains("success")) {
					CheckKeyValue = KeyVal.substring(KeyVal.indexOf(" ")+1,index);
					if(CheckKeyValue.equals(defaultKey[j])){						
						if(defaultKey[j].equals("channel")){
							if(KeyVal.substring(index+1,KeyVal.length()).contains(",")){
								Log.d(TAG,"Comma "+KeyVal.substring(index+1,KeyVal.length()).contains(","));
								CheckKeyValue = KeyVal.substring(index+1,KeyVal.indexOf(","));
								defaultEditor.putString("autoChannel",CheckKeyValue);
								oldconfigeditor.putString("autoChannel",CheckKeyValue);
								CheckKeyValue = KeyVal.substring(KeyVal.indexOf(",")+1,KeyVal.length());
								defaultEditor.putString("channel",CheckKeyValue);
								oldconfigeditor.putString("channel",CheckKeyValue);
								defaultEditor.commit();
								oldconfigeditor.commit();
							} else {
								SetDefaultValues(defaultKey[j]);
							}
						} else {
							if(defaultKey[j].equals("country_code")){
								KeyVal = KeyVal.substring(0, KeyVal.length()-1);
							}
							SetDefaultValues(defaultKey[j]);
						}
					}
				} else {					
					defaultEditor.putString(defaultKey[j],"");
					oldconfigeditor.putString(defaultKey[j],"");
					defaultEditor.commit();
					oldconfigeditor.commit();
				}
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);		
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(save.isEnabled()){
				new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(getString(R.string.alert_save))
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {	
						saveChanges(preferences,oldconfigs,internalKey,getApplicationContext());
						save.setEnabled(false);
						Log.d(TAG, "Changes Saved");
						finish();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {						
						SetOriginalValues();
						Log.d(TAG, "Changes Cancelled");
						finish();
					}
				})
				.create().show();
			}			
			return true;
		}else {			
			return super.onKeyDown(keyCode, event);				
		}
	}

	public void saveChanges(SharedPreferences preferences,SharedPreferences oldconfigs,String[] internalKeys,Context context){
		int successCount=0;
		int unsupportedCount=0;		

		defaultEditor = preferences.edit();	
		oldconfigeditor = oldconfigs.edit();
		ArrayList<String> changedCommad = comparePreferenceFiles(preferences,oldconfigs,internalKeys);
		Log.d(TAG,"LIST "+changedCommad);
		if(changedCommad!=null){
			for(int i=0;i<changedCommad.size();i++){
				Log.d(TAG,"Sending Command "+setCmdPrefix +changedCommad.get(i));

				response = mSoftAPCfg.SapSendCommand(setCmdPrefix +changedCommad.get(i));

				Log.d(TAG,"Response "+response);

				String setKey = changedCommad.get(i).substring(0,changedCommad.get(i).indexOf("="));
				Log.d(TAG,"setKey "+setKey);
				String setValue = changedCommad.get(i).substring(changedCommad.get(i).indexOf("=")+1,changedCommad.get(i).length());
				Log.d(TAG,"setValue "+setValue);
				if(setKey.equals("wps_state")){
					wpaResponse = response;
				}
				if(response.equals("success")){
					if(setKey.equals("add_to_allow_list")||setKey.equals("add_to_deny_list")){
						int index = changedCommad.get(i).indexOf("=");
						String value = changedCommad.get(i).substring(index+1,changedCommad.get(i).length());
						Log.d(TAG,"KeyVal "+value);
						StringTokenizer st = new StringTokenizer(value);									
						while(st.hasMoreElements()){
							String nextToken = st.nextToken();
							Log.d(TAG,"setKey.contains(ALLOW)"+setKey.contains(ALLOW) +" Token Value: "+nextToken);										
							if(setKey.contains(ALLOW)){								
								for (int m = 1; m <= MAX_LENGTH; m++) {
									String checkvalue = oldconfigs.getString(ALLOW+m, "");	
									if(checkvalue.equals("")) {
										oldconfigeditor.putString(ALLOW+m,nextToken);
										oldconfigeditor.commit();
										break;
									}
								}											
							}else{	
								for (int m = 1; m <= MAX_LENGTH; m++) {
									String checkvalue = oldconfigs.getString(DENY+m, "");	
									if(checkvalue.equals("")) {
										oldconfigeditor.putString(DENY+m,nextToken);
										oldconfigeditor.commit();
										break;
									}
								}											
							}									
						}
					}else if(setKey.equals("remove_from_allow_list")||setKey.equals("remove_from_deny_list")){
						int index = changedCommad.get(i).indexOf("=");
						String value = changedCommad.get(i).substring(index+1,changedCommad.get(i).length());
						Log.d(TAG,"KeyVal "+value);
						StringTokenizer st = new StringTokenizer(value);

						while(st.hasMoreElements()){										
							String nextToken = st.nextToken();
							if(setKey.contains(ALLOW)){											
								for( int m = 1; m <= MAX_LENGTH; m++){												
									String findVal = oldconfigs.getString(ALLOW+m, "");	
									if(nextToken.equals(findVal)){
										oldconfigeditor.putString(ALLOW+m, "");
										oldconfigeditor.commit();
										for(int l = 1; l < MAX_LENGTH; l++ ) {
											if(oldconfigs.getString(ALLOW+l,"").equals("")) {
												String val = oldconfigs.getString(ALLOW+(l+1),"");
												oldconfigeditor.putString(ALLOW+l,val);	
												oldconfigeditor.putString(ALLOW+(l+1),"");
												oldconfigeditor.commit();
											}										
										}
									}
								}
							}else{
								for( int m = 1; m <= MAX_LENGTH; m++){												
									String findVal = oldconfigs.getString(DENY+m, "");	
									if(nextToken.equals(findVal)){
										oldconfigeditor.putString(DENY+m, "");
										oldconfigeditor.commit();
										for(int l = 1; l < MAX_LENGTH; l++ ) {
											if(oldconfigs.getString(DENY+l,"").equals("")) {
												String val = oldconfigs.getString(DENY+(l+1),"");
												oldconfigeditor.putString(DENY+l,val);	
												oldconfigeditor.putString(DENY+(l+1),"");
												oldconfigeditor.commit();
											}										
										}
									}
								}
							}										
						}
						String getValue = mSoftAPCfg.SapSendCommand(getCmdPrefix +"deny_list");
						Log.d(TAG,"GetValue deny_list "+getValue);
						String getValue1 = mSoftAPCfg.SapSendCommand(getCmdPrefix +"allow_list");
						Log.d(TAG,"GetValue allow_list "+getValue1);
					}else {
						if(setValue.contains("\"")){
							Log.d(TAG,"Checking for escape Character "+setKey);
							setValue=setValue.substring(1,setValue.length()-1);
						}
						oldconfigeditor.putString(setKey, setValue);
						oldconfigeditor.commit();	
						defaultEditor.commit();	
					} 
					successCount++;
				} else {
					setValue = oldconfigs.getString(setKey,"");
					defaultEditor.putString(setKey, setValue);
					defaultEditor.commit();					

					unsupportedCount++;
					Log.e(TAG,"Response "+response);
				} 
			}
			if(changedCommad.size()==(successCount+unsupportedCount)){
				response = mSoftAPCfg.SapSendCommand(setCmdPrefix +"commit");	
				commitResponse = response;
				Toast.makeText(context,"Commit "+response,Toast.LENGTH_LONG).show();
				Log.d(TAG,"Response "+response);
				Log.d(TAG, "Commited Changes");	
			}
			preferenceChanged = false;
		}
	}
	public class dialogThr implements Runnable
	{
		int dialogID;
		public Handler mHandler;
		Thread t;
		public dialogThr(int id){
			dialogID=id;
			t=new Thread(this);t.start();
		}

		public void run() {
			Looper.prepare();

			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					// process incoming messages here
				}
			};
			switch(dialogID)
			{
			case DIALOG_OFF: showDialog(DIALOG_OFF);
			break;
			case DIALOG_ON: showDialog(DIALOG_ON);
			break;
			case DIALOG_SAVE: showDialog(DIALOG_SAVE);
			break;
			case DIALOG_RESET: showDialog(DIALOG_RESET);
			break;
			}
			Looper.loop();
		}
	}

	public boolean onPreferenceClick(Preference preference) {
		if (preference == wsPref) {		
			Log.d(TAG, "onPreferenceClick - WirelessSettings");
			Intent i = new Intent(MainMenuSettings.this, WirelessSettings.class);			
			startActivity(i);
		} else if (preference == nsPref) {		
			Log.d(TAG,"onPreferenceClick - NetworkSettings");
			Intent i = new Intent(MainMenuSettings.this, NetworkSettings.class);		
			startActivity(i);
		} else if (preference == statusPref) {		
			Log.d(TAG,"onPreferenceClick - Status");
			Intent i = new Intent(MainMenuSettings.this, StationStatus.class);		
			startActivityForResult(i, 0);
			SSRUNNING = true;
		} 
		return true;
	}	

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		boolean ret = false;		
		if (preference == wifiCheckEnable) {			
			if (wifiCheckEnable.isChecked()) {
				new dialogThr(DIALOG_OFF);
				Log.e("CHANGE", "dialog off");
				Log.d(TAG,"Notification Removed");
				nm.cancel(R.string.notification);			
				Log.d(TAG,"Sending Command "+setCmdPrefix+ENABLE_SOFTAP+"=0");	
				response = mSoftAPCfg.SapSendCommand(setCmdPrefix + ENABLE_SOFTAP+"=0");
				Log.d(TAG, "Received Response ........: " + response);				
				Toast.makeText(this, "Response = " + response, 0).show();
				if(response.equals("success")){					
					wifiCheckEnable.setSummary("Turn On");
					ret = enableFalse();					
				}else {
					ret = enableTrue();
				}				
				if(dialOff!=null) dialOff.cancel();
				
			} else {
				new dialogThr(DIALOG_ON);
				Log.e("CHANGE", "dialog on");
				Log.d(TAG,"Sending Command "+setCmdPrefix+ENABLE_SOFTAP+"=1");				
				response = mSoftAPCfg.SapSendCommand(setCmdPrefix +ENABLE_SOFTAP+"=1");				
				if(response.equals("success")){					
					Log.d(TAG,"Received response "+response);					
					ret = enableTrue();
					Toast.makeText(getApplicationContext(),"response "+response,Toast.LENGTH_LONG).show();
					wifiCheckEnable.setSummary("Turn Off");
				}else {
					Log.d(TAG,"Received response "+response);
					ret = enableFalse();
					Toast.makeText(getApplicationContext(),"response "+response,Toast.LENGTH_LONG).show();
				}
				if(dialOn!=null)dialOn.cancel();
			}		
		}		
		return ret;
	}

	public boolean enableFalse(){
		oldconfigeditor.putString(ENABLE_SOFTAP,"0");
		oldconfigeditor.putBoolean("status", false);
		defaultEditor.putBoolean("status", false);
		defaultEditor.putString(ENABLE_SOFTAP,"0");
		oldconfigeditor.commit();
		defaultEditor.commit();		
		wifiCheckEnable.setChecked(false);
		save.setEnabled(false);
		reset.setEnabled(false);
		killMonitorThread();
		return false;
	}

	public boolean enableTrue(){
		oldconfigeditor.putString(ENABLE_SOFTAP,"1");
		oldconfigeditor.putBoolean("status", true);
		defaultEditor.putBoolean("status", true);
		defaultEditor.putString(ENABLE_SOFTAP,"1");		
		oldconfigeditor.commit();
		defaultEditor.commit();		
		wifiCheckEnable.setChecked(true);
		getDataFromServer();
		reset.setEnabled(true);
		Log.d(TAG,"show Notification" );				
		showNotification();	
		apmonitor = new APEventMonitor(mSoftAPCfg,getApplicationContext());
		return true;
	}

	private void showNotification(String event) {		
		CharSequence text =event;
		Notification notification = new Notification(R.drawable.ap_notify,text, System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,new Intent(this, MainMenuSettings.class), 0);
		notification.setLatestEventInfo(this, text, text, contentIntent);
		nm.notify(EVENT_ID, notification);
		nm.cancel(EVENT_ID);
	}
	private void showNotification() {
		Log.d(TAG,"showNotification() Method");
		CharSequence text = getText(R.string.notification);
		Notification notification = new Notification(R.drawable.ap_notify,text, System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,new Intent(this, MainMenuSettings.class), 0);

		notification.setLatestEventInfo(this, text, text, contentIntent);
		nm.notify(R.string.notification, notification);
	}

	public void onResume(){
		super.onResume();	
		if(preferenceChanged){	
			save.setEnabled(true);
		}
	}
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_OFF: {			
			dialOff = new ProgressDialog(this);                
			dialOff.setMessage("Turning off SoftAP...");			
			dialOff.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialOff.setIndeterminate(true);
			dialOff.setCancelable(true);			
			return dialOff;
		}
		case DIALOG_ON: {			
			dialOn = new ProgressDialog(this);                
			dialOn.setMessage("Turning on SoftAP...");
			dialOn.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialOn.setIndeterminate(true);
			dialOn.setCancelable(true);			
			return dialOn;
		}
		case DIALOG_RESET:{
			dialReset = new ProgressDialog(this);                
			dialReset.setMessage("Resetting SoftAP...");
			dialReset.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialReset.setIndeterminate(true);
			dialReset.setCancelable(true);			
			return dialReset;
		}
		case DIALOG_SAVE:{
			dialSave = new ProgressDialog(this);                
			dialSave.setMessage("Applying Changes to SoftAP...");
			dialSave.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialSave.setIndeterminate(true);
			dialSave.setCancelable(true);			
			return dialSave;
		}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> comparePreferenceFiles(SharedPreferences file1,SharedPreferences file2,String[] internalKeys)
	{
		changes.clear();
		Map<String ,?> map1,map2;
		Set<?> s1,s2;
		Iterator<?> it1,it2;
		Map.Entry Copy=null,Original = null;
		String copyKey;
		map1=file1.getAll();
		map2=file2.getAll();
		//final String[] internalKey = getResources().getStringArray(R.array.internalKeys);

		for( int m = 1; m <= MAX_LENGTH; m++){		
			status = false;
			String copyAllow = file1.getString(ALLOW+m, "");			
			String copyDeny = file1.getString(DENY+m, "");	
			//Log.d(TAG, "Comparing Values : "+copyAllow+" "+copyDeny);
			for( int n = 1; n <= MAX_LENGTH; n++){
				String orignAllow = file2.getString(ALLOW+n, "");
				if(!copyAllow.equals("")){
					if(copyAllow.equals(orignAllow)){						
						status = false;
						break;
					}else {	
						status = true;
					}
				}				
			}
			if(status){
				Log.d(TAG, "Added Allow List :"+copyAllow+":");
				allowAddList += copyAllow+" ";
			}
			status = false;
			for( int n = 1; n <= MAX_LENGTH; n++){
				String orignDeny = file2.getString(DENY+n, "");
				if(!copyDeny.equals("")){
					if(copyDeny.equals(orignDeny)){												
						status = false;
						break;						
					}else
						status = true;
				}				
			}
			if(status){
				Log.d(TAG, "Added deny List : "+copyDeny);
				denyAddList += copyDeny+" ";
			}
		}	
		for( int m = 1; m <= MAX_LENGTH; m++){	
			status = false;
			String copyAllow = file2.getString(ALLOW+m, "");			
			String copyDeny = file2.getString(DENY+m, "");

			for( int n = 1; n <= MAX_LENGTH; n++){
				String orignAllow = file1.getString(ALLOW+n, "");				
				if(!copyAllow.equals("")){					
					if(copyAllow.equals(orignAllow)){											
						status = false;
						break;
					}else
						status = true;
				}				
			}
			if(status){
				Log.d(TAG, "Removed allow List : "+copyAllow);
				allowRemoveList += copyAllow+" ";
			}
			status = false;
			for( int n = 1; n <= MAX_LENGTH; n++){
				String orignDeny = file1.getString(DENY+n, "");
				if(!copyDeny.equals("")){
					if(copyDeny.equals(orignDeny)){						
						status = false;
						break;
					}else
						status = true;
				}				
			}
			if(status){
				Log.d(TAG, "Removed deny List : "+copyDeny);
				denyRemoveList += copyDeny+" ";
			}
		}			
		s1=map1.entrySet();				
		it1=s1.iterator();	
		while(it1.hasNext())
		{
			Copy = (Map.Entry) it1.next();

			s2=map2.entrySet();
			it2=s2.iterator();	
			while(it2.hasNext()){
				Original=(Map.Entry)it2.next();				
				if(Copy.getKey().equals(Original.getKey())){					
					if(!Copy.getValue().equals(Original.getValue())){
						copyKey = Copy.getKey().toString();
						for(int i =0; i < internalKeys.length ; i++){							
							if(!Copy.getKey().equals(internalKeys[i])){
								if(copyKey.contains(ALLOW)){
									Log.d(TAG,copyKey);
									continue;
								}else if(copyKey.contains(DENY)){
									Log.d(TAG,copyKey);
									continue;
								}else{
									if(copyKey.equals("wep_key0")||copyKey.equals("wep_key1")||													
											copyKey.equals("wep_key2")||copyKey.equals("wep_key3")){
										if(Copy.getValue().toString().length()==5
												||Copy.getValue().toString().length()==13){
											changes.add(Copy.getKey()+"="+"\""+Copy.getValue()+"\"");
											Log.d(TAG,"Changed Value "+Copy.getKey()+"="+"\""+Copy.getValue()+"\"");
										} else {
											changes.add(Copy.getKey()+"="+Copy.getValue());		
											Log.d(TAG,"Changed Value "+Copy.getKey()+"="+Copy.getValue());
										}

									}else if (copyKey.equals("country_code")){

										changes.add(Copy.getKey()+"="+Copy.getValue()+"I");		
										Log.d(TAG,"Changed Value "+Copy.getKey()+"="+Copy.getValue()+"I");	


									} else {
										changes.add(Copy.getKey()+"="+Copy.getValue());		
										Log.d(TAG,"Changed Value "+Copy.getKey()+"="+Copy.getValue());
									}
								}
							}
							break;
						}
					} 
				}
			}
		}
		if(!allowRemoveList.equals("")){
			changes.add("remove_from_allow_list="+allowRemoveList.substring(0,allowRemoveList.length()-1));
		}
		if(!denyRemoveList.equals("")){
			changes.add("remove_from_deny_list="+denyRemoveList.substring(0,denyRemoveList.length()-1));
		}
		if(!allowAddList.equals("")){
			changes.add("add_to_allow_list="+allowAddList.substring(0,allowAddList.length()-1));
		}		
		if(!denyAddList.equals("")){
			changes.add("add_to_deny_list="+denyAddList.substring(0,denyAddList.length()-1));
		}if(!port_filter.equals("")){
			changes.add("port_filter="+port_filter.substring(0,port_filter.length()-1));
		}if(!port_forward.equals("")){
			changes.add("port_forward="+port_forward.substring(0,port_forward.length()-1));
		}
		Log.d(TAG,"ChangedArray Content"+changes);
		allowRemoveList="";
		allowAddList="";
		denyRemoveList="";
		denyAddList="";
		return changes;
	}

	private void SetOriginalValues(){
		Map<String, ?> allValues = new HashMap<String, String>();
		oldconfigs = getSharedPreferences("oldconfig", MODE_WORLD_WRITEABLE);
		allValues = oldconfigs.getAll();
		Set<?> s = allValues.entrySet();
		Iterator<?> it = s.iterator();
		while (it.hasNext()) {
			Map.Entry m = (Map.Entry) it.next();
			String key = (String) m.getKey();
			String value = null;
			boolean b = false;
			if (m.getValue() instanceof String) {
				value = (String) m.getValue();
				defaultEditor.putString(key, value);
			} else {
				boolean wificheckVal = Boolean.parseBoolean(m.getValue()
						.toString());
				defaultEditor.putBoolean(key, wificheckVal);
			}
		}
		defaultEditor.commit();
		Log.d(TAG,"Commiting not Success");
		preferenceChanged = false;		
	}

	private void SetDefaultValues(String keyValue) {
		String value = KeyVal.substring(index+1,KeyVal.length());			
		defaultEditor.putString(keyValue, value);
		oldconfigeditor.putString(keyValue, value);

		defaultEditor.commit();
		oldconfigeditor.commit();
		Log.d(TAG,"Value "+value);
	}

	private void GetAllowDenyValue(String KeyValue){
		index = KeyVal.indexOf("=");
		String value = KeyVal.substring(index+1,KeyVal.length());
		Log.d(TAG,"value "+value);
		int KeyCheck=KeyValue.indexOf("_");
		if(!value.trim().equals("")){
			StringTokenizer st = new StringTokenizer(value);
			int i = 1;
			while(st.hasMoreElements()) {
				String nextToken = st.nextToken();
				defaultEditor.putString(KeyValue.substring(0,KeyCheck)+i,nextToken);
				oldconfigeditor.putString(KeyValue.substring(0,KeyCheck)+i,nextToken);
				defaultEditor.commit();
				oldconfigeditor.commit();
				i++;
			}
			if(i<MAX_LENGTH){
				for(int j = i;j<=MAX_LENGTH;j++){					
					defaultEditor.putString(KeyValue.substring(0,KeyCheck)+j,"");
					oldconfigeditor.putString(KeyValue.substring(0,KeyCheck)+j,"");
					defaultEditor.commit();
					oldconfigeditor.commit();
				}
			}
		} else {
			for(int i = 1;i<=MAX_LENGTH;i++){				
				defaultEditor.putString(KeyValue.substring(0,KeyCheck)+i,"");
				oldconfigeditor.putString(KeyValue.substring(0,KeyCheck)+i,"");
				defaultEditor.commit();
				oldconfigeditor.commit();
			}
		} 
	}
	public void killMonitorThread(){
		if(apmonitor!=null){
		apmonitor.KILLED=true;		
		}
	}

	public void onDestroy()
	{
		super.onDestroy();
		killMonitorThread();		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch(requestCode) {
		case 0:			
			if (resultCode == RESULT_OK) {
				SSRUNNING = false;
				break;
			}
		}
	}  
}
