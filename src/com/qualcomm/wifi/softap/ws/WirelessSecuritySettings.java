package com.qualcomm.wifi.softap.ws;

import com.qualcomm.wifi.softap.MainMenuSettings;
import com.qualcomm.wifi.softap.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class WirelessSecuritySettings extends PreferenceActivity implements OnPreferenceChangeListener {
	private SharedPreferences defShPref;
	private ListPreference securityModeLst;  
	private static final String TAG = "";
	private final String OPEN = "Open"; 
	private static final String WEP = "WEP";
	public static final String WPA_PSK = "WPA-PSK";
	public static final String WPA2_PSK = "WPA2-PSK";
	public static final String WPA_MIXED = "WPA-WPA2 Mixed";
	public static final String SM_EXTRA_KEY = "SecurityMode";	
	private final String SEC_MODE_KEY = "security_mode";
	
	private static String getConfigMode;
	Intent intent; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.wss_pref);
		defShPref = PreferenceManager.getDefaultSharedPreferences(this);		 

		securityModeLst = (ListPreference) findPreference(SEC_MODE_KEY);	
		securityModeLst.setOnPreferenceChangeListener(this);

		getConfigMode = defShPref.getString(SEC_MODE_KEY, "0");
		Log.d(TAG, "List Initial Values " + getConfigMode );

		if( getConfigMode != null ){
			getConfigMode = (String) securityModeLst.getEntry();			
			securityModeLst.setSummary(getConfigMode);		
		}
		securityModeLst.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {			
			public boolean onPreferenceClick(Preference preference) {				
				String tempStr = WirelessSecuritySettings.this.defShPref.getString(SEC_MODE_KEY, "0");
				Log.d(TAG, "onPreferenceClick - previous String : "+tempStr);
				if (tempStr.equals("0"))
					getConfigMode = OPEN;
				else if (tempStr.equals("1"))
					getConfigMode = WEP;
				else if (tempStr.equals("2")){
					Log.d(TAG, "onPreferenceClick - previous String  within if: "+tempStr);
					getConfigMode = WPA_PSK;
				} else if (tempStr.equals("3"))
					getConfigMode = WPA2_PSK;
				else if (tempStr.equals("4"))
					getConfigMode = WPA_MIXED;
				return true;
			}
		});			
	}	 
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference == securityModeLst){
			Log.d(TAG, "*********** Wireless Security Mode-onPreferenceChange *************");
			int index = securityModeLst.findIndexOfValue(newValue.toString());
			if (index != -1) {  
				String lstEntry = (String) securityModeLst.getEntries()[index];			
				Log.d(TAG, "Values - " +securityModeLst.getEntries()[index]);
				securityModeLst.setSummary(lstEntry);
				
				if(lstEntry.equals(WEP)) {
					intent = new Intent( getBaseContext(), WSS_WEP.class);	
					startActivity(intent);					
				} else if(!lstEntry.equals(OPEN)){					
					intent = new Intent( getBaseContext(), WSS_WPAPSK.class);
					intent.putExtra("PrevSecurityMode", getConfigMode);					
					if (lstEntry.equals(WPA_PSK)) {
						intent.putExtra(SM_EXTRA_KEY, WPA_PSK);						
					} else if (lstEntry.equals(WPA2_PSK)) {
						intent.putExtra(SM_EXTRA_KEY, WPA2_PSK);								
					} else if (lstEntry.equals(WPA_MIXED)) {
						intent.putExtra(SM_EXTRA_KEY, WPA_MIXED);						
					}					
					startActivityForResult(intent, 1);				
				}
			}
			MainMenuSettings.preferenceChanged = true;
		}	
		return true;
	}
}
