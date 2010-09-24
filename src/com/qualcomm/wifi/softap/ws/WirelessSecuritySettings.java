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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;

import com.qualcomm.wifi.softap.L10NConstants;
import com.qualcomm.wifi.softap.MainMenuSettings;
import com.qualcomm.wifi.softap.R;

/**
 * This class displays the options to select the security mode for the wireless security<br>
 * 
 * {@link com.qualcomm.wifi.softap.ws.WSS_WEP}
 * {@link com.qualcomm.wifi.softap.ws.WSS_WPAPSK}
 */
public class WirelessSecuritySettings extends PreferenceActivity implements OnPreferenceChangeListener {	
	private String SM_NM_CHECK = "";

	private Intent intent;
	private SharedPreferences defSharPref, orgSharPref;
	private ListPreference securityModeLst;

	/**
	 * Method initializes the Activity from the <i>wss_pref.xml</i> preference file.
	 * Getting the previous Security Mode to send to next activity screen with the help of preference 
	 * onClick handler   
	 * 
	 * @param savedInstanceState If the activity is being re-initialized after previously being shut down 
	 * then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle)
	 */	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.wss_pref);
		defSharPref = PreferenceManager.getDefaultSharedPreferences(this);		 
		orgSharPref = getSharedPreferences(L10NConstants.CONFIG_FILE_NAME, MODE_PRIVATE);
		//Get the reference to the security mode key
		securityModeLst = (ListPreference) findPreference(L10NConstants.SEC_MODE_KEY);	
		securityModeLst.setOnPreferenceChangeListener(this);
		securityModeLst.setSummary(securityModeLst.getEntry());
	}

	/**
	 * Redirect to the below activities <br>
	 * {@link WSS_WEP Security Mode - WEP}<br>
	 * {@link WSS_WPAPSK Security Mode - WPAPSK} based on the new changed preference value
	 * 
	 * @param preference The changed Preference.
	 * @param newValue The new value of the Preference.
	 * 
	 * @return boolean True to update the state of the Preference with the new value.
	 */
	public boolean onPreferenceChange(Preference preference, Object newValue) {		
		int index = securityModeLst.findIndexOfValue(newValue.toString());
		
		//Get Security mode key value from original file ie orgSharPref
		String securityCheck = orgSharPref.getString(L10NConstants.SEC_MODE_KEY, ""); 
		
		//Get string values for network_mode, rsn_pair and wpa_pair from default preference
		String sNM = defSharPref.getString(L10NConstants.HW_MODE_KEY, "");
		String sWpa = defSharPref.getString(L10NConstants.WPA_PAIR_KEY, "");
		String sRsn = defSharPref.getString(L10NConstants.RSN_PAIR_KEY, "");
		
		if (index != -1) {
			String lstEntry = (String) securityModeLst.getEntries()[index];
			//Show warning alert message which doesn't allow to set security mode=WEP for network mode=n/bgn
			if(lstEntry.equals(L10NConstants.WEP)) {			
				intent = new Intent(getBaseContext(), WSS_WEP.class);	
				if(sNM.equals(L10NConstants.SM_N_ONLY)) {
					SM_NM_CHECK = getString(R.string.wep_screen_alert_N) + " " +
					getString(R.string.common_append_alert_wep);
				}else if(sNM.equals(L10NConstants.SM_N)) {
					SM_NM_CHECK = getString(R.string.wep_screen_alert_BGN) +" " +
					getString(R.string.common_append_alert_wep);
				}
				if(sNM.equals(L10NConstants.SM_N_ONLY) || sNM.equals(L10NConstants.SM_N)) {						
					showAlertDialog(lstEntry);
					return false;
				} else{
					securityModeLst.setSummary(lstEntry);
					startActivity(intent);
				}
			//Show warning alert message for the scenario Security mode=WPA-PSK/WPA-2PSK/MIXED & network mode=n/bgn			
			} else if(!lstEntry.equals(L10NConstants.OPEN)) {
				securityModeLst.setSummary(lstEntry);	
				intent = new Intent(getBaseContext(), WSS_WPAPSK.class);
				intent.putExtra(L10NConstants.SM_EXTRA_KEY, lstEntry);
				
				if(sNM.equals(L10NConstants.SM_N_ONLY)) {
					SM_NM_CHECK = getString(R.string.wpa_screen_alert_N_TKIP) + " " +
					getString(R.string.common_append_alert_wpa);
				}else if(sNM.equals(L10NConstants.SM_N)){
					SM_NM_CHECK = getString(R.string.wpa_screen_alert_BGN_TKIP) + " " +
					getString(R.string.common_append_alert_wpa);
				}				
				if(sNM.equals(L10NConstants.SM_N_ONLY) || sNM.equals(L10NConstants.SM_N)){
					if(newValue.equals(L10NConstants.VAL_TWO)){
						if(sWpa.equals(L10NConstants.WPA_ALG_TKIP))
							showAlertDialog(lstEntry);
						else
							startActivity(intent);
					}else if(newValue.equals(L10NConstants.VAL_THREE)){
						if(sRsn.equals(L10NConstants.WPA_ALG_TKIP))
							showAlertDialog(lstEntry);
						 else
							startActivity(intent);
					} else{
						if(sWpa.equals(L10NConstants.WPA_ALG_TKIP) || sRsn.equals(L10NConstants.WPA_ALG_TKIP))
							showAlertDialog(lstEntry);
						else
							startActivity(intent);
					}							
				} else 
					startActivity(intent);																
			}else
				securityModeLst.setSummary(lstEntry);
		}	
		if(!securityCheck.equals(newValue)){
			MainMenuSettings.preferenceChanged = true;	
		}			
		return true;
	}
	
	/**
	 * Show alert dialog box displaying the warning message 
	 * @param lstEntry is the type of security mode selected
	 */
	private void showAlertDialog(final String lstEntry){
		new AlertDialog.Builder(this) 
		.setTitle(getString(R.string.str_dialog_warning))
		.setMessage(SM_NM_CHECK)
		.setPositiveButton(getString(R.string.alert_dialog_rename_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(!lstEntry.equals(L10NConstants.WEP)) {
					startActivity(intent);
				}				
			}			
		}).create().show();	
	}
}
