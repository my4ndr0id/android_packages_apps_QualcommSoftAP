package com.qualcomm.wifi.softap.ws;

import java.util.ArrayList;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.wifi.softap.MainMenuSettings;
import com.qualcomm.wifi.softap.QWiFiSoftApCfg;

import com.qualcomm.wifi.softap.R;

public class BasicWirelessSettings extends PreferenceActivity implements OnPreferenceChangeListener {
	private SharedPreferences preferences,oldconfigs;
	private timer timr;
	private final int MINUTE=2*60*1000;
	private final int DIALOG_WPS=4; 	
	public static ProgressDialog dialWPS;
	private ListPreference  networkLst, freqLst, authLst,wpsEnrollLst;
	private CheckBoxPreference broadChk,configLst;
	private EditTextPreference ssidEdit;	
	private PreferenceCategory cat_manual;
	private static final String TAG = "QCSOFTAP_GUI_BWS";	
	private boolean enable = true;
	private String ssidVal,response,wpsKey;	
	private EditText newPin;
	private String[] keys;	
	private static boolean prefChStatus;
	private static String pinValue=null;
	private Intent intent;
	private static final String NULL = "";
	private SharedPreferences.Editor defEditor,oldconfigeditor;
	private ArrayList<Preference> prefLst;	
	public QWiFiSoftApCfg mSoftAPCfg;

	private String[] internalKey;
	public static MainMenuSettings mms;

	private Builder wpsDialog;
	public static AlertDialog wpsAlertDialog;	
	private TextWatcher pinWatcher;

	private static String PIN_PATTERN = "^[0-9]{8,32}$";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.basic_wireless_settings);
		mms = new MainMenuSettings();		
		mSoftAPCfg=MainMenuSettings.mSoftAPCfg;
		internalKey = getResources().getStringArray(R.array.internalKeys);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		oldconfigs = getSharedPreferences("oldconfig", MODE_PRIVATE);
		defEditor = preferences.edit();	
		oldconfigeditor = oldconfigs.edit();
		configLst = (CheckBoxPreference) findPreference("wpsstate");
		configLst.setOnPreferenceChangeListener(this);
		networkLst = (ListPreference) findPreference("hw_mode");		
		freqLst = (ListPreference) findPreference("channel");
		authLst = (ListPreference) findPreference("auth_algs");		
		broadChk = (CheckBoxPreference) findPreference("broadcast_ssid");	
		broadChk.setOnPreferenceChangeListener(this);
		wpsEnrollLst = (ListPreference) findPreference("config_methods");
		wpsEnrollLst.setOnPreferenceChangeListener(this);
		wpsEnrollLst.setSummary("Select WPS Enroll Method");

		ssidEdit = (EditTextPreference) findPreference("ssid");		
		cat_manual = (PreferenceCategory) findPreference("cat_manual");		
		keys = getResources().getStringArray(R.array.bws_keys);

		prefLst = new ArrayList<Preference>();
		prefLst.add(networkLst); prefLst.add(ssidEdit);
		prefLst.add(freqLst); prefLst.add(authLst); 	
		

		String wpsChk = preferences.getString("wps_state", NULL);		
		if(wpsChk.equals("1")){
			configLst.setChecked(true);	
			wpsEnrollLst.setEnabled(true);
		}else{
			configLst.setChecked(false);
			wpsEnrollLst.setEnabled(false);
		}
		String sbroadChk = preferences.getString("ignore_broadcast_ssid", NULL);		
		if(sbroadChk.equals("1")){
			broadChk.setChecked(false);	
		}else
			broadChk.setChecked(true);	

		int keyCt = 0;
		for(Preference pref : prefLst){					
			pref.setOnPreferenceChangeListener(this);
			String getMode = preferences.getString(keys[keyCt], NULL);

			if(pref instanceof ListPreference){
				ListPreference lstPref = (ListPreference)pref;
				pref.setSummary(lstPref.getEntry());

				if (!getMode.equals(NULL)){					
					if (getMode.equals("0")) {
						cat_manual.setEnabled(true);

						String autoChannel = preferences.getString("autoChannel", NULL);
						if(!autoChannel.equals(NULL)){
							pref.setSummary(lstPref.getEntry() + "-Current Channel "+autoChannel);
						} else {
							pref.setSummary(lstPref.getEntry());
						}
					}else if(keys[keyCt].equals("channel"))
						pref.setSummary(getMode);
				}				
			} else if(pref instanceof EditTextPreference){
				Log.d(TAG,"SSID VALUE "+ getMode);
				pref.setSummary(getMode);
			}		
			keyCt++;
		}		

		final EditText editText = ssidEdit.getEditText();		
		editText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {					
				ssidVal = editText.getText().toString();
				if(ssidVal.equals("") ){
					editText.setError("Can not be Null");
				} 
				return false;
			}			
		});	
		ssidEdit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {			
			public boolean onPreferenceClick(Preference preference) {
				ssidEdit.getEditText().setError(null);
				return true;
			}
		});			
	}

	public boolean onPreferenceChange(final Preference preference, Object newValue) {	
		prefChStatus = true;
		String newChangedVal = newValue.toString();
		Log.d(TAG, "New Changed Value : "+newValue);
		if(preference instanceof ListPreference){
			ListPreference lstPref = (ListPreference) preference;
			int index = lstPref.findIndexOfValue((String) newValue);
			String lstEntry = (String) lstPref.getEntries()[index];			
			String lstVal = (String) lstPref.getEntryValues()[index];
			
			if(preference == wpsEnrollLst){
				lstPref.setSummary("Select WPS Enroll Method");
			}else {
				lstPref.setSummary(lstEntry);
			}
			if(preference == wpsEnrollLst){				
				if(newValue.equals("1")){
					TextView view;
					LayoutInflater factory = LayoutInflater.from(BasicWirelessSettings.this);
					final View textEntryView = factory.inflate(R.layout.alert_dialog_layout, null);				
					newPin = (EditText)textEntryView.findViewById(R.id.editText);
					view = (TextView) textEntryView.findViewById(R.id.txt_view);
					view.setText("Must be 8 to 32 digits");		
					newPin.setHint("Enter PIN");					

					pinWatcher = new TextWatcher() {						
						public void onTextChanged(CharSequence s, int start, int before, int count) {
							// TODO Auto-generated method stub							
						}						
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {
							// TODO Auto-generated method stub							
						}						
						public void afterTextChanged(Editable s) {
							String val = s.toString();
							if(!val.equals("")){
								if(!val.matches(PIN_PATTERN)){
									Log.d(TAG, "PIN Validation false Part");
									newPin.setError("Min 8 Digits");
								}else{
									Log.d(TAG, "PIN Validation True Part");
									newPin.setError(null);	
								}	
							}else
								newPin.setError("Can not be null");
						}
					};
					newPin.addTextChangedListener(pinWatcher);

					new AlertDialog.Builder(BasicWirelessSettings.this)				                
					.setTitle("Enter WPS PIN")
					.setView(textEntryView)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {						
							pinValue = newPin.getText().toString();
							if(!pinValue.equals("")){
								if(pinValue.matches(PIN_PATTERN)){	
									Log.d(TAG,"Sending Command "+MainMenuSettings.setCmdPrefix +"config_methods=1 "+pinValue);

									response = mSoftAPCfg.SapSendCommand(MainMenuSettings.setCmdPrefix +"config_methods=1 "+pinValue);				

									Log.d(TAG,"Response "+response);
									response = "success";
									if(response.contains("success")){
										if(timr!=null) timr.cancel();
										timr=new timer(MINUTE,1000);
										timr.start();
										wpsKey = pinValue.toString();						
										UpdateChanges(preferences,oldconfigs,preference.getKey());
										StartWpsPopUp("PIN ENTRY",wpsKey);

										Log.d(TAG,"Response From Config_methods Success Reply "+response);
									} else {
										wpsKey = oldconfigs.getString("wpsKey", "");								 

										UpdateChanges(oldconfigs,preferences,preference.getKey());
										prefChStatus=false;
										Log.d(TAG,"Response From COnfig_methods UnSuccess Reply "+response);
									}
									
								}
								else {
									Toast.makeText(getApplicationContext(), "Invalid PIN ", 0).show();								
								}								
							}else {
								Toast.makeText(getApplicationContext(), "Can not be Null ", 0).show();
							}	
						}				
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).create().show();		
					//return true;
				} else if(newValue.equals("0")){
					Log.d(TAG,"Sending Command "+MainMenuSettings.setCmdPrefix +preference.getKey()+"="+newValue);
					if(mSoftAPCfg!=null)
					response = mSoftAPCfg.SapSendCommand(MainMenuSettings.setCmdPrefix +preference.getKey()+"="+newValue);
					Log.d(TAG,"Response "+response);
					if(response.contains("success")){
						if(timr!=null) timr.cancel();
						timr=new timer(MINUTE,1000);
						timr.start();
						UpdateChanges(preferences,oldconfigs,preference.getKey());						
						StartWpsPopUp("PUSH BUTTON",null);
						wpsAlertDialog.show();	
					} else {
						UpdateChanges(oldconfigs,preferences,preference.getKey());						
					}
				}
				
				prefChStatus = false;
			} else if (preference == freqLst) {									
				if(newChangedVal.equals("12") || newChangedVal.equals("13") 
						|| newChangedVal.equals("14")){				     
					defEditor.putString("hw_mode","b");
					defEditor.commit();					 
					intent = new Intent(getApplicationContext(), BasicWirelessSettings.class);
					startActivity(intent);
					finish();
				}
				if(newChangedVal.equals("0")) {
					String autoChannel = preferences.getString("autoChannel", NULL);
					if(!autoChannel.equals(NULL)){
						freqLst.setSummary(lstEntry + "-Current Channel "+autoChannel);
					} else{ 
						freqLst.setSummary(lstEntry);
					}
				}else {
					freqLst.setSummary(newChangedVal);
				}	
				MainMenuSettings.preferenceChanged = true;
			}else if(preference == networkLst){
				defEditor.putString("basic_rates", "");				
				defEditor.putString("basic_rates_pos", "");
				defEditor.putString("supported_rates", "");
				defEditor.putString("supported_rates_pos", "");
				defEditor.commit();
				MainMenuSettings.preferenceChanged = true;
			}
		}else if(preference instanceof EditTextPreference){
			EditTextPreference editPref = (EditTextPreference) preference;
			String newEditVal = newValue.toString();	
			if((preference == ssidEdit) ){					
				if(!newChangedVal.equals(NULL)){				
					editPref.setSummary(newEditVal);					
					MainMenuSettings.preferenceChanged = true;										
				}else{
					Toast.makeText(this, "Value can not be Null", 0).show();
					prefChStatus = false;
				}			
			}
		}else if(preference == broadChk){			
			if(!broadChk.isChecked()){
				defEditor.putString("ignore_broadcast_ssid", "0");	
				defEditor.commit();
			}else {
				defEditor.putString("ignore_broadcast_ssid", "1");	
				defEditor.commit();
			}
			MainMenuSettings.preferenceChanged = true;
		} else if(preference == configLst){
			if(!configLst.isChecked()){
				Log.d(TAG,"isChecked() ");
				new dialogThr(DIALOG_WPS);
				defEditor.putString("wps_state", "1");	
				defEditor.commit();
				MainMenuSettings.preferenceChanged = true;

				mms.saveChanges(preferences,oldconfigs,internalKey,getApplicationContext());
				if(dialWPS!=null)dialWPS.cancel();
				if(MainMenuSettings.wpaResponse != null){
					if(MainMenuSettings.wpaResponse.contains("success")){
						if(MainMenuSettings.commitResponse.contains("success")){
							wpsEnrollLst.setEnabled(true);
						}else{
							prefChStatus = SettingOldValue();
						}
					}else{
						prefChStatus = SettingOldValue();
					}
				} else {
					Log.d(TAG,"wpaResponse Null");
					configLst.setChecked(false);
				}
			}else {
				Log.d(TAG,"Not isChecked() ");

				response = mSoftAPCfg.SapSendCommand(MainMenuSettings.setCmdPrefix +"wps_state="+"0");					
				Log.d(TAG,"Response "+response);
				response = mSoftAPCfg.SapSendCommand(MainMenuSettings.setCmdPrefix +"commit");
				if(response.equals("success")){
					defEditor.putString("wps_state", "0");	
					oldconfigeditor.putString("wps_state", "0");
					defEditor.commit();
					oldconfigeditor.commit();
					wpsEnrollLst.setEnabled(false);
					if(timr!=null){
						timr.cancel();
					}
					Log.d(TAG,"wpsEnrollLst false");
				} else {
					defEditor.putString("wps_state", "1");	
					oldconfigeditor.putString("wps_state", "1");
					defEditor.commit();
					oldconfigeditor.commit();
					wpsEnrollLst.setEnabled(true);
					Log.d(TAG,"wpsEnrollLst True");
				}
				MainMenuSettings.preferenceChanged = true;
			}
		} 
		
		return prefChStatus;
	}	

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_WPS: 			
			dialWPS = new ProgressDialog(this);                
			dialWPS.setMessage("Applying Changes to softAP...");			
			dialWPS.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialWPS.setIndeterminate(true);
			dialWPS.setCancelable(true);			
			return dialWPS;
		}
		return null;
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
			case DIALOG_WPS: showDialog(DIALOG_WPS);
			break;
			}
			Looper.loop();
		}
	}

	public void onResume()
	{
		super.onResume();
		
	}
	public boolean SettingOldValue(){
		Log.d(TAG,"Called ");
		preferences.edit().putString("wps_state", "0");
		oldconfigs.edit().putString("wps_state", "0");
		preferences.edit().commit();
		oldconfigs.edit().commit();		
		return false;
	}

	private class timer extends CountDownTimer
	{		
		int counter=0;
		public timer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			
		}		
		
		@Override
		public void onFinish() {
			if(wpsDialog!=null){
				wpsAlertDialog.dismiss();	
			}
			Log.e(TAG,"wait timer finished");					
			defEditor.putString("config_methods", "");
			defEditor.commit();
			intent = new Intent(getApplicationContext(), BasicWirelessSettings.class);
			startActivity(intent);
			finish();
			
		}
		@Override
		public void onTick(long millisUntilFinished) {
			
			Log.d(TAG,"Timer is running count:"+(++counter));
		}
	}
	public void UpdateChanges(SharedPreferences sour,SharedPreferences dest,String key){
		String configKey = sour.getString("config_methods","");
		Log.d(TAG,"configKey "+configKey);		
		SharedPreferences.Editor edit = dest.edit();
		edit.putString("config_methods", configKey);
		if(key.equals("wpsKey")){
			edit.putString("wpsKey", wpsKey);
		}
		edit.commit();
	}
	public void StartWpsPopUp(String Enroll,String pin){
		TextView view;
		LayoutInflater factory = LayoutInflater.from(BasicWirelessSettings.this);
		final View textEntryView = factory.inflate(R.layout.alert_message, null);						
		view = (TextView) textEntryView.findViewById(R.id.message_view);
		if(Enroll.equals("PIN ENTRY")){
			view.setText("Enter PIN number "+pin+" on the client to connect");	
		} else {
			view.setText("Press the Push button on the client to connect");	
		}

		wpsDialog=new AlertDialog.Builder(BasicWirelessSettings.this);				                
		wpsDialog.setTitle("WPS Session is On");
		wpsDialog.setView(textEntryView);

		wpsAlertDialog = wpsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}				
		}).create();
		wpsAlertDialog.show();
	}
	public void onDestroy()
	{
		super.onDestroy();
		
		if(wpsDialog!=null)	{
			wpsAlertDialog.dismiss();	
		}
		if(timr!=null){
			timr.cancel();
		}
		defEditor.putString("config_methods", "");
		defEditor.commit();
	}
}
