package com.qualcomm.wifi.softap.ws;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.Toast;
import com.qualcomm.wifi.softap.MainMenuSettings;
import com.qualcomm.wifi.softap.R;

public class WSS_WPAPSK extends PreferenceActivity 
implements OnPreferenceChangeListener, OnKeyListener, OnPreferenceClickListener {   
	private static final String TAG = "QCSOFTAP_GUI_WSS_WPAPSK";	
	private SharedPreferences preferences;
	private ListPreference wpa_pairwiseLst, rsn_pairwiseLst;
	private EditTextPreference wpapskEdit1,wpapskEdit2;
	private String[] keys = {"wpa_pairwise", "wpa_passphrase", "wpa_group_rekey", "rsn_pairwise"};	
	private PreferenceCategory wss_wpapsk_catag;
	private String SecurityMode, PrevSecurityMode;
	private SharedPreferences.Editor wssEditor;
	private EditText passEdit, groupEdit;
	private String OFR_ER = "should be >= 600";
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.wss_pref_wpapsk); 
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		wssEditor = preferences.edit(); 

		wpa_pairwiseLst = (ListPreference)findPreference("wpa_pairwise");
		rsn_pairwiseLst = (ListPreference) findPreference("rsn_pairwise");
		rsn_pairwiseLst.setOnPreferenceChangeListener(this);
		wpa_pairwiseLst.setOnPreferenceChangeListener(this);
		wpapskEdit1 = (EditTextPreference) findPreference("wpa_passphrase");
		wpapskEdit1.setOnPreferenceChangeListener(this);
		wpapskEdit1.setOnPreferenceClickListener(this);
		wpapskEdit2 = (EditTextPreference) findPreference("wpa_group_rekey");
		wpapskEdit2.setOnPreferenceChangeListener(this);
		wpapskEdit2.setOnPreferenceClickListener(this);
		wss_wpapsk_catag = (PreferenceCategory) findPreference("wss_wpapsk_catag");
		
		SecurityMode = getIntent().getExtras().getString("SecurityMode");
		PrevSecurityMode = getIntent().getExtras().getString("PrevSecurityMode");
		wss_wpapsk_catag.setTitle(SecurityMode);
		
		Log.i(TAG, "Current and Previous Security Mode : "+SecurityMode+" & "+PrevSecurityMode);		

		if(!PrevSecurityMode.equals("")){
			if (!PrevSecurityMode.equals(SecurityMode)){
				Log.i(TAG, "Current and Previous Security Mode are different ");
				wssEditor.putString("wpa_passphrase", "");
				wssEditor.putString("wpa_group_rekey", "");
				wssEditor.commit();
				wpapskEdit1.setText("");
				wpapskEdit2.setText("");
				wpapskEdit1.setSummary("");
				wpapskEdit2.setSummary("");
			}
		}		

		if (SecurityMode.equals(WirelessSecuritySettings.WPA_PSK)){
			wpa_pairwiseLst.setEnabled(true);
			rsn_pairwiseLst.setEnabled(false);
		} else if (SecurityMode.equals(WirelessSecuritySettings.WPA2_PSK)){
			wpa_pairwiseLst.setEnabled(false);
			rsn_pairwiseLst.setEnabled(true);
		} else if (SecurityMode.equals(WirelessSecuritySettings.WPA_MIXED)){
			wpa_pairwiseLst.setEnabled(true);
			rsn_pairwiseLst.setEnabled(true);
		} else {
			Log.d(TAG, "Security Mode : Wrong Option");
		}

		for( int i = 0; i < keys.length; i++) {        	
			String getConfigMode = preferences.getString(keys[i], null);
			Log.d(TAG, "List Initial Values " + getConfigMode);	
			
			if( getConfigMode != null ){	
				if (getConfigMode.equals("TKIP CCMP")){
					getConfigMode = "Mixed";
				}
				if( keys[i].equals("wpa_pairwise")){						
					wpa_pairwiseLst.setSummary(getConfigMode);	
				} else if( keys[i].equals("rsn_pairwise")){						
					rsn_pairwiseLst.setSummary(getConfigMode);	
				} else if( keys[i].equals("wpa_passphrase")){
					wpapskEdit1.setSummary(getConfigMode);
				}else if( keys[i].equals("wpa_group_rekey")){					
					wpapskEdit2.setSummary(getConfigMode);					
				}
			}
		} 
		passEdit = wpapskEdit1.getEditText();
		passEdit.setOnKeyListener(this);
		groupEdit = wpapskEdit2.getEditText();
		groupEdit.setOnKeyListener(this);		
	}
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if( v instanceof EditText){
			EditText editTemp = (EditText)v;
			String textVal = editTemp.getText().toString();
			if(!textVal.equals("")){
				if(v == passEdit){
					if(textVal.length() > 63 ){
						editTemp.setError("Max 63 chars");
					}else if(textVal.length() < 8){
						editTemp.setError("Min 8 chars");
					}
				} else if(v == groupEdit){	
					try{
						if(Integer.parseInt(textVal) < 600) {
							editTemp.setError(OFR_ER);						
						}
					}catch(Exception e){
						editTemp.setError("Out of Range");
					}				
				}
			}else
				editTemp.setError("Can not be Null");
		}		
		return false;
	}
	
	public boolean onPreferenceClick(Preference preference) {
		if(preference instanceof EditTextPreference){
			EditTextPreference editPref = (EditTextPreference) preference;
			editPref.getEditText().setError(null);
		}
		return true;
	}
	
	public boolean onPreferenceChange(Preference preference, Object newValue) {		
		if(preference instanceof ListPreference){
			ListPreference lstPref = (ListPreference) preference;
			int index = lstPref.findIndexOfValue(newValue.toString());
			if (index != -1){
				String algmEntry = (String) lstPref.getEntries()[index];
				lstPref.setSummary(algmEntry);	
			}
			MainMenuSettings.preferenceChanged = true;
		} else if (preference instanceof EditTextPreference){
			EditTextPreference editPref = (EditTextPreference) preference;
			String editVal = editPref.getEditText().getEditableText().toString();
			if(!editVal.equals("")){
				if(preference == wpapskEdit1){					
					if( editVal.length()>=8 && editVal.length()<=63) {
						wpapskEdit1.setSummary(editVal);	
					} else {
						Toast.makeText(this, "Invalid Entry", 0).show();
						return false;
					}					
				} else if(preference == wpapskEdit2){
					try{
						if(Integer.parseInt(editVal) >= 600){
							wpapskEdit2.setSummary(editVal);
						}else{
							Toast.makeText(this, "Invalid Entry", 0).show();
							return false;
						}
					}catch(NumberFormatException nfe){
						Toast.makeText(this, "Out of Range", 0).show();
						return false;
					}					
				}
			}else{
				Toast.makeText(this, "Value can not be Null", 0).show();
				return false;
			}
			MainMenuSettings.preferenceChanged = true;
		}		
		return true;
	}
}

