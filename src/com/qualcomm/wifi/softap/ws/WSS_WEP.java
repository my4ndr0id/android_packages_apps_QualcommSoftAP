package com.qualcomm.wifi.softap.ws;

import com.qualcomm.wifi.softap.MainMenuSettings;

import com.qualcomm.wifi.softap.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.Toast;

public class WSS_WEP extends PreferenceActivity implements OnPreferenceChangeListener, OnKeyListener, OnPreferenceClickListener {

	private static final String TAG = "QCSOFTAP_GUI_WSS_WEP";
	private SharedPreferences preferences;
	private ListPreference weplistPref1, weplistPref2;
	private String[] keys = { "listPrefEncryption", "wep_default_key",
							"wep_key0", "wep_key1",
							"wep_key2", "wep_key3" };	
	private EditTextPreference wepEdit1, wepEdit2, wepEdit3, wepEdit4;		
	private static String hexaDigits="(([0-9a-fA-F]){10})";	
	private static String hexaDigit = "(([0-9a-fA-F]){26})";
	private static String encryKey64 = "^[0-9a-zA-Z]{5}$";
	private static String encryKey128 = "^[0-9a-zA-Z]{13}$";
	private SharedPreferences.Editor keyEditer;
	private static String EX__KEY;
	private static String encryptionType;
	private EditText keyET1, keyET2, keyET3, keyET4;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.wss_pref_wep);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		keyEditer = preferences.edit(); 

		weplistPref1 = (ListPreference) findPreference("listPrefEncryption");
		weplistPref1.setOnPreferenceChangeListener(this);

		weplistPref2 = (ListPreference) findPreference("wep_default_key");
		weplistPref2.setOnPreferenceChangeListener(this);		

		wepEdit1 = (EditTextPreference) findPreference("wep_key0");
		wepEdit1.setOnPreferenceChangeListener(this);
		wepEdit1.setOnPreferenceClickListener(this);
		wepEdit2 = (EditTextPreference) findPreference("wep_key1");
		wepEdit2.setOnPreferenceChangeListener(this);
		wepEdit2.setOnPreferenceClickListener(this);
		wepEdit3 = (EditTextPreference) findPreference("wep_key2");
		wepEdit3.setOnPreferenceChangeListener(this);
		wepEdit3.setOnPreferenceClickListener(this);
		wepEdit4 = (EditTextPreference) findPreference("wep_key3");
		wepEdit4.setOnPreferenceChangeListener(this);
		wepEdit4.setOnPreferenceClickListener(this);

		dialogMsgKey((String)weplistPref1.getEntry());		 

		for (int i = 0; i < keys.length; i++) {
			String getConfigMode = preferences.getString(keys[i], null);
			if(getConfigMode.contains("\""))
				getConfigMode=getConfigMode.substring(1,getConfigMode.length()-1);
			Log.d(TAG,"List Initial Values " + getConfigMode);
			if (getConfigMode != null) {				
				if (keys[i].equals("listPrefEncryption")) {					
					encryptionType = (String)weplistPref1.getEntry();					
					weplistPref1.setSummary(encryptionType);
				} else if( keys[i].equals("wep_key0")){					
					wepEdit1.setSummary(getConfigMode);	
				}else if( keys[i].equals("wep_key1")){
					wepEdit2.setSummary(getConfigMode);					
				} else	if( keys[i].equals("wep_key2")){
					wepEdit3.setSummary(getConfigMode);	
				}else if( keys[i].equals("wep_key3")){
					wepEdit4.setSummary(getConfigMode);					
				}else if( keys[i].equals("wep_default_key")){
					weplistPref2.setSummary(weplistPref2.getEntry());
				}
			}
		}
		keyET1 = wepEdit1.getEditText();
		keyET1.setOnKeyListener(this);
		keyET2 = wepEdit2.getEditText();
		keyET2.setOnKeyListener(this);
		keyET3 = wepEdit3.getEditText();
		keyET3.setOnKeyListener(this);
		keyET4 = wepEdit4.getEditText();
		keyET4.setOnKeyListener(this);
	}
	
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if( v instanceof EditText){
			EditText editTemp = (EditText)v;			
			if((keyCode >= 7 && keyCode <= 16) || (keyCode >= 29 && keyCode <= 54) || keyCode == 59 || keyCode == 67){
				String textVal = editTemp.getText().toString();
				if(!textVal.equals("")){
					if(encryptionType.equals("64 bits")){
						if(textVal.length() > 10){
							editTemp.setError("Max 10 Hexa Digits");
						}
					}else{
						if(textVal.length() > 26){
							editTemp.setError("Max 26 Hexa Digits");
						}
					}					
				}else
					editTemp.setError("Key can not be null");
			}else if((keyCode >= 20 && keyCode <= 22)){
				editTemp.setError(null);
			}
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
		Log.d(TAG," WSS_WEP-onPreferenceChange");
		if (preference == weplistPref1) {
			clearAllKeys();						
			int index = weplistPref1.findIndexOfValue(newValue.toString());
			if (index != -1) {
				String lstValue = (String) weplistPref1.getEntries()[index];
				encryptionType = lstValue; 
				Log.d(TAG,"Values - " + weplistPref1.getEntries()[index]);				
				weplistPref1.setSummary(lstValue);
				dialogMsgKey(lstValue);
				MainMenuSettings.preferenceChanged = true;
			}
		} else if (preference == weplistPref2) {
			int index = weplistPref2.findIndexOfValue(newValue.toString());
			if (index != -1) {

				String lstValue = (String) weplistPref2.getEntries()[index];
				Log.d(TAG,"Values - " + weplistPref2.getEntries()[index]);
				weplistPref2.setSummary(lstValue);
				MainMenuSettings.preferenceChanged = true;
			}
		} else if (preference==wepEdit1) {
			Log.d(TAG, "After Change "+EX__KEY);
			String value=wepEdit1.getEditText().getEditableText().toString();
			Log.d(TAG, "String Checking "+value);
			return validateKey(wepEdit1, value, encryptionType);				
		} else if (preference==wepEdit2) {
			String value=wepEdit2.getEditText().getEditableText().toString();
			return validateKey(wepEdit2, value, encryptionType);
		} else if (preference==wepEdit3) {
			String value=wepEdit3.getEditText().getEditableText().toString();
			return validateKey(wepEdit3, value, encryptionType);			
		} else if (preference==wepEdit4) {
			String value=wepEdit4.getEditText().getEditableText().toString();
			return validateKey(wepEdit4, value, encryptionType);			
		}
		return true;
	}
	private boolean validateKey(EditTextPreference keyEdit, String value, String encryptionType){	
		boolean rtnValue = false;
		if(!value.equals("")){
			String value1 = value;
			if( encryptionType.equals("64 bits")){
				if(value.matches(encryKey64) || value.matches(hexaDigits)) {
					if(!value.matches(hexaDigits))
						value="\""+value+"\"";
					keyEdit.setSummary(value1);
					keyEdit.setDialogMessage(EX__KEY);
					MainMenuSettings.preferenceChanged = true;
					rtnValue = true;
				} else {
					Log.d(TAG,"Key1 is invalid");					
					Toast.makeText(this, "Invalid Entry", 0).show();
					rtnValue = false;
				}	
			}else if( encryptionType.equals("128 bits")){
				if(value.matches(encryKey128) || value.matches(hexaDigit)) {
					if(!value.matches(hexaDigits))
						value="\""+value+"\"";
					keyEdit.setSummary(value1);
					keyEdit.setDialogMessage(EX__KEY);
					MainMenuSettings.preferenceChanged = true;
					rtnValue = true;
				} else {
					Log.d(TAG,"Key1 is invalid");
					Toast.makeText(this, "Invalid Entry", 0).show();
					rtnValue = false;
				}
			}
		}else
			Toast.makeText(this, "Key can not be null", 0).show();
		return rtnValue;
	}

	private void dialogMsgKey(String lstValue){
		if (lstValue.equals("64 bits")){					
			EX__KEY = "Must be 5 chars or 10 hex digits";			
		} else if (lstValue.equals("128 bits")){					
			EX__KEY = "Must be 13 chars or 26 hex digits";			
		}
		wepEdit1.setDialogMessage(EX__KEY);
		wepEdit2.setDialogMessage(EX__KEY);
		wepEdit3.setDialogMessage(EX__KEY);
		wepEdit4.setDialogMessage(EX__KEY);
	}
	private void clearAllKeys() {
		wepEdit1.setSummary(""); 
		wepEdit2.setSummary("");
		wepEdit3.setSummary("");
		wepEdit4.setSummary("");
		wepEdit1.setText(""); 
		wepEdit2.setText(""); 
		wepEdit3.setText(""); 
		wepEdit4.setText("");
		keyEditer.putString("wep_key0", "");
		keyEditer.putString("wep_key1", "");
		keyEditer.putString("wep_key2", "");
		keyEditer.putString("wep_key3", "");
		keyEditer.commit();
	}	
}
