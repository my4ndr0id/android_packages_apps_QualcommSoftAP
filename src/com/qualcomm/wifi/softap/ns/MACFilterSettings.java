/*
 * Copyright (c) 2010, Code Aurora Forum. All rights reserved.
  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *  * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
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

package com.qualcomm.wifi.softap.ns;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.wifi.softap.L10NConstants;
import com.qualcomm.wifi.softap.MainMenuSettings;
import com.qualcomm.wifi.softap.QWiFiSoftApCfg;
import com.qualcomm.wifi.softap.R;

/**
 * This MACFilterSettings class configures MAC Address List for addition/removal of MAC address
 */
public class MACFilterSettings extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener, OnClickListener{
	AlertDialog mfsalertdialog;
	Builder mfsbuilder;
	private QWiFiSoftApCfg qwifisoftAPCfg;
	private Button add;
	private EditText editMacAddr;
	private LayoutInflater ltfactory;
	private TextWatcher macAddrWatcher;
	private TextView txtMacAddr;
	private View textEntryView;
	
	private ListPreference macFilterMode;	
	private PreferenceCategory macAddrLst;
	private PreferenceScreen prefScr;
	private SharedPreferences defSharPref;
	private SharedPreferences.Editor defPrefEditor;
	private Preference pref;
	public String allowkeys[];
	public String denykeys[];
	private String slstType, slstEntries;
	private boolean blstTypeAorD = false;
	private Intent intent;
	
	private List<Preference> macAddrList = new ArrayList<Preference>();
	private static String sTextB4Chd = new String();
	
	/**
	 * This method inflates MAC Filter Settings UI View's on the screen from <b>mac_filter_settings.xml</b>  
	 * and displays the default MAC Address accept/deny list based on the selected mode from 
	 * <i>MAC Filter Mode</i> List
	 * 
	 * @param icicle, This could be null or some state information previously saved 
	 * by the onSaveInstanceState method  
	 */	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		qwifisoftAPCfg=MainMenuSettings.mSoftAPCfg;
		MainMenuSettings.mfsEvent=this;
		addPreferencesFromResource(R.xml.mac_filter_settings);
		defSharPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		allowkeys = getResources().getStringArray(R.array.mac_allow_keylst);
		denykeys = getResources().getStringArray(R.array.mac_deny_keylst);		
		macFilterMode = (ListPreference) findPreference("macaddr_acl");
		macAddrLst = (PreferenceCategory)findPreference("mac_filter_catag_list"); 
		
		slstType = defSharPref.getString("macaddr_acl", "");			
		macFilterMode.setOnPreferenceChangeListener(this);
		macFilterMode.setSummary(macFilterMode.getEntry());
		prefScr = this.getPreferenceScreen();		
		defPrefEditor = defSharPref.edit();

		setMacAddrLstTitle(slstType);
		restoreUIState(defSharPref);
		setContentView(R.layout.dynamic_add);
		add = (Button) findViewById(R.id.btn);
		add.setOnClickListener(this);
	}

	/**
	 * Returns the changed state of the <b>MAC Filter Mode</b> ListPreference and
	 * displays the appropriate accept/deny MAC Address List, if it is true
	 * 
	 * @param preference The changed Preference.
	 * @param newValue The new value of the Preference.
	 * 
	 * @return boolean True to update the state of the Preference with the new value.
	 */	
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.d(L10NConstants.TAG_MFS, "MACFilterSettings - onPreferenceChange");
		
		if (preference == macFilterMode) {
			int index = macFilterMode.findIndexOfValue(newValue.toString());
			if (index != -1) {
				String lstValue = (String) macFilterMode.getEntries()[index];		
				macFilterMode.setSummary(lstValue);		
				//slstType = newValue.toString();//(String) macFilterMode.getEntryValues()[index];
				String macaddrCheck = defSharPref.getString("macaddr_acl", "");
						
				if(!macaddrCheck.equals(newValue)){
					setMacAddrLstTitle(newValue.toString());
					restoreUIState(defSharPref);				
					MainMenuSettings.preferenceChanged = true;
				}
			}
		} 
		return true;
	}
	
	/**
	 * This method set the MAC address list title to AllowList/DenyList
	 */
	public void setMacAddrLstTitle(String sLstType)
	{
		if(sLstType.equals(L10NConstants.VAL_ZERO)) {			
			slstEntries = "deny";
			macAddrLst.setTitle("Deny List");
			blstTypeAorD = false;		
		} else {
			slstEntries = "allow";
			macAddrLst.setTitle("Accept List");
			blstTypeAorD = true;
		}
	}

	/**
	 * This method pops-up remove/cancel dialog box. 
	 * 
	 * @param preference The preference MAC address view(eg:11:22:33:44:55:66). 
	 * @return boolean true, if the click was handled.
	 */
	public boolean onPreferenceClick(final Preference preference) {		
		final String sListAlert[] = {"Remove", "Cancel"};
		for (Preference prefLst : macAddrList) {
			if(prefLst == preference) {
				mfsbuilder=new AlertDialog.Builder(MACFilterSettings.this);
				mfsbuilder.setTitle("Select");
				mfsalertdialog=mfsbuilder.setItems(sListAlert, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {						
						String[] sCpyLstAlert = sListAlert;
						if(sCpyLstAlert[which].equals("Remove")){							
							if(slstEntries.equals("allow")){
								removeMacAddListView(preference, L10NConstants.ALLOW, allowkeys);								
							}else if(slstEntries.equals("deny")) {
								removeMacAddListView(preference, L10NConstants.DENY, denykeys);								
							}
							MainMenuSettings.preferenceChanged = true;}
						else if(sCpyLstAlert[which].equals("Cancel")) {
							//No action
						}			
					}
				}).create();
				mfsalertdialog.show();
			}
		}
		return true;		
	}

	/**
	 * This method is used to remove MAC addresses from allow/deny List 
	 * @param preference It represents the MAC address entry preference to be removed
	 * @param sFilterMode It represents the Filter mode ie allow/deny
 	 * @param sModeKeys It represents ModeKeys used in default shared preference ie allows[1..15]/deny[1..15]
	 */
	private void removeMacAddListView(Preference preference, String sFilterMode, String[] sModeKeys){
		int iCnt;
		String key = "";
		String title = preference.getTitle().toString();
		//get the key for the selected preference 
		for(iCnt = 1;iCnt <= 15; iCnt++){												
			String findVal = defSharPref.getString(sFilterMode+iCnt, "");	
			if(title.equals(findVal)){
				key=sFilterMode+iCnt;
				break;
			}
		}
		//Remove the preference view (ie cell)
		prefScr.removePreference(preference);								
		defPrefEditor.putString(key, "");								
		defPrefEditor.commit();
		Log.d(L10NConstants.TAG_MFS, "Removed Pref :"+preference.getKey());						
		macAddrList.remove(preference);
		String val;
		// Remove the preference(ie cell) and adjust the remaining below preferences(ie cells) 
		for(iCnt = 0;iCnt <sModeKeys.length-1; iCnt++ ) {
			if(defSharPref.getString(sModeKeys[iCnt],"").equals("")) {
				val = defSharPref.getString(sModeKeys[iCnt+1],"");
				defPrefEditor.putString(sModeKeys[iCnt],val);	
				defPrefEditor.putString(sModeKeys[iCnt+1],"");
				defPrefEditor.commit();					
			}
		}
	}
	
	/**
	 * Private method to add MAC address dynamically and updating the copy preference file
	 * @param newValue New MAC address to be added in the appropriate list
	 * @param lstType List Type of the MAC address
	 */	
	private void addMacAddr(String newValue, String lstType) {		
		if(!validateMACAddr(newValue)) {
			Toast.makeText(MACFilterSettings.this, "Please Type a valid MAC Address", 1).show();				
		}else{					
			for (int n = 0; n < 15; n++){
				String macVal = defSharPref.getString(lstType+(n+1), "");				
				if(newValue.equalsIgnoreCase(macVal)){
					Toast.makeText(this, "Duplicate MAC address", 1).show();						
					break;
				} else if(n == 14){
					for (int j = 1; j <= 15; j++) {						
						String checkvalue = defSharPref.getString(lstType+j, "");						
						if(checkvalue.equals("")) {
							Preference pref = new Preference(this);
							pref.setTitle(newValue);
							pref.setKey(lstType+j);
							pref.setOnPreferenceClickListener(this);
							defPrefEditor.putString(lstType+j, newValue);
							defPrefEditor.commit();							
							prefScr.addPreference(pref);				
							macAddrList.add(pref);
							MainMenuSettings.preferenceChanged = true;
							break;
						}else if(j == 15){							
							Toast.makeText(MACFilterSettings.this, "List is Full", 0).show();
						}										
					}
					break;
				}
			}				
		}		
	}

	/**
	 * Private method to restore the UI state of the <b>MACFilterSettings</b> Activity based on the 
	 * MAC Filter Mode
	 * 
	 * @param defSharPref SharedPreferences object to get the value from the default preference file
	 */	
	private void restoreUIState(SharedPreferences defSharPref) {
		for(int i=0; i<macAddrList.size(); i++){
			prefScr.removePreference(macAddrList.get(i));		
		}
		macAddrList.clear();
		for (int j = 1; j <= 15; j++){
			String checkvalue = defSharPref.getString(slstEntries+j, "");			
			if(!checkvalue.equals("")){
				pref = new Preference(this);
				pref.setTitle(checkvalue);
				pref.setKey(slstEntries+j);
				pref.setOnPreferenceClickListener(this);		
				prefScr.addPreference(pref);
				macAddrList.add(pref);
			}
		}	
	}
	
	/**
	 * Validating the entered MAC Address
	 * @param macAddr New MAC Address
	 * @return boolean true, if the MAC address is valid or false
	 */	
	public static boolean validateMACAddr(String macAddr){
		return macAddr.matches(L10NConstants.MAC_PATTERN);
	}
	
	/**
	 * Button click handler to add new MAC address and validate dynamically 
	 * @param v Button view object 
	 */
	public void onClick(View v) {	
		ltfactory = LayoutInflater.from(MACFilterSettings.this);
		textEntryView = ltfactory.inflate(R.layout.alert_dialog_mac, null);				
		editMacAddr = (EditText)textEntryView.findViewById(R.id.mac_edit_txt);
		txtMacAddr = (TextView) textEntryView.findViewById(R.id.mac_txt_view);
		txtMacAddr.setText("Eg: 11:22:33:44:55:AF");
		editMacAddr.setHint("MAC Address");
		
		showAlertDialog();				
	}
	
	public void showAlertDialog(){
		mfsbuilder=new AlertDialog.Builder(MACFilterSettings.this);				                
		mfsbuilder.setTitle("Add MAC Address");
		mfsbuilder.setView(textEntryView);
		mfsalertdialog=mfsbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String newValue = editMacAddr.getText().toString();
				if(blstTypeAorD)
					addMacAddr(newValue, L10NConstants.ALLOW);
				else
					addMacAddr(newValue, L10NConstants.DENY);
			}				
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		}).create();
		mfsalertdialog.show();	
	}

	public void EventHandler(String evt) {		
		if(evt.contains(L10NConstants.STATION_105)){			
			finish();
		}
	}
	public void onDestroy(){
		super.onDestroy();
		MainMenuSettings.mfsEvent = null;
		if(mfsalertdialog != null && mfsalertdialog.isShowing()) mfsalertdialog.cancel();
		Log.d(L10NConstants.TAG_MFS,"destroying MACFilterSettings");
	}
}
