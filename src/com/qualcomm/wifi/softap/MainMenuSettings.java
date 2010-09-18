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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.qualcomm.wifi.softap.ns.NetworkSettings;
import com.qualcomm.wifi.softap.ss.StationStatus;
import com.qualcomm.wifi.softap.ws.BasicWirelessSettings;
import com.qualcomm.wifi.softap.ws.WirelessSettings;
/** 
 * The class contains the Application Main screen display, it provides UI to turn on/turn off
 * softAP and options for softAP settings. It does provide the 'Save Settings' button to save
 * the changed settings and also the 'Reset Settings' button to reset the softAP settings.
 *  
 * {@link com.qualcomm.wifi.softap.ws.BasicWirelessSettings}<br>
 * {@link com.qualcomm.wifi.softap.ns.NetworkSettings}<br>
 * {@link com.qualcomm.wifi.softap.mgmt.Management}<br>
 * {@link com.qualcomm.wifi.softap.prof.ProfileSettings}<br>
 */
public class MainMenuSettings extends PreferenceActivity implements OnPreferenceClickListener, 
OnPreferenceChangeListener, OnClickListener, QWiFiSoftApEvent {	
	private String sKeyVal;	
	private int index;
	private String sResponse;
	public static Context app_Context;
	public static String TAG;
	public boolean bStatusRunning = false;
	public static String sWpsResponse, sCommitResponse;
	private String sAllowAddList = new String("");
	private String sAllowRemoveList = new String("");
	private String sDenyAddList = new String("");
	private String sDenyRemoveList = new String("");
	private String[] defaultKey;

	public static boolean bStatus = false, preferenceChanged;
	private String sCheckKeyValue;	
	private boolean bIsValid = false;

	public static Button saveBtn, resetBtn;
	private ProgressDialog dialOn, dialOff, dialSave, dialReset, dialInitial;
	private Preference wsPref, nsPref, statusPref;
	private CheckBoxPreference wifiCheckEnable;
	private SharedPreferences.Editor defPrefEditor, orgPrefEditor;	
	private SharedPreferences defSharPref, orgSharPref;
	private NotificationManager notifyManager;

	public static QWiFiSoftApCfg mSoftAPCfg;	
	public static StationStatus ssrefToMMS;	
	private ArrayList<String> changes_List = new ArrayList<String>();
	Intent intent;

	/**
	 * This method handles any events received in the string format. two main events Association and Dissociation,
	 * for Association the wps dialog if launched, is dismissed and for either Association/Disassociation the station status
	 * Activity if running, will be restarted. 
	 */
	public void EventHandler(String evt) {
		Log.e("EVTHLR", evt);		
		showNotification(evt);
		Intent i = new Intent(MainMenuSettings.this, StationStatus.class);
		if(evt.contains(L10NConstants.STATION_102) || evt.contains(L10NConstants.STATION_103)){			
			Log.d("EventHandler", "Asso/Disso of StationStatus:"+bStatusRunning);
			if(bStatusRunning) {
				if(ssrefToMMS!=null) {
					Log.d("EventHandler", evt+" REF NOT NULL StationStatus:"+bStatusRunning);					
					ssrefToMMS.finish();
					startActivityForResult(i, 0);
					bStatusRunning = true;
				}
			}
			Log.d(TAG,"Recieved 102 Station");
			if(evt.contains(L10NConstants.STATION_102)){
				if(BasicWirelessSettings.wpsAlertDialog!=null) {				
					Log.d(TAG,"dismissing wpsAlertDialogue");
					BasicWirelessSettings.wpsAlertDialog.dismiss();
				}
			}
		}		
	}
	
	/**
	 * This method initialize all the resources such as preferences, notification manager.
	 * It also set the status checked/unchecked based on the daemon value
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.main_settings_pref);

		//get the application context
		app_Context=getApplicationContext();

		setContentView(R.layout.mms_layout);
		TAG = getString(R.string.tag) + "MMS";

		//get the reference to QWiFiSoftApCfg class object
		mSoftAPCfg = new QWiFiSoftApCfg(this);

		//Create notification manager
		notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		//Edit default and original file
		defSharPref = PreferenceManager.getDefaultSharedPreferences(this);
		defPrefEditor = defSharPref.edit();	
		orgSharPref = getSharedPreferences(L10NConstants.CONFIG_FILE_NAME, MODE_PRIVATE);
		orgPrefEditor = orgSharPref.edit();

		//get the reference to the WI-FI AP,wireless setting,network settings, status 
		wsPref = (Preference) findPreference(getString(R.string.ws_key));
		nsPref = (Preference) findPreference(getString(R.string.ns_key));
		statusPref = (Preference) findPreference(getString(R.string.station_status_key));
		wifiCheckEnable = (CheckBoxPreference) findPreference(getString(R.string.wifi_status_key));
		wsPref.setOnPreferenceClickListener(this);
		nsPref.setOnPreferenceClickListener(this);	
		statusPref.setOnPreferenceClickListener(this);
		wifiCheckEnable.setOnPreferenceChangeListener(this);
		wifiCheckEnable.setOnPreferenceClickListener(this);

		//get the reference to the save and reset buttons and set OnClick listener
		saveBtn = (Button) findViewById(R.id.save);
		resetBtn = (Button) findViewById(R.id.reset);
		saveBtn.setOnClickListener(this);
		resetBtn.setOnClickListener(this);

		//set initial save button to false
		saveBtn.setEnabled(false);
		preferenceChanged = false;

		//get command for enable_softap to get the response from the daemon		
		Log.d(TAG,"Getting Command "+ L10NConstants.GET_CMD_PREFIX + L10NConstants.ENABLE_SOFTAP);
		sKeyVal = mSoftAPCfg.SapSendCommand(L10NConstants.GET_CMD_PREFIX + L10NConstants.ENABLE_SOFTAP);
		Log.d(TAG,"Received response " + sKeyVal);

		//On success from the daemon for enable_softap start a thread to display a looper progress dialog
		//and enable the options
		if(sKeyVal.contains(L10NConstants.SUCCESS)){
			if(sKeyVal.contains(L10NConstants.VAL_ONE)){
				new DialogThr(L10NConstants.DIALOG_INITIAL);
				enableOrDisable(true, L10NConstants.VAL_ONE);				
				if(dialInitial!=null)
					dialInitial.cancel();
			} else
				enableOrDisable(false, L10NConstants.VAL_ZERO);			
		} else 
			wifiCheckEnable.setEnabled(false);

		orgPrefEditor.putString("wpsKey", "");
		orgPrefEditor.commit();

		//if Wi-Fi app check box is unchecked set both save and reset button disabled
		if(!wifiCheckEnable.isChecked()){
			saveBtn.setEnabled(false);
			resetBtn.setEnabled(false);
		}
		intent = new Intent();
	}

	/**
	 * This method handles the 'Save Settings' and 'Reset Settings' button click events.
	 */
	public void onClick(View v) {
		int id = v.getId();
		switch(id){
		case R.id.save:
			new DialogThr(L10NConstants.DIALOG_SAVE);
			saveChanges(defSharPref, orgSharPref, "");
			saveBtn.setEnabled(false);
			if(dialSave != null)
				dialSave.cancel();			
			break;
		case R.id.reset:
			new DialogThr(L10NConstants.DIALOG_RESET);
			mSoftAPCfg.SapSendCommand(L10NConstants.SET_CMD_PREFIX + "reset_to_default");			
			getSoftAPValues();			
			saveBtn.setEnabled(false);	
			if(dialReset != null) 
				dialReset.cancel();
			preferenceChanged = false;
			break;
		}		
	}
	/**
	 * This method issues the get command to set all the values of softAP.
	 */
	public void getSoftAPValues(){
		defaultKey = getResources().getStringArray(R.array.defaultProfileKeys);
		for(int j = 0; j < defaultKey.length; j++){
			Log.d(TAG,"Getting Command "+L10NConstants.GET_CMD_PREFIX +defaultKey[j]);			
			sKeyVal = mSoftAPCfg.SapSendCommand(L10NConstants.GET_CMD_PREFIX +defaultKey[j]);
			Log.d(TAG,"Received response " + sKeyVal);	
			index = sKeyVal.indexOf("=");	

			if(sKeyVal.contains(L10NConstants.SUCCESS)) {
				sCheckKeyValue = sKeyVal.substring(sKeyVal.indexOf(" ")+1, index);

				if(sCheckKeyValue.equals(defaultKey[j])){						
					if(defaultKey[j].equals(L10NConstants.CHNL_KEY)){
						if(sKeyVal.substring(index+1, sKeyVal.length()).contains(",")) {							
							sCheckKeyValue = sKeyVal.substring(index+1,sKeyVal.indexOf(","));
							defPrefEditor.putString("autoChannel", sCheckKeyValue);
							orgPrefEditor.putString("autoChannel", sCheckKeyValue);

							sCheckKeyValue = sKeyVal.substring(sKeyVal.indexOf(",")+1,sKeyVal.length());
							defPrefEditor.putString(L10NConstants.CHNL_KEY, sCheckKeyValue);
							orgPrefEditor.putString(L10NConstants.CHNL_KEY, sCheckKeyValue);
							commitPref();
						} else {
							SetDefaultValues(defaultKey[j]);
						}
					} else if(defaultKey[j].equals("allow_list")){						
						getAllowDenyValue(defaultKey[j],"allow_list");
					}else if(defaultKey[j].equals("deny_list")){						
						getAllowDenyValue(defaultKey[j],"deny_list");
					} else {
						if(defaultKey[j].equals(L10NConstants.COUNTRY_KEY)){
							sKeyVal = sKeyVal.substring(0, sKeyVal.length()-1);	
							} else if(defaultKey[j].equals("wep_key0") || defaultKey[j].equals("wep_key1")||													
								defaultKey[j].equals("wep_key2") || defaultKey[j].equals("wep_key3")){
							if(sKeyVal.contains("\"")) {
								Log.d(TAG,"WEP_KEy"+ sKeyVal.substring(0,sKeyVal.indexOf("=")+1)
										.concat(sKeyVal.substring(sKeyVal.indexOf("=")+2,sKeyVal.length()-1)));
								sKeyVal = sKeyVal.substring(0,sKeyVal.indexOf("=")+1)
								.concat(sKeyVal.substring(sKeyVal.indexOf("=")+2,sKeyVal.length()-1));
							}
						}
						SetDefaultValues(defaultKey[j]);
					}
				}
			} else {					
				defPrefEditor.putString(defaultKey[j], "");
				orgPrefEditor.putString(defaultKey[j], "");
				commitPref();
			}
		}
	}
	/**
	 * This method shows a dialog box to save settings when application is tried to close without clicking 'Save Settings'
	 * button for changes.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);		
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(saveBtn.isEnabled()){
				new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(getString(R.string.alert_save))
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {	
						saveChanges(defSharPref, orgSharPref, "");
						saveBtn.setEnabled(false);						
						finish();
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {						
						SetOriginalValues();						
						finish();
					}
				}).create().show();
			}			
			return true;
		}else {			
			return super.onKeyDown(keyCode, event);				
		}
	}
	/**
	 * This method configures the softAP with changes made by the user.
	 * 
	 * @param defSharPref Copy Preference file
	 * @param orgSharPref Original Preference file	 
	 * @param bws_flag Basic Wireless Settings
	 */
	public void saveChanges(SharedPreferences defSharPref, SharedPreferences orgSharPref, 
			String bws_flag) {
		int successCount = 0;
		int unsupportedCount = 0;	
		defPrefEditor = defSharPref.edit();	
		orgPrefEditor = orgSharPref.edit();
		ArrayList<String> changedCmdLst = comparePreferenceFiles(defSharPref, orgSharPref);
		Log.d(TAG,"LIST "+ changedCmdLst);
		if(changedCmdLst != null){
			for(int i = 0; i < changedCmdLst.size(); i++) {
				Log.d(TAG,"Sending Command "+L10NConstants.SET_CMD_PREFIX +changedCmdLst.get(i));
				sResponse = mSoftAPCfg.SapSendCommand(L10NConstants.SET_CMD_PREFIX + changedCmdLst.get(i));
				Log.d(TAG,"Received Response "+sResponse);

				String sSetKey = changedCmdLst.get(i).substring(0, changedCmdLst.get(i).indexOf("="));				
				String sSetValue = changedCmdLst.get(i).substring(changedCmdLst.get(i).indexOf("=")+1, 
						changedCmdLst.get(i).length());

				if(sSetKey.equals(L10NConstants.WPS_KEY)){
					sWpsResponse = sResponse;
				}
				if(sResponse.equals(L10NConstants.SUCCESS)) {
					if(sSetKey.equals("add_to_allow_list") || sSetKey.equals("add_to_deny_list")) {
						int index = changedCmdLst.get(i).indexOf("=");
						String value = changedCmdLst.get(i).substring(index+1, changedCmdLst.get(i).length());						
						StringTokenizer st = new StringTokenizer(value);									
						while(st.hasMoreElements()){
							String nextToken = st.nextToken();																	
							if(sSetKey.contains(L10NConstants.ALLOW)){	
								addAllowDenyList(orgSharPref, L10NConstants.ALLOW,nextToken);
							}else{	
								addAllowDenyList(orgSharPref, L10NConstants.DENY, nextToken);																			
							}									
						}
					}else if(sSetKey.equals("remove_from_allow_list") || sSetKey.equals("remove_from_deny_list")){
						int index = changedCmdLst.get(i).indexOf("=");
						String value = changedCmdLst.get(i).substring(index+1, changedCmdLst.get(i).length());						
						StringTokenizer st = new StringTokenizer(value);
						
						while(st.hasMoreElements()){										
							String nextToken = st.nextToken();
							if(sSetKey.contains(L10NConstants.ALLOW)) {	
								removeAllowDenyList(orgSharPref, L10NConstants.ALLOW, nextToken);								
							}else{
								removeAllowDenyList(orgSharPref, L10NConstants.ALLOW, nextToken);								
							}										
						}						
					} else {
						if(sSetValue.contains("\"")) {							
							sSetValue = sSetValue.substring(1, sSetValue.length()-1);
						} 
						if(sSetKey.equals(L10NConstants.COUNTRY_KEY)){
							Log.d(TAG, "Changed Country Code : "+changedCmdLst.get(i));							
							orgPrefEditor.putString(sSetKey, defSharPref.getString(L10NConstants.COUNTRY_KEY, ""));
							commitPref();
						}else{
							orgPrefEditor.putString(sSetKey, sSetValue);
							commitPref();
						}							
					} 
					successCount++;
				} else {
					sSetValue = orgSharPref.getString(sSetKey, "");
					defPrefEditor.putString(sSetKey, sSetValue);
					defPrefEditor.commit();					

					unsupportedCount++;					
				} 
			}
			if(changedCmdLst.size() == (successCount+unsupportedCount)){
				sResponse = mSoftAPCfg.SapSendCommand(L10NConstants.SET_CMD_PREFIX +"commit");	
				sCommitResponse = sResponse;				
				Log.d(TAG,"Received Response ..... "+sResponse);
				Log.d(TAG, "Commited Changes");	
				if(!sResponse.equals(L10NConstants.SUCCESS)){
					for(int i = 0; i < changedCmdLst.size() ; i++){					
						if(changedCmdLst.get(i).contains(L10NConstants.WPS_KEY)){							
							defPrefEditor.putString(L10NConstants.WPS_KEY, L10NConstants.VAL_ZERO);
							orgPrefEditor.putString(L10NConstants.WPS_KEY, L10NConstants.VAL_ZERO);
							commitPref();
						}
					}
				}
			}
			preferenceChanged = false;
		}
		if(bIsValid){
			showWarningDialog(bws_flag, defSharPref, orgSharPref);
			bIsValid = false;
		}
	}
	
	/**
	 * This method will remove the particular MAC Address from the original preference file
	 *  
	 * @param orgFile Original Preference File Name
	 * @param lstType MAC Address List Type
	 * @param macAddr MAC Address, which has to be removed 
	 */	
	private void removeAllowDenyList(SharedPreferences orgFile,
			String lstType, String macAddr) {
		for( int m = 1; m <= L10NConstants.MAX_LENGTH; m++){												
			String findVal = orgFile.getString(lstType+m, "");	
			if(macAddr.equals(findVal)){
				orgPrefEditor.putString(lstType+m, "");
				orgPrefEditor.commit();
				for(int l = 1; l < L10NConstants.MAX_LENGTH; l++ ) {
					if(orgFile.getString(lstType+l,"").equals("")) {
						String val = orgFile.getString(lstType+(l+1),"");
						orgPrefEditor.putString(lstType+l,val);	
						orgPrefEditor.putString(lstType+(l+1),"");
						orgPrefEditor.commit();
					}										
				}
			}
		}		
	}
	
	/**
	 * This method will add MAC address in the Original Preference file
	 * 
	 * @param orgFile Original Preference File Name
	 * @param lstType MAC Address List Type
	 * @param macAddr MAC Address, which has to be added
	 */
	private void addAllowDenyList(SharedPreferences orgFile,
			String lstType, String macAddr) {
		for (int m = 1; m <= L10NConstants.MAX_LENGTH; m++) {
			String checkvalue = orgFile.getString(lstType+m, "");	
			if(checkvalue.equals("")) {
				orgPrefEditor.putString(lstType+m, macAddr);
				orgPrefEditor.commit();
				break;
			}
		}	
	}
	/**
	 * This thread implements a method for launching different progress bar dialog boxes.
	 */
	public class DialogThr implements Runnable {
		int dialogID;
		public Handler mHandler;
		Thread thread;
		/**
		 * Its a thread initializer and also starts the thread. 
		 */
		public DialogThr(int id){
			dialogID=id;
			thread = new Thread(this);
			thread.start();
		}
		/**
		 * Thread body to launch the  progress bar dialog.
		 */
		public void run() {
			Looper.prepare();			
			switch(dialogID) {
			case L10NConstants.DIALOG_OFF: 
				showDialog(L10NConstants.DIALOG_OFF);
				break;
			case L10NConstants.DIALOG_ON: 
				showDialog(L10NConstants.DIALOG_ON);
				break;
			case L10NConstants.DIALOG_SAVE: 
				showDialog(L10NConstants.DIALOG_SAVE);
				break;
			case L10NConstants.DIALOG_RESET: 
				showDialog(L10NConstants.DIALOG_RESET);
				break;
			case L10NConstants.DIALOG_INITIAL: 
				showDialog(L10NConstants.DIALOG_INITIAL);
				break;			
			}
			Looper.loop();
		}
	}
	/**
	 * This method handles the preference click events to redirect to the corresponding preference activity screen.
	 */
	public boolean onPreferenceClick(Preference preference) {
		if (preference == wsPref) {			
			intent.setClass(MainMenuSettings.this, WirelessSettings.class);			
			startActivity(intent);
		} else if (preference == nsPref) {			
			intent.setClass(MainMenuSettings.this, NetworkSettings.class);		
			startActivity(intent);
		} else if (preference == statusPref) {			
			intent.setClass(MainMenuSettings.this, StationStatus.class);		
			startActivityForResult(intent, 0);
			bStatusRunning = true;
		} 
		return true;
	}	
	/**
	 * This method handles the preference change event for Wi-Fi Turn Off/Turn On. 
	 */
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		boolean isValid = false;		
		if (preference == wifiCheckEnable) {			
			if (wifiCheckEnable.isChecked()) {
				new DialogThr(L10NConstants.DIALOG_OFF);				
				Log.d(TAG,"Notification Removed");
				notifyManager.cancel(R.string.notification);			
				Log.d(TAG,"Sending Command "+L10NConstants.SET_CMD_PREFIX 
						+ L10NConstants.ENABLE_SOFTAP+"=0");	
				sResponse = mSoftAPCfg.SapSendCommand(L10NConstants.SET_CMD_PREFIX 
						+ L10NConstants.ENABLE_SOFTAP+"=0");
				Log.d(TAG, "Received Response ........: " + sResponse);				
				if(sResponse.equals(L10NConstants.SUCCESS)){					
					isValid = enableOrDisable(false, L10NConstants.VAL_ZERO);//enableFalse();					
				}else {
					isValid = enableOrDisable(true, L10NConstants.VAL_ONE);//enableTrue();
				}				
				if(dialOff!=null)
					dialOff.cancel();
			} else {
				new DialogThr(L10NConstants.DIALOG_ON);				
				Log.d(TAG,"Sending Command "+L10NConstants.SET_CMD_PREFIX 
						+ L10NConstants.ENABLE_SOFTAP+"=1");				
				sResponse = mSoftAPCfg.SapSendCommand(L10NConstants.SET_CMD_PREFIX 
						+ L10NConstants.ENABLE_SOFTAP+"=1");				
				if(sResponse.equals(L10NConstants.SUCCESS)){					
					Log.d(TAG,"Received response "+sResponse);					
					isValid = enableOrDisable(true, L10NConstants.VAL_ONE);//enableTrue();					
				}else {
					Log.d(TAG,"Received response "+sResponse);
					isValid = enableOrDisable(false, L10NConstants.VAL_ZERO);//enableFalse();					
				}
				if(dialOn!=null)
					dialOn.cancel();
			}		
		}		
		return isValid;
	}
	/**
	 * This method will Enable or Disable the WifiSoftAP based on the parameters
	 * 
	 * @param Enable if bStatus is True or Disable
	 * @param Value to update the preference file
	 * 
	 * @return Returns <b> bStatus <b> Parameter
	 */
	private boolean enableOrDisable(boolean bStatus, String sVal){	
		if(bStatus){
			getSoftAPValues();						
			showNotification();	
			Log.d(TAG,"Launching APEventMonitor Thread");
			if(APEventMonitor.KILLED){
				new APEventMonitor(mSoftAPCfg,getApplicationContext());
				APEventMonitor.KILLED = false;
			}
		} else {
			saveBtn.setEnabled(bStatus);		
			preferenceChanged = bStatus;
			killMonitorThread();
		}
		orgPrefEditor.putString(L10NConstants.ENABLE_SOFTAP, sVal);
		defPrefEditor.putString(L10NConstants.ENABLE_SOFTAP, sVal);		
		defPrefEditor.putBoolean(L10NConstants.WIFI_AP_CHECK, bStatus);		
		commitPref();	
		wifiCheckEnable.setChecked(bStatus);
		resetBtn.setEnabled(bStatus);	
		return bStatus;
	}

	/**
	 * This method shows a notification in the status bar for station events.
	 * 
	 * @param New Joined Station MAC Address
	 */
	private void showNotification(String event) {		
		CharSequence text = event;
		Notification notification = new Notification(R.drawable.ap_notify, text, System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,new Intent(this, MainMenuSettings.class), 0);
		notification.setLatestEventInfo(this, text, text, contentIntent);
		notifyManager.notify(L10NConstants.EVENT_ID, notification);
		notifyManager.cancel(L10NConstants.EVENT_ID);
	}
	
	/**
	 * This method shows a notification in the status bar for Wi-Fi softAP Turn On/Turn Off events.
	 */
	private void showNotification() {		
		CharSequence text = getText(R.string.notification);
		Notification notification = new Notification(R.drawable.ap_notify,text, System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,new Intent(this, MainMenuSettings.class), 0);
		notification.setLatestEventInfo(this, text, text, contentIntent);
		notifyManager.notify(R.string.notification, notification);
	}

	public void onResume(){
		super.onResume();	
		if(preferenceChanged){	
			saveBtn.setEnabled(true);
		}
	}
	/**
	 * This method displays different progress bar dialogs. 
	 */
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case L10NConstants.DIALOG_OFF: 			
			dialOff = dialogCreater("Turning off SoftAP...");
			return dialOff;	
		case L10NConstants.DIALOG_ON:			
			dialOn = dialogCreater("Turning on SoftAP...");
			return dialOn;		
		case L10NConstants.DIALOG_RESET:
			dialReset = dialogCreater("Resetting SoftAP...");
			return dialReset;		
		case L10NConstants.DIALOG_SAVE:
			dialSave = dialogCreater("Applying Changes to SoftAP...");
			return dialSave;		
		case L10NConstants.DIALOG_INITIAL:			
			dialInitial = dialogCreater("Initializing SoftAP...");
			return dialInitial;
		}
		return null;
	}
	
	private ProgressDialog dialogCreater(String msg){
		ProgressDialog temp=new ProgressDialog(this);
		temp.setMessage(msg);
		temp.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		temp.setIndeterminate(true);
		temp.setCancelable(true);
		return temp;
	}
	/**
	 * This method compares the current preference file with the previous preference file. 
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> comparePreferenceFiles(SharedPreferences defSharPref, 
			SharedPreferences orgSharPref) {
		changes_List.clear();
		Map<String ,?> defMap, orgMap;
		Set<?> defSet, orgSet;
		Iterator<?> defIterator, orgIterator;
		Map.Entry defMapEntry = null, orgMapEntry = null;
		String sPrefKey;
		defMap = defSharPref.getAll();
		orgMap = orgSharPref.getAll();	

		for( int m = 1; m <= L10NConstants.MAX_LENGTH; m++) {		
			bStatus = false;
			String copyAllow = defSharPref.getString(L10NConstants.ALLOW+m, "");			
			String copyDeny = defSharPref.getString(L10NConstants.DENY+m, "");	
			String orgAllow = orgSharPref.getString(L10NConstants.ALLOW+m, "");			
			String orgyDeny = orgSharPref.getString(L10NConstants.DENY+m, "");
			
			compareAllowDenyList(orgSharPref, copyAllow, L10NConstants.ALLOW);
			compareAllowDenyList(orgSharPref, copyDeny, L10NConstants.DENY);
			compareAllowDenyList(defSharPref, orgAllow, L10NConstants.ALLOW);
			compareAllowDenyList(defSharPref, orgyDeny, L10NConstants.DENY);
		}	

		defSet = defMap.entrySet();				
		defIterator = defSet.iterator();	
		while(defIterator.hasNext()) {
			defMapEntry = (Map.Entry) defIterator.next();

			orgSet = orgMap.entrySet();
			orgIterator = orgSet.iterator();

			while(orgIterator.hasNext()) {
				orgMapEntry = (Map.Entry) orgIterator.next();				

				if(defMapEntry.getKey().equals(orgMapEntry.getKey())) {	
					if(orgMapEntry.getValue() instanceof String){		
						if(!defMapEntry.getValue().equals(orgMapEntry.getValue())) {
							sPrefKey = defMapEntry.getKey().toString();						

							if(!sPrefKey.contains(L10NConstants.ALLOW) && 
									!sPrefKey.contains(L10NConstants.DENY)) {
								if(sPrefKey.equals("wep_key0") || sPrefKey.equals("wep_key1") ||													
										sPrefKey.equals("wep_key2") || sPrefKey.equals("wep_key3")) {										
									if(defMapEntry.getValue().toString().length() == 5
											|| defMapEntry.getValue().toString().length()==13
											|| defMapEntry.getValue().toString().length()==16) {
										changes_List.add(defMapEntry.getKey()+"="+"\""+defMapEntry.getValue()+"\"");											
									} else {
										changes_List.add(defMapEntry.getKey()+"="+defMapEntry.getValue());										
									}
								} else if (sPrefKey.equals(L10NConstants.COUNTRY_KEY)){
									changes_List.add(defMapEntry.getKey()+"="+defMapEntry.getValue().toString().substring(0, defMapEntry.getValue().toString().indexOf(","))+"I");		
									Log.d(TAG,"Changed Value "+defMapEntry.getKey()+"="+defMapEntry.getValue()+"I");

								} else if (sPrefKey.equals(L10NConstants.SEC_MODE_KEY) || 
										sPrefKey.equals(L10NConstants.HW_MODE_KEY)){									
									String networkCheck = defSharPref.getString(L10NConstants.HW_MODE_KEY, "");
									String secCheck = defSharPref.getString(L10NConstants.SEC_MODE_KEY, "");
									String rsnCheck = defSharPref.getString(L10NConstants.RSN_PAIR_KEY, "");
									String wpaCheck = defSharPref.getString(L10NConstants.WPA_PAIR_KEY, "");

									if(networkCheck.equals(L10NConstants.SM_N_ONLY) || networkCheck.equals(L10NConstants.SM_N)) {											
										if((secCheck.equals(L10NConstants.VAL_TWO) && wpaCheck.equals(L10NConstants.WPA_ALG_TKIP))
												|| (secCheck.equals(L10NConstants.VAL_THREE) && rsnCheck.equals(L10NConstants.WPA_ALG_TKIP))
												|| (secCheck.equals(L10NConstants.VAL_FOUR) && 
														(rsnCheck.equals(L10NConstants.WPA_ALG_TKIP) || wpaCheck.equals(L10NConstants.WPA_ALG_TKIP)))){
											bIsValid = true;
										} 
									}
									if(!bIsValid){
										updateChangesList(defMapEntry);
									}
								} else {
									updateChangesList(defMapEntry);
								}										
							}
						}
					}
					break;
				}
			}
		}
		if(!sAllowRemoveList.equals("")) {
			changes_List.add("remove_from_allow_list=" + sAllowRemoveList.substring(0, sAllowRemoveList.length()-1));
			sAllowRemoveList = "";
		} 
		if(!sDenyRemoveList.equals("")) {
			changes_List.add("remove_from_deny_list=" + sDenyRemoveList.substring(0, sDenyRemoveList.length()-1));
			sDenyRemoveList = "";
		} 
		if(!sAllowAddList.equals("")) {
			changes_List.add("add_to_allow_list=" + sAllowAddList.substring(0, sAllowAddList.length()-1));
			sAllowAddList = "";
		} 
		if(!sDenyAddList.equals("")) {
			changes_List.add("add_to_deny_list=" + sDenyAddList.substring(0, sDenyAddList.length()-1));
			sDenyAddList = "";		
		}	
		return changes_List;
	}

	@SuppressWarnings("unchecked")
	private void updateChangesList(Map.Entry Copy) {
		changes_List.add(Copy.getKey()+"="+Copy.getValue());		
		Log.d(TAG,"Changed Value "+Copy.getKey()+"="+Copy.getValue());
	}

	private void compareAllowDenyList(SharedPreferences sharedPref, String sMacAddr, String listType) {	
		for( int n = 1; n <= L10NConstants.MAX_LENGTH; n++){
			String orignAllow = sharedPref.getString(listType + n, "");
			if(!sMacAddr.equals("")){
				if(sMacAddr.equals(orignAllow)){						
					bStatus = false;
					break;
				} else {	
					bStatus = true;
				}
			}				
		}
		if(bStatus) {
			if(sharedPref.equals(orgSharPref)) {
				if(listType.equals(L10NConstants.ALLOW)){					
					sAllowAddList += sMacAddr+" ";
				} else {
					sDenyAddList += sMacAddr+" ";
				}
			} else {
				if(listType.equals(L10NConstants.ALLOW)) {
					sAllowRemoveList += sMacAddr+" ";
				} else {
					sDenyRemoveList += sMacAddr+" ";
				}
			}
		}
		bStatus = false;		
	}

	/**
	 * This method will show the warning dialog when the network mode is N or BGN with respect to the  
	 * Security mode WPA_PSK, WPA2_PSK & WPA_PSK Mixed 
	 *    
	 * @param bws_flag Basic Wireless Settings 
	 * @param copyFile Copy Preference file
	 * @param orgFile Original preference file
	 */
	public void showWarningDialog(String bws_flag, SharedPreferences copyFile, SharedPreferences orgFile) {
		SharedPreferences.Editor copyEdit =  copyFile.edit();
		if(!bws_flag.equals("BWS")) {					
			new AlertDialog.Builder(this)				                
			.setTitle(getString(R.string.str_dialog_warning))
			.setMessage(getString(R.string.mms_screen_alert) +
					getString(R.string.common_append_alert_wpa))				
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// No Action
						}			
					}).create().show();	
		}		
		String prevSecurity = orgFile.getString(L10NConstants.SEC_MODE_KEY, "");
		String prevNWMode = orgFile.getString(L10NConstants.HW_MODE_KEY, "");
		copyEdit.putString(L10NConstants.SEC_MODE_KEY, prevSecurity);
		copyEdit.putString(L10NConstants.HW_MODE_KEY, prevNWMode);
		copyEdit.commit();
	}

	/**
	 * This Method Will set the Original values, which is from daemon  
	 */
	@SuppressWarnings("unchecked")
	private void SetOriginalValues() {
		Map<String, ?> allValues = new HashMap<String, String>();		
		allValues = orgSharPref.getAll();
		Set<?> s = allValues.entrySet();
		Iterator<?> it = s.iterator();
		while (it.hasNext()) {
			Map.Entry m = (Map.Entry) it.next();
			String key = (String) m.getKey();
			String value = null;			
			if (m.getValue() instanceof String) {
				value = (String) m.getValue();
				defPrefEditor.putString(key, value);
			} else {
				boolean wificheckVal = Boolean.parseBoolean(m.getValue()
						.toString());
				defPrefEditor.putBoolean(key, wificheckVal);
			}
		}
		defPrefEditor.commit();
		Log.d(TAG,"Commiting not Success");
		preferenceChanged = false;		
	}

	/**
	 * Set the Default Values to the WifiSoftAP
	 * 
	 * @param Value will be set to this key
	 */

	private void SetDefaultValues(String key) {
		String value = sKeyVal.substring(index + 1, sKeyVal.length());			

		if(key.equals(L10NConstants.COUNTRY_KEY)) {			
			final String[] countryArray = getResources().getStringArray(R.array.countryCodeValues);
			for(int i = 0; i < countryArray.length; i++){
				if(value.equals(countryArray[i].substring(0, countryArray[i].indexOf(",")))) {		
					value = value.concat(countryArray[i].substring(countryArray[i].indexOf(","), countryArray[i].length()));
					break;
				}
			}
		}		
		defPrefEditor.putString(key, value);
		orgPrefEditor.putString(key, value);
		commitPref();

	}

	/**
	 * Getting MAC Address allow or deny list from Daemon
	 * 
	 * @param MAC Address
	 * @param Key for that MAC Address
	 */
	private void getAllowDenyValue(String KeyValue, String key){
		defPrefEditor.putString(key, sKeyVal.substring(index+1,sKeyVal.length()));
		orgPrefEditor.putString(key, sKeyVal.substring(index+1,sKeyVal.length()));
		commitPref();
		index = sKeyVal.indexOf("=");
		String value = sKeyVal.substring(index+1, sKeyVal.length());		
		int KeyCheck = KeyValue.indexOf("_");

		if(!value.trim().equals("")){
			StringTokenizer st = new StringTokenizer(value);
			int i = 1;
			while(st.hasMoreElements()) {
				String nextToken = st.nextToken();
				defPrefEditor.putString(KeyValue.substring(0,KeyCheck)+i, nextToken);
				orgPrefEditor.putString(KeyValue.substring(0,KeyCheck)+i, nextToken);
				commitPref();
				i++;
			}
			if(i < L10NConstants.MAX_LENGTH) {
				createAllowDenyList(KeyValue, KeyCheck, i);				
			}
		} else {
			createAllowDenyList(KeyValue, KeyCheck, 1);
		} 
	}	
	private void createAllowDenyList(String KeyValue, int KeyCheck, int i){
		for(int j = i; j <= L10NConstants.MAX_LENGTH; j++){					
			defPrefEditor.putString(KeyValue.substring(0,KeyCheck)+j, "");
			orgPrefEditor.putString(KeyValue.substring(0,KeyCheck)+j, "");
			commitPref();
		}
	}	

	public void killMonitorThread(){
		APEventMonitor.KILLED=true;
	}

	public void onDestroy() {
		super.onDestroy();		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case 0:			
			if (resultCode == RESULT_OK) {
				bStatusRunning = false;
				break;
			}
		}
	}
	/**
	 * This method commits the changes made.
	 */
	public void commitPref(){
		defPrefEditor.commit();
		orgPrefEditor.commit();
	}
}