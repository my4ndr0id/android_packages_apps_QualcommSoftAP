package com.qualcomm.wifi.softap.ws;

import com.qualcomm.wifi.softap.MainMenuSettings;
import com.qualcomm.wifi.softap.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
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

public class AdvancedWireless extends PreferenceActivity 
implements OnPreferenceChangeListener, OnKeyListener, OnPreferenceClickListener {
	private String mode;
	private static final String TAG = "QCSOFTAP_GUI_AWS";		
	private SharedPreferences defPref;	
	//private ListPreference transmitPwrLst;
	//private Preference dataRates; 
	private CheckBoxPreference protectChk, wmmChk, intrabssChk,chkbx;
	private EditTextPreference fragmentETP, rtsETP, beaconETP, dtimETP,transmitPwr;
	private String[] keys = {"tx_power", "data_rate","fragm_threshold", 
			"rts_threshold", "beacon_int","dtim_period","country_code"};	
	private EditText rtsEdit, beaconEdit, dtimEdit, fragmentEdit,txpowerEdit;
	private String NULL_ER = "Can not be Null";
	private String OFR_ER = " is out of range";
	private String BR_ER = " is below range";
	private String[] br_opt,br_optValues;
	private ListPreference dataRatesLst,countryCodeLst;
	private boolean[] br_selections;
	private static final String NULL = "";
	private SharedPreferences.Editor defEditor;
	private String sprotectChk, swmmChk, sintrabssChk,dChk;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		addPreferencesFromResource(R.xml.advancedwireless);		

		defPref = PreferenceManager.getDefaultSharedPreferences(this);
		defEditor = defPref.edit();
		mode = defPref.getString("hw_mode"," ");
		Log.d(TAG,"Network Mode " + mode);		

		transmitPwr = (EditTextPreference) findPreference("tx_power");
		transmitPwr.setOnPreferenceChangeListener(this);
		transmitPwr.setOnPreferenceClickListener(this);

		protectChk = (CheckBoxPreference) findPreference("protect_flag");	
		protectChk.setOnPreferenceChangeListener(this);
		wmmChk = (CheckBoxPreference) findPreference("wmm_key");	
		wmmChk.setOnPreferenceChangeListener(this);
		intrabssChk = (CheckBoxPreference) findPreference("intra_bss");	
		intrabssChk.setOnPreferenceChangeListener(this);
		chkbx = (CheckBoxPreference) findPreference("d_chk");	
		chkbx.setOnPreferenceChangeListener(this);		
		dataRatesLst = (ListPreference) findPreference("data_rate");
		dataRatesLst.setOnPreferenceChangeListener(this);
		countryCodeLst = (ListPreference) findPreference("country_code");
		countryCodeLst.setOnPreferenceChangeListener(this);

		fragmentETP = (EditTextPreference) findPreference("fragm_threshold");
		fragmentETP.setOnPreferenceChangeListener(this);
		fragmentETP.setOnPreferenceClickListener(this);
		rtsETP = (EditTextPreference) findPreference("rts_threshold");
		rtsETP.setOnPreferenceChangeListener(this);
		rtsETP.setOnPreferenceClickListener(this);
		beaconETP = (EditTextPreference) findPreference("beacon_int");
		beaconETP.setOnPreferenceChangeListener(this);
		beaconETP.setOnPreferenceClickListener(this);
		dtimETP = (EditTextPreference) findPreference("dtim_period");
		dtimETP.setOnPreferenceChangeListener(this);	
		dtimETP.setOnPreferenceClickListener(this);			

		sprotectChk = defPref.getString("protection_flag", NULL);
		if(!sprotectChk.equals("")){
			if(sprotectChk.equals("0")){
				protectChk.setChecked(false);	
			}else if(sprotectChk.equals("1"))
				protectChk.setChecked(true);
		} else {
			protectChk.setChecked(false);
		}
		swmmChk = defPref.getString("wmm_enabled", NULL);		
		if(!swmmChk.equals("")){
			if(swmmChk.equals("0")){
				wmmChk.setChecked(false);	
			}else if(swmmChk.equals("1"))
				wmmChk.setChecked(true);
		} else {
			wmmChk.setChecked(false);
		}
		sintrabssChk = defPref.getString("intra_bss_forward", NULL);		
		if(!sintrabssChk.equals("")){
			if(sintrabssChk.equals("0")){
				intrabssChk.setChecked(false);	
			}else if(sintrabssChk.equals("1"))
				intrabssChk.setChecked(true);
		} else {
			intrabssChk.setChecked(false);
		}
		dChk = defPref.getString("regulatory_domain", NULL);		
		if(!dChk.equals("")){
			if(dChk.equals("0")){
				chkbx.setChecked(false);	
			}else if(dChk.equals("1"))
				chkbx.setChecked(true);
		} else {		
			chkbx.setChecked(false);	
		}
		if(mode.equals("b")){	
			br_opt = getResources().getStringArray(R.array.dataRatesArrayB);
			dataRatesLst.setEntries(br_opt);
			br_optValues = getResources().getStringArray(R.array.dataRatesValuesB);	
			dataRatesLst.setEntryValues(br_optValues);
		}else if((mode.equals("g_only"))|| (mode.equals("g"))){
			br_opt = getResources().getStringArray(R.array.dataRatesArrayGBG);
			dataRatesLst.setEntries(br_opt);
			br_optValues = getResources().getStringArray(R.array.dataRatesValuesGBG);
			dataRatesLst.setEntryValues(br_optValues);			
		}else if((mode.equals("n_only"))|| (mode.equals("n"))){
			br_opt = getResources().getStringArray(R.array.dataRatesArrayNBGN);
			dataRatesLst.setEntries(br_opt);
			br_optValues = getResources().getStringArray(R.array.dataRatesValuesNBGN);
			dataRatesLst.setEntryValues(br_optValues);
		}
		if(!chkbx.isChecked()){
			countryCodeLst.setEnabled(false);			
		}else {
			countryCodeLst.setEnabled(true);			
		} 
		setDefaultValues();
		txpowerEdit = transmitPwr.getEditText();
		txpowerEdit.setOnKeyListener(this);
		rtsEdit = rtsETP.getEditText();
		rtsEdit.setOnKeyListener(this);
		beaconEdit = beaconETP.getEditText();	
		beaconEdit.setOnKeyListener(this);		
		dtimEdit = dtimETP.getEditText();	
		dtimEdit.setOnKeyListener(this);
		fragmentEdit = fragmentETP.getEditText();
		fragmentEdit.setOnKeyListener(this);		
	}

	public boolean onPreferenceClick(Preference preference) {
		if(preference instanceof EditTextPreference){
			EditTextPreference edit = (EditTextPreference) preference;
			edit.getEditText().setError(null);
		}

		return true;
	}
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if( v instanceof EditText){
			EditText editTemp = (EditText)v;
			String textVal = editTemp.getText().toString();
			if(!textVal.equals("")){
				if(v == txpowerEdit){
					if(Integer.parseInt(textVal) > 18) {
						editTemp.setError(textVal+OFR_ER);						
					}
				} else if(v == rtsEdit){
					if(Integer.parseInt(textVal) > 2347) {
						editTemp.setError(textVal+OFR_ER);						
					}
				} else if(v == beaconEdit){
					if(Integer.parseInt(textVal) > 65535){
						editTemp.setError(textVal+OFR_ER);
					}
				} else if(v == dtimEdit){
					if(Integer.parseInt(textVal) > 255){
						editTemp.setError(textVal+OFR_ER);					
					}
				} else if(v == fragmentEdit){
					if(Integer.parseInt(textVal) > 2346){
						editTemp.setError(textVal+OFR_ER);					
					}else if(Integer.parseInt(textVal) < 256)
						editTemp.setError(textVal+BR_ER);
				}
			}else{
				editTemp.setError(NULL_ER);				
			}				
		}		
		return false;
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.d(TAG,"Advanced Wireless-onPreferenceChange" );
		if(preference instanceof ListPreference){
			ListPreference lstPref = (ListPreference) preference;
			int index = lstPref.findIndexOfValue(newValue.toString());
			if (index != -1)  {  
				String lstValue = (String) lstPref.getEntries()[index];
				Log.d(TAG,"Values of Each List : " +lstValue);
				lstPref.setSummary(lstValue);
			}			
			MainMenuSettings.preferenceChanged = true;
		} else if(preference instanceof EditTextPreference){
			EditTextPreference etPref = (EditTextPreference) preference;
			String value = etPref.getEditText().getEditableText().toString();
			if(!value.equals("")){
				if(preference == transmitPwr){					
					int i = Integer.parseInt(value);
					if(i >= 1 && i <= 18){							
						etPref.setSummary(Integer.toString(i));							
					} else {
						Toast.makeText(this, "Invalid Transmit Power", 0).show();
						return false;
					}				
					MainMenuSettings.preferenceChanged = true;
				} else if(preference == fragmentETP){					
					int i = Integer.parseInt(value);
					if(i >= 256 && i <= 2346){							
						etPref.setSummary(Integer.toString(i));							
					} else {
						Toast.makeText(this, "Invalid Fragmentation Threshold", 0).show();
						return false;
					}				
					MainMenuSettings.preferenceChanged = true;
				}else if(preference == rtsETP){				
					int i=Integer.parseInt(value);
					if(i >= 0 && i <= 2347) {
						etPref.setSummary(Integer.toString(i));
					} else {
						Toast.makeText(this, "Invalid RTS Threshold", 0).show();
						return false;
					}					
				}else if(preference == beaconETP){							
					int i=Integer.parseInt(value);
					if(i >= 1 && i <= 65535) {							
						etPref.setSummary(Integer.toString(i));
					} else {
						Toast.makeText(this, "Invalid Beacon Period", 0).show();
						return false;
					}				
				}else if(preference == dtimETP){
					int i=Integer.parseInt(value);
					if(i >= 1 && i <= 255) {							
						etPref.setSummary(Integer.toString(i));
					} else {						
						Toast.makeText(this, "Invalid DTIM Period", 0).show();
						return false;
					}							
				} 
			} else{
				Toast.makeText(this, "Value can not be null", 0).show();
				return false;
			}
			MainMenuSettings.preferenceChanged = true;
		} else if(preference == protectChk){			
			if(!protectChk.isChecked()){
				defEditor.putString("protection_flag", "1");	
				defEditor.commit();
			}else {
				defEditor.putString("protection_flag", "0");	
				defEditor.commit();
			}
			MainMenuSettings.preferenceChanged = true;
		} else if(preference == wmmChk){			
			if(!wmmChk.isChecked()){
				defEditor.putString("wmm_enabled", "1");	
				defEditor.commit();
			}else {
				defEditor.putString("wmm_enabled", "0");	
				defEditor.commit();
			}
			MainMenuSettings.preferenceChanged = true;
		} else if(preference == intrabssChk){			
			if(!intrabssChk.isChecked()){
				defEditor.putString("intra_bss_forward", "1");	
				defEditor.commit();
			}else {
				defEditor.putString("intra_bss_forward", "0");	
				defEditor.commit();
			}
			MainMenuSettings.preferenceChanged = true;
		} else if(preference == chkbx){			
			if(!chkbx.isChecked()){
				countryCodeLst.setEnabled(true);
				defEditor.putString("regulatory_domain", "1");	
				defEditor.commit();

			}else {
				countryCodeLst.setEnabled(false);
				defEditor.putString("regulatory_domain", "0");	
				defEditor.commit();
			}
			MainMenuSettings.preferenceChanged = true;
		}
		return true;
	}

	private void setDefaultValues(){
		for(int i = 0; i < keys.length; i++){
			String getConfigMode = defPref.getString(keys[i], null);

			Log.d(TAG,"List Initial Values " + getConfigMode);
			if( getConfigMode != null ){
				if( keys[i].equals("tx_power")){
					transmitPwr.setSummary(getConfigMode);
				}else if( keys[i].equals("fragm_threshold")){
					fragmentETP.setSummary(getConfigMode);
				} else if( keys[i].equals("rts_threshold")){
					rtsETP.setSummary(getConfigMode);
				} else if( keys[i].equals("beacon_int")){
					beaconETP.setSummary(getConfigMode);
				} else if( keys[i].equals("dtim_period")){
					dtimETP.setSummary(getConfigMode);
				}else if( keys[i].equals("data_rate")){				
					dataRatesLst.setSummary(dataRatesLst.getEntry());
				} else if( keys[i].equals("country_code")){				
					countryCodeLst.setSummary(countryCodeLst.getEntry());
				}

			}
		}
	}
}