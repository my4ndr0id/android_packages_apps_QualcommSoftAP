package com.qualcomm.wifi.softap.ns;

import java.util.ArrayList;

import java.util.List;
import java.util.StringTokenizer;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.wifi.softap.L10NConstants;
import com.qualcomm.wifi.softap.MainMenuSettings;
import com.qualcomm.wifi.softap.R;

/**
 * This MACFilterSettings class configures MAC Address List for addition/removal of MAC address
 */
public class MACFilterSettings extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener, OnClickListener{
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

		setMacAddrLstTitle();
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
				slstType = (String) macFilterMode.getEntryValues()[index];
				String macaddrCheck = defSharPref.getString("macaddr_acl", "");
						
				if(!macaddrCheck.equals(newValue)){
					setMacAddrLstTitle();
					intent = new Intent(getApplicationContext(), MACFilterSettings.class);					
					startActivity(intent);
					finish();
					MainMenuSettings.preferenceChanged = true;
				}
			}
		} 
		return true;
	}
	
	/**
	 * This method set the MAC address list title to AllowList/DenyList
	 */
	public void setMacAddrLstTitle()
	{
		if(slstType.equals(L10NConstants.VAL_ZERO)) {			
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
				new AlertDialog.Builder(MACFilterSettings.this)
				.setTitle("Select")
				.setItems(sListAlert, new DialogInterface.OnClickListener() {
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
				}).create().show();
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
				String macallowValue = defSharPref.getString("allow"+(n+1), "");
				String macdenyValue = defSharPref.getString("deny"+(n+1), "");
				if(newValue.equalsIgnoreCase(macallowValue)){
					Toast.makeText(this, "Address Already used in Accept/Deny List", 1).show();						
					break;
				}else if (newValue.equalsIgnoreCase(macdenyValue)){
					Toast.makeText(this, "Address Already used in Accept/Deny List", 1).show();						
					break;
				}else if(n == 14){
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
		for (int j = 1; j <= 15; j++){
			String checkvalue = defSharPref.getString(slstEntries+j, "");			
			if(!checkvalue.equals("")){
				Preference pref = new Preference(this);
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
						if(!token.matches(L10NConstants.MAC_PATTERN1)  && before != 1)
							editMacAddr.setError("Invalid");
					}				
					String part1 = macAddr.substring(index+1);					
					if(!part1.equals("")){
						if(part1.length() == 2 && part1.matches(L10NConstants.MAC_PATTERN1)){
							if(count <= 4 && before != 1){
								CharSequence newStr = macAddr+":";								
								editMacAddr.setText(newStr);								
								editMacAddr.setSelection(newStr.length());								
							}							
						}else if(!part1.matches(L10NConstants.MAC_PATTERN1) && before != 1)
							editMacAddr.setError("Invalid");
					}
				}else{
					if(macAddr.length() == 2 && before != 1){
						if(macAddr.matches(L10NConstants.MAC_PATTERN1)){						
							CharSequence newStr = macAddr+":";						
							editMacAddr.setText(newStr);												
							editMacAddr.setSelection(newStr.length());
						}
					}else if(!macAddr.matches(L10NConstants.MAC_PATTERN1))
						editMacAddr.setError("Invalid");
				}				
				if(before == 1 && sTextB4Chd.charAt(start) == ':'){		
					if(!sTextB4Chd.equals("")){
						editMacAddr.setText(sTextB4Chd);
						editMacAddr.setSelection(start+1);
					}
				}else if(before == 0 && macAddr.charAt(start) == ':'){
					if(!sTextB4Chd.equals("")){
						editMacAddr.setText(sTextB4Chd);
						editMacAddr.setSelection(start);	
					}
				}								
			}			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				sTextB4Chd = s.toString();				
			}			
			public void afterTextChanged(Editable theWatchedText) {				
			}
		};		
		editMacAddr.addTextChangedListener(macAddrWatcher);
		//display the alert dialog box
		showAlertDialog();				
	}
	
	public void showAlertDialog(){
		new AlertDialog.Builder(MACFilterSettings.this)				                
		.setTitle("Add MAC Address")
		.setView(textEntryView)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
		}).create().show();	
	}
}