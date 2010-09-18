package com.qualcomm.wifi.softap.ws;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.Toast;

import com.qualcomm.wifi.softap.L10NConstants;
import com.qualcomm.wifi.softap.MainMenuSettings;
import com.qualcomm.wifi.softap.R;
/**
 * This class configures WEP encryption keys and transmit key.  
 */
public class WSS_WEP extends PreferenceActivity implements OnPreferenceChangeListener, 
OnKeyListener, OnPreferenceClickListener {	

	private String[] keys;	
	private SharedPreferences defSharPref;
	private ListPreference wepTransmitKeylist;		
	private EditTextPreference wepEdit1, wepEdit2, wepEdit3, wepEdit4;	
	private EditText keyET1, keyET2, keyET3, keyET4;
	private ArrayList<EditTextPreference> editPrefLst;

	/**
	 * Method initializes the Activity from <i>wss_pref_wep</i> preference file
	 * 
	 * @param savedInstanceState If the activity is being re-initialized after previously being shut down 
	 * then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.wss_pref_wep);
		
		//initialize array list to store the keys
		editPrefLst = new ArrayList<EditTextPreference>();

		//get the keys list ie key1/key2/key3/key4
		keys = getResources().getStringArray(R.array.str_arr_wep_keys);
		defSharPref = PreferenceManager.getDefaultSharedPreferences(this);

		//get the reference to the transmit key and set the preference change listener
		wepTransmitKeylist = (ListPreference) findPreference("wep_default_key");
		wepTransmitKeylist.setOnPreferenceChangeListener(this);

		//get the reference to the keys
		wepEdit1 = (EditTextPreference) findPreference("wep_key0");		
		wepEdit2 = (EditTextPreference) findPreference("wep_key1");		
		wepEdit3 = (EditTextPreference) findPreference("wep_key2");		
		wepEdit4 = (EditTextPreference) findPreference("wep_key3");	

		editPrefLst.add(wepEdit1); editPrefLst.add(wepEdit2);
		editPrefLst.add(wepEdit3); editPrefLst.add(wepEdit4);

		//set onPreference change and click listener to the encryption keys and set the default value getting 
		//from the daemon
		for (int i = 0; i < keys.length; i++) {
			String getConfigMode = defSharPref.getString(keys[i], null);			
			editPrefLst.get(i).setOnPreferenceChangeListener(this);
			editPrefLst.get(i).setOnPreferenceClickListener(this);			
			if (getConfigMode != null) {
				editPrefLst.get(i).setSummary(getConfigMode);			
			}
		}
		wepTransmitKeylist.setSummary(wepTransmitKeylist.getEntry());
		//set key listener to the encryption key edit text box
		keyET1 = wepEdit1.getEditText();
		keyET1.setOnKeyListener(this);
		keyET2 = wepEdit2.getEditText();
		keyET2.setOnKeyListener(this);
		keyET3 = wepEdit3.getEditText();
		keyET3.setOnKeyListener(this);
		keyET4 = wepEdit4.getEditText();
		keyET4.setOnKeyListener(this);
	}

	/**
	 * Validating the Keys dynamically through key Listener 
	 * 
	 * @param v The view the key has been dispatched to.
	 * @param keyCode The code for the physical key that was pressed
	 * @param event The KeyEvent object containing full information about the event.
	 * 
	 * @return boolean True if the listener has consumed the event, false otherwise.
	 */
	public boolean onKey(View v, int keyCode, KeyEvent event) {		
		//Compare the ascii value entered for digits, alphabets, backspace
		if((keyCode >= 7 && keyCode <= 16) || (keyCode >= 29 && keyCode <= 54) || keyCode == 59 || keyCode == 67){
			String textVal = ((EditText)v).getText().toString();
			if(textVal.equals("")){
				((EditText)v).setError("Key can not be null");
			}					
		}else if((keyCode >= 20 && keyCode <= 22)){
			((EditText)v).setError(null);
		}
		return false;
	}

	/**
	 * Setting NULL value to the setError method for all the Edit Text Preference 
	 * @param preference is the edit text view .
	 * 
	 * @return boolean true on success. 
	 */
	public boolean onPreferenceClick(Preference preference) {		
		((EditTextPreference) preference).getEditText().setError(null);
		return true;
	}	

	/**
	 * Updating the value selected in transmit key list and WEP keys edit text preference
	 * 
	 * @param preference either transmit key or wep keys 
	 * @param newValue The new value which is changed.
	 * 
	 * @return boolean true on success.
	 */
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		int index;
		if (preference == wepTransmitKeylist) {
			index = wepTransmitKeylist.findIndexOfValue(newValue.toString());
			if (index != -1) {							
				wepTransmitKeylist.setSummary((String)wepTransmitKeylist.getEntries()[index]);
				String defaultCheck = defSharPref.getString("wep_default_key", "");
				//Dont allow the selection of the same value in the list to be saved
				if(!defaultCheck.equals(newValue)){
					MainMenuSettings.preferenceChanged = true;	
				}
			}
		} else {
			//Validate key entered for wep encryption edit text values
			return validateKey((EditTextPreference) preference, newValue.toString());
		}
		return true;
	}

	/**
	 * Validate the encryption key values entered to match hexadecimal/characters
	 * 
	 * @param keyEdit selected edit text preference object
	 * @param value new Entered encryption key value
	 * @return true if the key is valid,or false otherwise
	 */
	private boolean validateKey(EditTextPreference keyEdit, String value){		
		if(!value.equals("")){
			//Validate for hexa decimal values of length 10/26/32
			if(value.matches(L10NConstants.HEXA_PATTERN)){
				keyEdit.setSummary(value);	
				MainMenuSettings.preferenceChanged = true;
				return true;				
			}else if(value.length() == 5 || value.length() == 13 || value.length() == 16) {
				keyEdit.setSummary(value);	
				MainMenuSettings.preferenceChanged = true;
				return true;				
			} else {
				Toast.makeText(this, "Invalid Entry", 1).show();
				return false;				
			} 
		} else 
			Toast.makeText(this, "Key can not be null", 1).show();	
		return false;
	}	
}