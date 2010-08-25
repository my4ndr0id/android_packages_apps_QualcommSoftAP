package com.qualcomm.wifi.softap.ss;

import java.util.ArrayList;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

import com.qualcomm.wifi.softap.MainMenuSettings;
import com.qualcomm.wifi.softap.QWiFiSoftApCfg;
import com.qualcomm.wifi.softap.R;

public class StationStatus extends PreferenceActivity implements OnPreferenceClickListener{
	private PreferenceScreen prefScr;
	private Preference pref;	


	private SharedPreferences defShPref;
	private SharedPreferences.Editor defEditor;
	public static String TAG,response;
	public static QWiFiSoftApCfg mSoftAPCfg;	
	public static final String setCmdPrefix = "set ";
	public static final String getCmdPrefix = "get ";

	private String KeyVal;
	private StringTokenizer strToken;

	private ArrayList<Preference> macArrayLst;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);			
		
		MainMenuSettings.ssrefToMMS=this;
		addPreferencesFromResource(R.xml.station_status);
		prefScr = getPreferenceScreen();
		
		mSoftAPCfg = MainMenuSettings.mSoftAPCfg;		
		TAG = getString(R.string.tag)+"SS";

		defShPref = PreferenceManager.getDefaultSharedPreferences(this);
		defEditor = defShPref.edit();

		macArrayLst = new ArrayList<Preference>();

		Log.d(TAG,"Getting Command "+getCmdPrefix +"sta_mac_list");			
		KeyVal = mSoftAPCfg.SapSendCommand(getCmdPrefix +"sta_mac_list");		
		Log.d(TAG,"Received response "+KeyVal);	

		if(!KeyVal.equals("")){			
			if(KeyVal.contains("success")){
				int index = KeyVal.indexOf("=");
				updateStationLst(KeyVal.substring(index+1));								
			}

		}
		Intent returnIntent = new Intent();		
		setResult(RESULT_OK, returnIntent);       
		Log.d("StatusStatus", "I am in Station Status and Result has been set");
	}
	

	private void updateStationLst(String macLst){
		strToken = new StringTokenizer(macLst);

		while(strToken.hasMoreTokens()){
			String mac = strToken.nextToken(); 
			pref = new Preference(this);
			pref.setTitle(mac);			
			pref.setOnPreferenceClickListener(this);			
			prefScr.addPreference(pref);				
			macArrayLst.add(pref);	
		}
	}

	public boolean onPreferenceClick(final Preference preference) {
		final String sr[] = getResources().getStringArray(R.array.station_lst_opt);
		for ( Preference prefLst : macArrayLst) {
			if(prefLst == preference) {
				new AlertDialog.Builder(StationStatus.this)
				.setTitle("Select")
				.setItems(sr, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String[] items = sr;

						if(items[which].equals("Disassociate")){							
							Log.d(TAG,"Sending Command "+setCmdPrefix+"disassoc_sta ="+ preference.getTitle());	
							response = mSoftAPCfg.SapSendCommand(setCmdPrefix + "disassoc_sta=" + preference.getTitle());							
							Log.d(TAG, "Received Response ........: " + response);							
							if(response.equals("success")){					
								prefScr.removePreference(preference);				
							}else
								Toast.makeText(StationStatus.this, "Could not disassociate the station", 0).show();

						}else if(items[which].equals("Cancel")) {							
						}			
					}
				}).create().show();
			}
		}
		return false;
	}	
}
