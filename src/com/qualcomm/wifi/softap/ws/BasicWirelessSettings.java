/*
 * Copyright (c) 2010, Code Aurora Forum. All rights reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *  * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *  * Neither the name of Code Aurora Forum, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


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
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
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

import com.qualcomm.wifi.softap.L10NConstants;
import com.qualcomm.wifi.softap.MainMenuSettings;
import com.qualcomm.wifi.softap.QWiFiSoftApCfg;

import com.qualcomm.wifi.softap.R;

/**
 * This class implements basic wireless configuration settings<br> 
 * which includes network/ssid/channel/auth_mode settings<br>
 */
public class BasicWirelessSettings extends PreferenceActivity implements OnPreferenceChangeListener {

	private String ssidVal,response,wpsKey;
	private String[] keys;	
	private static boolean prefChStatus;
	private static String pinValue = null, regulatoryDomain ;		
	private String[] freqArray, freqArrayValues;
	private static String NW_MODE_WARNING;
	private boolean bNModeVerifyCheck;

	private SharedPreferences defSharPref, orgSharPref;
	private SharedPreferences.Editor defPrefEditor, orgPrefEditor;

	public static ProgressDialog dialWPS;
	private ListPreference  networkLst, freqLst, authLst, wpsEnrollLst;
	private CheckBoxPreference broadChk, configLst;
	private EditTextPreference ssidEdit;	

	private EditText newPin;	
	private Intent intent;

	private ArrayList<Preference> prefLst;	
	public QWiFiSoftApCfg mSoftAPCfg;
	private timer timr;

	public static MainMenuSettings mainMenuStngs;
	private Builder wpsDialog;
	public static AlertDialog wpsAlertDialog;	
	private TextWatcher pinWatcher;

	private String sRsn;
	private String sWpa;
	private String sSM;

	/**
	 * This method initialize the default values for the UI <br>
	 * objects in parallel to inflating the same on the screen
	 * 
	 * @param savedInstanceState If the activity is being re-initialized after previously being shut down 
	 * then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.basic_wireless_settings);
		mainMenuStngs = new MainMenuSettings();
		mSoftAPCfg = MainMenuSettings.mSoftAPCfg;	

		//Edit default and orgSharPref preference
		defSharPref = PreferenceManager.getDefaultSharedPreferences(this);
		orgSharPref = getSharedPreferences(L10NConstants.CONFIG_FILE_NAME, MODE_PRIVATE);
		defPrefEditor = defSharPref.edit();
		orgPrefEditor = orgSharPref.edit();

		//Get the reference to the preferences defined in the UI
		configLst = (CheckBoxPreference) findPreference("wpsstate");		
		networkLst = (ListPreference) findPreference(L10NConstants.HW_MODE_KEY);		
		freqLst = (ListPreference) findPreference(L10NConstants.CHNL_KEY);
		authLst = (ListPreference) findPreference(L10NConstants.AUTH_MODE_KEY);
		broadChk = (CheckBoxPreference) findPreference("broadcast_ssid");	
		wpsEnrollLst = (ListPreference) findPreference(L10NConstants.CONFIG_KEY);
		wpsEnrollLst.setSummary(getString(R.string.str_bws_enroll));
		ssidEdit = (EditTextPreference) findPreference("ssid");				
		keys = getResources().getStringArray(R.array.bws_keys);

		//set preference change listener to SSID Broadcast,WPS, WPS Enroll Method
		wpsEnrollLst.setOnPreferenceChangeListener(this);
		broadChk.setOnPreferenceChangeListener(this);
		configLst.setOnPreferenceChangeListener(this);

		prefLst = new ArrayList<Preference>();
		prefLst.add(networkLst); 
		prefLst.add(ssidEdit);
		prefLst.add(freqLst); 
		prefLst.add(authLst);  	

		try{
			//set WPS enroll method 'true' based on WPS check value in the preference
			String wpsChk = defSharPref.getString(L10NConstants.WPS_KEY, "");
			if(wpsChk.equals(L10NConstants.VAL_ONE)){
				configLst.setChecked(true);				
			}else{
				configLst.setChecked(false);			
			}
			//set SSID broadcast check 'true' based on SSID Broadcast value in the preference
			String sbroadChk = defSharPref.getString(L10NConstants.IGNORE_BROAD_SSID_KEY, "");		
			if(sbroadChk.equals(L10NConstants.VAL_ONE)){
				broadChk.setChecked(false);	
			}else
				broadChk.setChecked(true);	

			String countryCode = defSharPref.getString(L10NConstants.COUNTRY_KEY, "");
			String cCode = countryCode; //added newly
			if(!countryCode.equals("")){
				// Extract country code value(eg:US) and regulatory domain(eg: REGDOMAIN_FCC) from pref file((eg: US,REGDOMAIN_FCC)
				if(countryCode.contains(",")){
					countryCode = countryCode.substring(0,countryCode.indexOf(","));					
					regulatoryDomain = cCode.substring(cCode.indexOf(",")+1,cCode.length());					
				} 					
				//Set the channel freqency based on the regulatory domain associated with the country code
				//eg. US->Channel's=11, JP->Channel's=14
				if(regulatoryDomain.equals("REGDOMAIN_FCC")||regulatoryDomain.equals("REGDOMAIN_WORLD")
						||regulatoryDomain.equals("REGDOMAIN_N_AMER_EXC_FCC")){					
					freqArray = getResources().getStringArray(R.array.freqeleven);
					freqArrayValues = getResources().getStringArray(R.array.freqelevenValues);
				} else if(regulatoryDomain.equals("REGDOMAIN_ETSI")||regulatoryDomain.equals("REGDOMAIN_APAC")
						||regulatoryDomain.equals("REGDOMAIN_KOREA")||regulatoryDomain.equals("REGDOMAIN_HI_5GHZ")
						||regulatoryDomain.equals("REGDOMAIN_NO_5GHZ")){
					freqArray = getResources().getStringArray(R.array.freqthirteen);
					freqArrayValues = getResources().getStringArray(R.array.freqthirteenValues);					
				} else if(regulatoryDomain.equals("REGDOMAIN_JAPAN")){
					freqArray = getResources().getStringArray(R.array.freqfourteen);
					freqArrayValues = getResources().getStringArray(R.array.freqfourteenValues);
				}
				freqLst.setEntries(freqArray);					
				freqLst.setEntryValues(freqArrayValues);
			}	
			//set the default value for Network mode, SSID, Channel, authentication mode
			Log.d(L10NConstants.TAG_BWS, "Channel After the change "+defSharPref.getString(L10NConstants.CHNL_KEY, L10NConstants.VAL_ZERO));
			int keyCt = 0;
			for(Preference pref : prefLst){					
				pref.setOnPreferenceChangeListener(this);
				String getMode = defSharPref.getString(keys[keyCt], L10NConstants.VAL_ZERO);

				if(pref instanceof ListPreference){
					ListPreference lstPref = (ListPreference)pref;
					lstPref.setSummary(lstPref.getEntry());	
					if (!getMode.equals("")){					
						if (getMode.equals(L10NConstants.VAL_ZERO)) {
							String autoChannel = defSharPref.getString("autoChannel", "");
							if(!autoChannel.equals("")){
								pref.setSummary(lstPref.getEntry() + "-Current Channel "+autoChannel);
							} else {
								pref.setSummary(lstPref.getEntry());
							}
						}else if(keys[keyCt].equals(L10NConstants.CHNL_KEY))
							pref.setSummary(getMode);
					}				
				} else if(pref instanceof EditTextPreference){					
					pref.setSummary(getMode);
				}		
				keyCt++;
			}		
		}catch(Exception e){
			Log.d(L10NConstants.TAG_BWS, "Unknown Exception...");			
		}
		//ssid editText is validated not to allow empty string
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

	/**
	 * This method implements change event handler in Basic wireless setting
	 * 
	 * @param preference The changed Preference.
	 * @param newValue The new value of the Preference.
	 * 
	 * @return boolean True to update the state of the Preference with the new value.
	 */
	public boolean onPreferenceChange(final Preference preference, Object newValue) {	
		prefChStatus = true;		
		Log.d(L10NConstants.TAG_BWS, "New Changed Value : "+newValue);
		if(preference instanceof ListPreference){
			ListPreference lstPref = (ListPreference) preference;
			int index = lstPref.findIndexOfValue((String) newValue);
			String lstEntry = (String) lstPref.getEntries()[index];	

			if(preference == wpsEnrollLst){
				lstPref.setSummary(getString(R.string.str_bws_enroll));
			}else if(preference  != networkLst){
				lstPref.setSummary(lstEntry);
			}
			if(preference == wpsEnrollLst){				
				if(newValue.equals(L10NConstants.VAL_ONE)){
					TextView view;
					// Inflate EditText and TextView on the Dialog box for WPS Pin
					LayoutInflater factory = LayoutInflater.from(BasicWirelessSettings.this);
					final View textEntryView = factory.inflate(R.layout.alert_dialog_layout, null);				
					newPin = (EditText)textEntryView.findViewById(R.id.editText);
					view = (TextView) textEntryView.findViewById(R.id.txt_view);
					view.setText("Must be 8 to 32 digits");		
					newPin.setHint("Enter PIN");

					// Handle text entry for WPS pin
					pinWatcher = new TextWatcher() {						
						public void onTextChanged(CharSequence s, int start, int before, int count) {
							//do nothing
						}						
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {
							//do nothing
						}						
						public void afterTextChanged(Editable s) {
							String val = s.toString();
							if(!val.equals("")){
								if(!val.matches(L10NConstants.PIN_PATTERN)){									
									newPin.setError("Min 8 Digits");
								}else{									
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
					.setPositiveButton(getString(R.string.alert_dialog_rename_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {						
							pinValue = newPin.getText().toString();
							if(!pinValue.equals("")){
								if(pinValue.matches(L10NConstants.PIN_PATTERN)){	
									Log.d(L10NConstants.TAG_BWS,"Sending Command "+ L10NConstants.SET_CMD_PREFIX +"config_methods=1 "+pinValue);
									response = mSoftAPCfg.SapSendCommand(L10NConstants.SET_CMD_PREFIX +"config_methods=1 "+pinValue);
									Log.d(L10NConstants.TAG_BWS,"Response "+response);
									response = L10NConstants.SUCCESS;
									if(response.contains(L10NConstants.SUCCESS)){
										//start counter 
										startTimer();				
										UpdateChanges(defSharPref,orgSharPref,preference.getKey());
										showWpsDialog("PIN ENTRY", pinValue.toString());
										Log.d(L10NConstants.TAG_BWS,"Response From Config_methods Success Reply "+response);
									} else {
										wpsKey = orgSharPref.getString("wpsKey", "");
										UpdateChanges(orgSharPref,defSharPref,preference.getKey());
										prefChStatus=false;
										Log.d(L10NConstants.TAG_BWS,"Response From COnfig_methods UnSuccess Reply "+response);
									}
								} else {
									Toast.makeText(getApplicationContext(), "Invalid PIN ", 1).show();								
								}								
							} else {
								Toast.makeText(getApplicationContext(), "Can not be Null ", 1).show();
							}	
						}				
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							//do nothing
						}
					}).create().show();					
				} else if(newValue.equals(L10NConstants.VAL_ZERO)){
					Log.d(L10NConstants.TAG_BWS,"Sending Command "+L10NConstants.SET_CMD_PREFIX +preference.getKey()+"="+newValue);
					if(mSoftAPCfg!=null)
						response = mSoftAPCfg.SapSendCommand(L10NConstants.SET_CMD_PREFIX +preference.getKey()+"="+newValue);
					Log.d(L10NConstants.TAG_BWS,"Response "+response);
					if(response.contains(L10NConstants.SUCCESS)){						
						//start counter 
						startTimer();
						UpdateChanges(defSharPref, orgSharPref, preference.getKey());						
						showWpsDialog("PUSH BUTTON",null);
						wpsAlertDialog.show();	
					} else {
						UpdateChanges(orgSharPref, defSharPref, preference.getKey());						
					}
				}
				prefChStatus = false;
			} else if (preference == freqLst) {	
				String channelCheck = defSharPref.getString(L10NConstants.CHNL_KEY, "");				
				if(!channelCheck.equals(newValue)){
					MainMenuSettings.preferenceChanged = true;	
				} 
				//if channel selected is 12,13 or 14 then set network_mode=b
				defPrefEditor.putString(L10NConstants.CHNL_KEY,newValue.toString());
				defPrefEditor.commit();
				if(newValue.equals("12") || newValue.equals("13") 
						|| newValue.equals("14")){				     
					defPrefEditor.putString(L10NConstants.HW_MODE_KEY,"b");
					defPrefEditor.commit();					 
					intent = new Intent(getApplicationContext(), BasicWirelessSettings.class);
					startActivity(intent);
					finish();
				}
				if(newValue.equals(L10NConstants.VAL_ZERO)) {
					String autoChannel = defSharPref.getString("autoChannel", "");
					if(!autoChannel.equals("")) {
						freqLst.setSummary(lstEntry + "-Current Channel "+autoChannel);
					} else { 
						freqLst.setSummary(lstEntry);
					}
				} else {
					freqLst.setSummary(newValue.toString());
				}	

			}else if(preference == networkLst) {
				String sDataRate = defSharPref.getString(L10NConstants.DATA_RATE_KEY, "");				
				String[] dr = null;		

				if(newValue.equals(L10NConstants.SM_B)) {					
					dr = getResources().getStringArray(R.array.dataRatesValuesB);													
				}else if((newValue.equals(L10NConstants.SM_G_ONLY)) || (newValue.equals(L10NConstants.SM_G))) {					
					dr = getResources().getStringArray(R.array.dataRatesValuesGBG);		
				}else if((newValue.equals(L10NConstants.SM_N_ONLY)) || (newValue.equals(L10NConstants.SM_N))) {					
					dr = getResources().getStringArray(R.array.dataRatesValuesNBGN);
				}
				if(!isAvailable(dr, sDataRate)) {					
					defPrefEditor.putString("data_rate", L10NConstants.VAL_ZERO);
				}

				sRsn = defSharPref.getString(L10NConstants.RSN_PAIR_KEY, "");
				sWpa = defSharPref.getString(L10NConstants.WPA_PAIR_KEY, "");
				sSM = defSharPref.getString(L10NConstants.SEC_MODE_KEY, "");

				if(newValue.equals(L10NConstants.SM_N_ONLY)) {	
					if(sSM.equals(L10NConstants.VAL_ONE)) {						
						NW_MODE_WARNING = getString(R.string.bws_screen_alert_N_WEP) +" "+
						getString(R.string.common_append_alert_wep);
						bNModeVerifyCheck=true;
						verifyNModeAlgorithm(NW_MODE_WARNING, lstPref, lstEntry);					
					} else{
						NW_MODE_WARNING = getString(R.string.bws_screen_alert_N_TKIP) +" "+
						getString(R.string.common_append_alert_wpa);
						bNModeVerifyCheck=false;
						verifyNModeAlgorithm(NW_MODE_WARNING, lstPref, lstEntry);
					}					
				}else if(newValue.equals(L10NConstants.SM_N)) {
					if(sSM.equals(L10NConstants.VAL_ONE)){						
						NW_MODE_WARNING = getString(R.string.bws_screen_alert_BGN_WEP) +" "+
						getString(R.string.common_append_alert_wep);
						bNModeVerifyCheck=true;
						verifyNModeAlgorithm(NW_MODE_WARNING, lstPref, lstEntry);
					} else {						
						NW_MODE_WARNING = getString(R.string.bws_screen_alert_BGN_TKIP) +" "+
						getString(R.string.common_append_alert_wpa);
						bNModeVerifyCheck=false;
						verifyNModeAlgorithm(NW_MODE_WARNING, lstPref, lstEntry);
					}
				} 	
				else
					lstPref.setSummary(lstEntry);
			}
			if(!prefChStatus == false) {
				if(preference == networkLst){
					String networkCheck = defSharPref.getString(L10NConstants.HW_MODE_KEY, "");				
					if(!networkCheck.equals(newValue)) {
						MainMenuSettings.preferenceChanged = true;	
					} 
				} else if(preference == authLst) {
					String authCheck = defSharPref.getString(L10NConstants.AUTH_MODE_KEY, "");				
					if(!authCheck.equals(newValue)) {
						MainMenuSettings.preferenceChanged = true;	
					} 
				}
			}
		}else if(preference instanceof EditTextPreference) {
			EditTextPreference editPref = (EditTextPreference) preference;				
			if((preference == ssidEdit) ) {
				if(!newValue.equals("")) {				
					editPref.setSummary(newValue.toString());					
					MainMenuSettings.preferenceChanged = true;										
				} else {
					Toast.makeText(this, "Value can not be Null", 1).show();
					prefChStatus = false;
				}			
			}
		} else if(preference == broadChk) {			
			if(!broadChk.isChecked()) {
				defPrefEditor.putString(L10NConstants.IGNORE_BROAD_SSID_KEY, L10NConstants.VAL_ZERO);
			} else {
				defPrefEditor.putString(L10NConstants.IGNORE_BROAD_SSID_KEY, L10NConstants.VAL_ONE);				
			}
			defPrefEditor.commit();
			MainMenuSettings.preferenceChanged = true;
		} else if(preference == configLst){			
			if(!configLst.isChecked()){				
				new DialogThrd(L10NConstants.DIALOG_WPS);
				defPrefEditor.putString(L10NConstants.WPS_KEY, L10NConstants.VAL_ONE);	
				defPrefEditor.commit();
				mainMenuStngs.saveChanges(defSharPref, orgSharPref, "BWS");
				if(dialWPS!=null)dialWPS.cancel();
				if(MainMenuSettings.sWpsResponse != null){
					if(MainMenuSettings.sWpsResponse.contains(L10NConstants.SUCCESS)){
						if(MainMenuSettings.sCommitResponse.contains(L10NConstants.SUCCESS)){
							configLst.setChecked(true);							
						}else{
							prefChStatus = setPrevValue();
						}
					}else{
						prefChStatus = setPrevValue();
					}
				} else {				
					configLst.setChecked(false);
				}
				MainMenuSettings.saveBtn.setEnabled(false);
			}else {
				response = mSoftAPCfg.SapSendCommand(L10NConstants.SET_CMD_PREFIX + L10NConstants.WPS_KEY + "=0");					
				Log.d(L10NConstants.TAG_BWS,"Response "+response);
				response = mSoftAPCfg.SapSendCommand(L10NConstants.SET_CMD_PREFIX +"commit");
				if(response.equals(L10NConstants.SUCCESS)){
					defPrefEditor.putString(L10NConstants.WPS_KEY, L10NConstants.VAL_ZERO);	
					orgPrefEditor.putString(L10NConstants.WPS_KEY, L10NConstants.VAL_ZERO);					
					if(timr!=null){
						timr.cancel();
					}					
				} else {
					defPrefEditor.putString(L10NConstants.WPS_KEY, L10NConstants.VAL_ONE);	
					orgPrefEditor.putString(L10NConstants.WPS_KEY, L10NConstants.VAL_ONE);					
				}
				defPrefEditor.commit();
				orgPrefEditor.commit();
			}
			MainMenuSettings.preferenceChanged = false;
		} 
		return prefChStatus;
	}

	/**
	 * Verifies network mode throws warning dialog when it matches<br>
	 * Network Mode=n & security_mode=WPA-PSK/WPA2-PSK/WPA-WPA2 Mixed & Encyption algorithm=TKIP<br>
	 * Network Mode=n-only & security_mode=WPA-PSK/WPA2-PSK/WPA-WPA2 Mixed & Encyption algorithm=TKIP<br>
	 * Network Mode=n & security_mode=WEP Mixed<br>
	 *   
	 * @param sWarning Message to be displayed as warning
	 * @param lstPref represents ListPreference of network mode
	 * @param lstEntry represents one of the B/G/N/BG/BGN
	 */
	public void verifyNModeAlgorithm(String sWarning, ListPreference lstPref, String lstEntry)
	{
		if(bNModeVerifyCheck){
			showAlertDialog(sWarning);							
			prefChStatus = false;
		} 
		else{		
			if((sSM.equals(L10NConstants.VAL_TWO) && sWpa.equals(L10NConstants.WPA_ALG_TKIP)) 
					|| (sSM.equals(L10NConstants.VAL_THREE) && sRsn.equals(L10NConstants.WPA_ALG_TKIP))
					|| (sSM.equals(L10NConstants.VAL_FOUR) && 
							(sRsn.equals(L10NConstants.WPA_ALG_TKIP) || sWpa.equals(L10NConstants.WPA_ALG_TKIP)))) {
				showAlertDialog(NW_MODE_WARNING);
				prefChStatus = false;
			}else
				lstPref.setSummary(lstEntry);				
		}
	}

	/**
	 * This method starts the timer counter
	 */
	public void startTimer()
	{
		if(timr!=null)
			timr.cancel();
		timr=new timer(L10NConstants.MINUTE, 1000);
		timr.start();
	}

	private boolean isAvailable(String[] dr, String sDataRate){
		boolean available = false; 
		for(int i = 0; i < dr.length; i++){
			if(sDataRate.equals(dr[i])){
				available = true;
				break;							
			}
		}		
		return available;
	}

	/**
	 * this method show the dialog when its is enabled
	 * 
	 * @param id ID of the Dialog box
	 * @return Dialog Returns the Dialog box based on the ID
	 */
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case L10NConstants.DIALOG_WPS: 			
			dialWPS = new ProgressDialog(this);                
			dialWPS.setMessage("Applying Changes to softAP...");			
			dialWPS.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialWPS.setIndeterminate(true);
			dialWPS.setCancelable(true);			
			return dialWPS;		
		}
		return null;
	}

	private void showAlertDialog(String AlertMessage){
		new AlertDialog.Builder(this)				                
		.setTitle(getString(R.string.str_dialog_warning))
		.setMessage(AlertMessage)
		.setPositiveButton(getString(R.string.alert_dialog_rename_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {			
				//do nothing
			}			
		}).create().show();	
	}
	/**
	 * 
	 * Its a thread to launch progress bar dialog for wps.
	 *
	 */
	public class DialogThrd implements Runnable {
		int dialogID;
		public Handler mHandler;
		Thread t;
		/**
		 * Its a thread initilizer and also starts the thread 
		 */
		public DialogThrd(int id) {
			dialogID=id;
			t=new Thread(this);t.start();
		}
		/**
		 * Thread body to launch the progress bar dialog for wps.
		 */
		public void run() {
			Looper.prepare();
			switch(dialogID) {
			case L10NConstants.DIALOG_WPS: showDialog(L10NConstants.DIALOG_WPS);
			break;
			}
			Looper.loop();
		}
	}
	public void onResume() {
		super.onResume();
	}
	/**
	 * It restore the old values of wps_state ,when commit failure or wpa failure is received 
	 * @return boolean
	 */
	public boolean setPrevValue() {		
		defSharPref.edit().putString(L10NConstants.WPS_KEY, L10NConstants.VAL_ZERO);
		orgSharPref.edit().putString(L10NConstants.WPS_KEY, L10NConstants.VAL_ZERO);
		defSharPref.edit().commit();
		orgSharPref.edit().commit();		
		return false;
	}
	/**
	 * This class launches a two minute timer for wps.
	 * 
	 */
	private class timer extends CountDownTimer {		
		int counter = 0;
		/**
		 * timer initializer
		 */
		public timer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}		
		/**
		 * This method dismisses the wps dialog when timer expires.
		 */
		@Override
		public void onFinish() {
			if(wpsDialog!=null) {
				wpsAlertDialog.dismiss();	
			}								
			defPrefEditor.putString(L10NConstants.CONFIG_KEY, "");
			defPrefEditor.commit();
			intent = new Intent(getApplicationContext(), BasicWirelessSettings.class);
			startActivity(intent);
			finish();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			Log.d(L10NConstants.TAG_BWS,"Timer is running count:"+(++counter));
		}
	}
	/**
	 * This method sets the updates given by the user,when failed the values is copied from original to copy file,
	 * whereas when it get success,the values are copied from copy file to original
	 * 
	 */
	public void UpdateChanges(SharedPreferences sour,SharedPreferences dest,String key) {
		String configKey = sour.getString(L10NConstants.CONFIG_KEY,"");				
		SharedPreferences.Editor edit = dest.edit();
		edit.putString(L10NConstants.CONFIG_KEY, configKey);
		if(key.equals("wpsKey")) {
			edit.putString("wpsKey", wpsKey);
		}
		edit.commit();
	}
	/**
	 * This Method shows the pop up when user clicks on enroll button list
	 * 
	 */
	public void showWpsDialog(String Enroll,String pin) {
		TextView view;
		LayoutInflater factory = LayoutInflater.from(BasicWirelessSettings.this);
		final View textEntryView = factory.inflate(R.layout.alert_message, null);						
		view = (TextView) textEntryView.findViewById(R.id.message_view);
		if(Enroll.equals("PIN ENTRY")) {
			view.setText("Enter PIN number "+pin+" on the client to connect");	
		} else {
			view.setText("Press the Push button on the client to connect");	
		}
		wpsDialog=new AlertDialog.Builder(BasicWirelessSettings.this);				                
		wpsDialog.setTitle("WPS Session is On");
		wpsDialog.setView(textEntryView);

		wpsAlertDialog = wpsDialog.setPositiveButton(getString(R.string.alert_dialog_rename_ok), 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//do nothing
			}				
		}).create();
		wpsAlertDialog.show();
	}

	public void onDestroy() {
		super.onDestroy();
		if(wpsDialog!=null)	{
			wpsAlertDialog.dismiss();	
		}
		if(timr!=null) {
			timr.cancel();
		}
		defPrefEditor.putString(L10NConstants.CONFIG_KEY, "");
		defPrefEditor.commit();
	}
}
