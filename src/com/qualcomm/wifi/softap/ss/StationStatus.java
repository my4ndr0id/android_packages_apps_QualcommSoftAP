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


package com.qualcomm.wifi.softap.ss;

import java.util.ArrayList;
import java.util.StringTokenizer;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

import com.qualcomm.wifi.softap.L10NConstants;
import com.qualcomm.wifi.softap.MainMenuSettings;
import com.qualcomm.wifi.softap.QWiFiSoftApCfg;
import com.qualcomm.wifi.softap.R;

/**
 * This class displays the MAC Addresses of all currently associated stations and updating the MAC Addresses 
 * for every association/disassociation of station
 */
public class StationStatus extends PreferenceActivity implements OnPreferenceClickListener{
	private PreferenceScreen prefScr;
	private Preference pref;
	private String KeyVal;
	private StringTokenizer strToken;
	private ArrayList<Preference> macArrayLst;

	public static String TAG, response;
	public static QWiFiSoftApCfg mSoftAPCfg;	
	
	/**
	 * This method inflates UI views from <i>station_status.xml</i>
	 * It is getting all the associated stations from the underlined daemon and updating the screen
	 * 
	 *@param savedInstanceState, This could be null or some state information previously saved 
	 * by the onSaveInstanceState method
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);			
		
		MainMenuSettings.ssrefToMMS=this;
		addPreferencesFromResource(R.xml.station_status);
		prefScr = getPreferenceScreen();
		
		mSoftAPCfg = MainMenuSettings.mSoftAPCfg;
		TAG = getString(R.string.tag)+"SS";
		macArrayLst = new ArrayList<Preference>();
		Log.d(TAG,"Getting Command "+L10NConstants.GET_CMD_PREFIX +"sta_mac_list");			
		KeyVal = mSoftAPCfg.SapSendCommand(L10NConstants.GET_CMD_PREFIX +"sta_mac_list");		
		Log.d(TAG,"Received response "+KeyVal);
		
		// Pulls only mac_addresses from success result sent by daemon   
		if(!KeyVal.equals("")){			
			if(KeyVal.contains("success")){
				int index = KeyVal.indexOf("=");
				updateStationLst(KeyVal.substring(index+1));							
			}
		}
		Intent returnIntent = new Intent();	
		setResult(RESULT_OK, returnIntent);		
	}	

	/**
	 * This methods displays the associated station's MAC Addresses  
	 * 
	 * @param macLst MAC Address List from underlined daemon 
	 */
	private void updateStationLst(String macLst){
		strToken = new StringTokenizer(macLst);
		while(strToken.hasMoreTokens()){
			String sMacAddress = strToken.nextToken(); 
			pref = new Preference(this);
			pref.setTitle(sMacAddress);	
			pref.setOnPreferenceClickListener(this);			
			prefScr.addPreference(pref);				
			macArrayLst.add(pref);	
		}
	}

	/**
	 * This method displays the dialog box to disassociate the MAC Address else cancel the change
	 * 
	 * @param preference the Preference that was clicked.  
	 * @return true/false on success/failure.
	 */
	public boolean onPreferenceClick(final Preference preference) {
		final String sAlertListVal[] = getResources().getStringArray(R.array.station_lst_opt);
		for (Preference prefLst : macArrayLst) {
			if(prefLst == preference) {
				new AlertDialog.Builder(StationStatus.this)
				.setTitle(L10NConstants.SELECT_TITLE)
				.setItems(sAlertListVal, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String[] items = sAlertListVal;
						if(items[which].equals("Disassociate")){							
							Log.d(TAG,"Sending Command "+L10NConstants.SET_CMD_PREFIX+"disassoc_sta ="+ preference.getTitle());	
							response = mSoftAPCfg.SapSendCommand(L10NConstants.SET_CMD_PREFIX + "disassoc_sta=" + preference.getTitle());							
							Log.d(TAG, "Received Response ........: " + response);							
							if(response.equals("success")){					
								prefScr.removePreference(preference);				
							}else
								Toast.makeText(StationStatus.this, "Could not disassociate the station", 1).show();

						}else if(items[which].equals("Cancel")) {
							//No action on cancel 
						}			
					}
				}).create().show();
			}
		}
		return false;
	}	
}
