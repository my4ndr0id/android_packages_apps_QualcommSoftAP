package com.qualcomm.wifi.softap.ns;

import java.util.ArrayList;

import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.BufferType;

import com.qualcomm.wifi.softap.MainMenuSettings;
import com.qualcomm.wifi.softap.R;

public class MACFilterSettings extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener, OnClickListener{
	private SharedPreferences preferences;
	private ListPreference macFilterMode;	

	private static final String TAG = "QCSOFTAP_GUI_MACFS";	
	private PreferenceCategory macAddrLst;			
	private String lstType,lstEntries;
	private boolean lstTypeAorD=false;		

	private Button add;
	private EditText newMacAddr;
	private String lstdType;
	private SharedPreferences preferences1;

	private LayoutInflater factory;
	private TextWatcher macAddrWatcher;
	private TextView view;
	private View textEntryView;

	public String allowkeys[] = {"allow1", "allow2", "allow3", "allow4", "allow5", "allow6", "allow7", "allow8", 
			"allow9", "allow10", "allow11", "allow12", "allow13", "allow14", "allow15"};
	public String denykeys[] = {"deny1", "deny2", "deny3", "deny4", "deny5", "deny6", "deny7", "deny8", 
			"deny9", "deny10", "deny11", "deny12", "deny13", "deny14", "deny15"};
	private String tag = "QCSOFTAP_GUI";

	private List<Preference> macAddrList = new ArrayList<Preference>();

	private PreferenceScreen prefScr;
	private SharedPreferences.Editor copyEdit;
	private static String MAC_PATTERN = "((([0-9a-fA-F]){2}[:]){5}([0-9a-fA-F]){2})";
	private static String MAC_PATTERN1 = "^[0-9a-fA-F]{1,2}$";
	private static String strB4Chd = new String();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.mac_filter_settings);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);                
		lstType = preferences.getString("macaddr_acl", "");			

		macFilterMode = (ListPreference) findPreference("macaddr_acl");
		macFilterMode.setOnPreferenceChangeListener(this);				

		macFilterMode.setSummary(macFilterMode.getEntry());			

		prefScr = this.getPreferenceScreen();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);		
		macAddrLst = (PreferenceCategory)findPreference("mac_filter_catag_list");
		copyEdit = preferences.edit();		

		if(lstType.equals("0"))					
		{
			lstEntries = "deny";
			macAddrLst.setTitle("Deny List");
			lstTypeAorD=false;
			restoreUIState(preferences);
		}
		else
		{
			lstEntries = "allow";
			macAddrLst.setTitle("Accept List");
			lstTypeAorD=true;
			restoreUIState(preferences);
		}
		setContentView(R.layout.dynamic_add);
		add = (Button) findViewById(R.id.btn);
		add.setOnClickListener(this);	
		Display display = getWindowManager().getDefaultDisplay(); 
		int width = display.getWidth();
		int height = display.getHeight();
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.d(TAG, "MACFilterSettings - onPreferenceChange");
		if (preference == macFilterMode) {
			int index = macFilterMode.findIndexOfValue(newValue.toString());
			if (index != -1) {
				String lstValue = (String) macFilterMode.getEntries()[index];				
				Log.d(TAG, "Values - " + macFilterMode.getEntries()[index]);

				macFilterMode.setSummary(lstValue);		
				lstType = (String) macFilterMode.getEntryValues()[index];			

				if(lstType.equals("0")) {					
					lstEntries = "deny";
					macAddrLst.setTitle("Deny List");
					lstTypeAorD=false;
					Intent intent = new Intent(getApplicationContext(), MACFilterSettings.class);
					startActivity(intent);
					finish();					
				} else {
					lstEntries = "allow";
					macAddrLst.setTitle("Accept List");
					lstTypeAorD=true;
					Intent intent = new Intent(getApplicationContext(), MACFilterSettings.class);
					startActivity(intent);
					finish();					
				}
			}
			MainMenuSettings.preferenceChanged = true;
		} 
		return true;
	}	

	public boolean onPreferenceClick(final Preference preference) {			
		final String sr[] = {"Remove","Cancel"};
		for ( Preference prefLst : macAddrList) {
			if(prefLst == preference) {
				new AlertDialog.Builder(MACFilterSettings.this)
				.setTitle("Select")
				.setItems(sr, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String[] items = sr;
						if(items[which].equals("Remove")){	
							Log.d(TAG,"lstEntries "+lstEntries);
							if(lstEntries.equals("allow")){	
								String key = "";
								String title = preference.getTitle().toString();									
								for( int m = 1; m <= 15; m++){												
									String findVal = preferences.getString("allow"+m, "");	
									if(title.equals(findVal)){
										key = "allow"+m;
										break;
									}
								}								
								prefScr.removePreference(preference);								
								copyEdit.putString(key, "");
								copyEdit.commit();
								Log.d(tag, "Removed Pref :"+preference.getKey());							
								String val;
								macAddrList.remove(preference);

								for(int i = 0;i < allowkeys.length-1; i++ ) {
									if(preferences.getString(allowkeys[i],"").equals("")) {
										val = preferences.getString(allowkeys[i+1],"");
										copyEdit.putString(allowkeys[i],val);	
										copyEdit.putString(allowkeys[i+1],"");
										copyEdit.commit();											
									}										
								}
								MainMenuSettings.preferenceChanged = true;
							}else if(lstEntries.equals("deny")) {	
								String key = "";
								String title = preference.getTitle().toString();									
								for( int m = 1; m <= 15; m++){												
									String findVal = preferences.getString("deny"+m, "");	
									if(title.equals(findVal)){
										key = "deny"+m;		
										break;
									}
								}
								prefScr.removePreference(preference);								
								copyEdit.putString(key, "");
								copyEdit.commit();
								Log.d(tag, "Removed Pref :"+preference.getKey());							
								String val;
								macAddrList.remove(preference);

								for(int i = 0;i < denykeys.length-1; i++ ) {
									if(preferences.getString(denykeys[i],"").equals("")) {
										val = preferences.getString(denykeys[i+1],"");
										copyEdit.putString(denykeys[i],val);	
										copyEdit.putString(denykeys[i+1],"");
										copyEdit.commit();
									}										
								}
							}
							MainMenuSettings.preferenceChanged = true;
						}else if(items[which].equals("Cancel")) {
						}			
					}
				}).create().show();
			}
		}
		return true;		
	}

	private void addMacAddr(String newValue, String lstType) {				
		if( !validateMACAddr(newValue) ) {
			Toast.makeText(MACFilterSettings.this, "Please Type a valid MAC Address", Toast.LENGTH_LONG).show();				
		}else{										
			for (int n = 0; n < 15; n++){
				String macallowValue = preferences.getString("allow"+(n+1), "");
				String macdenyValue = preferences.getString("deny"+(n+1), "");	
				if (newValue.equals(macallowValue)){
					Toast.makeText(this, "Address Already used in Accept/Deny List", 0).show();						
					break;
				}else if (newValue.equals(macdenyValue)){
					Toast.makeText(this, "Address Already used in Accept/Deny List", 0).show();						
					break;
				}else if(n == 14){
					for (int j = 1; j <= 15; j++) {
						Log.d(tag, "Entry Index: " +j);			
						if(lstType.equals("allow")){
							Log.d(tag, "allow Type: " +newValue);				
						}else if(lstType.equals("deny")){
							Log.d(tag, "Deny type: " +newValue);				
						}			
						String checkvalue = preferences.getString(lstType+j, "");				

						Log.d("", "Getting Value of " +lstType+j+": "+checkvalue);
						if(checkvalue.equals("")) {
							Preference pref = new Preference(this);
							pref.setTitle(newValue);
							pref.setKey(lstType+j);
							pref.setOnPreferenceClickListener(this);

							copyEdit.putString(lstType+j, newValue);
							copyEdit.commit();
							Log.d(tag, "addMacAddr - Check Value = "+checkvalue+" Key : "+lstType+j);
							prefScr.addPreference(pref);				
							macAddrList.add(pref);
							MainMenuSettings.preferenceChanged = true;
							break;
						}else if(j == 15){
							Log.d(tag, "No Space in the Entry List");
							Toast.makeText(MACFilterSettings.this, "List is Full", 0).show();
						}										
					}
					break;
				}
			}				
		}		
	}

	private void restoreUIState( SharedPreferences custProf) {
		for (int j = 1; j <= 15; j++){
			String checkvalue = custProf.getString(lstEntries+j, "");
			Log.d(tag, "restoreUIState - Check Value = "+checkvalue+" Index : "+lstEntries+j);
			if(!checkvalue.equals("")){
				Preference pref = new Preference(this);
				pref.setTitle(checkvalue);
				pref.setKey(lstEntries+j);
				Log.d(tag, "restoreUIState - Check Value = "+checkvalue+" Key : "+lstEntries+j);
				pref.setOnPreferenceClickListener(this);			
				prefScr.addPreference(pref);				
				macAddrList.add(pref);	
			}
		}	
	}	
	public static boolean validateMACAddr( String macAddr ){
		return macAddr.matches( MAC_PATTERN );
	}
	public void onClick(View v) {	
		factory = LayoutInflater.from(MACFilterSettings.this);
		textEntryView = factory.inflate(R.layout.alert_dialog_mac, null);				
		newMacAddr = (EditText)textEntryView.findViewById(R.id.mac_edit_txt);
		view = (TextView) textEntryView.findViewById(R.id.mac_txt_view);

		view.setText("Eg: 11:22:33:44:55:AF");		
		newMacAddr.setHint("MAC Address");		

		macAddrWatcher = new TextWatcher() {			
			public void onTextChanged(CharSequence s, int start, int before, int ct) {				
				String macAddr = s.toString();								

				if(macAddr.contains(":")){
					int index = macAddr.lastIndexOf(":");
					StringTokenizer strToken = new StringTokenizer(macAddr, ":");
					int count = 0;
					char[] charArry = macAddr.toCharArray();
					for(int i = 0; i < charArry.length; i++){
						if(charArry[i] == ':'){
							count++;
						}
					}
					while(strToken.hasMoreTokens()){
						String token = strToken.nextToken();												
						if(!token.matches(MAC_PATTERN1)  && before != 1)
							newMacAddr.setError("Invalid");
					}				
					String part1 = macAddr.substring(index+1);					
					if(!part1.equals("")){
						if(part1.length() == 2 && part1.matches(MAC_PATTERN1)){
							if(count <= 4 && before != 1){
								CharSequence newStr = macAddr+":";								
								newMacAddr.setText(newStr);								
								newMacAddr.setSelection(newStr.length());								
							}							
						}else if(!part1.matches(MAC_PATTERN1) && before != 1)
							newMacAddr.setError("Invalid");
					}
				}else{
					if(macAddr.length() == 2 && before != 1){
						if(macAddr.matches(MAC_PATTERN1)){						
							CharSequence newStr = macAddr+":";						
							newMacAddr.setText(newStr);												
							newMacAddr.setSelection(newStr.length());
						}
					}else if(!macAddr.matches(MAC_PATTERN1))
						newMacAddr.setError("Invalid");
				}				
				if(before == 1 && strB4Chd.charAt(start) == ':'){					
					newMacAddr.setText(strB4Chd);
					newMacAddr.setSelection(start+1);				
				}else if(before ==0 && macAddr.charAt(start) == ':'){
					newMacAddr.setText(strB4Chd);
					newMacAddr.setSelection(start);	
				}
				Log.d(TAG, "String :"+s+" Start :"+start+" before :"+before+" Count :"+ct+"Before String :"+strB4Chd);				
			}			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				strB4Chd = s.toString();				
			}			
			public void afterTextChanged(Editable theWatchedText) {
			}
		};		
		newMacAddr.addTextChangedListener(macAddrWatcher);		

		new AlertDialog.Builder(MACFilterSettings.this)				                
		.setTitle("Add MAC Address")
		.setView(textEntryView)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String newValue = newMacAddr.getText().toString();
				if(lstTypeAorD)
					addMacAddr(newValue, "allow");
				else
					addMacAddr(newValue, "deny");
			}				
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		}).create().show();			
	}
}