package com.qualcomm.wifi.softap;

public class L10NConstants {
	public static final String TAG_NS = "QCSOFTAP_GUI_NS";
	public static final String TAG_MFS = "QCSOFTAP_GUI_MACFS";
	public static final String TAG_WS = "QCSOFTAP_GUI_WS";
	public static final String TAG_BWS = "QCSOFTAP_GUI_BWS";
	public static final String TAG_WSS_WPAPSK = "QCSOFTAP_GUI_WSS_WPAPSK";
	public static final String TAG_AWS = "QCSOFTAP_GUI_AWS";
	
	//	--- Original Preference file Name ---	
	public static final String CONFIG_FILE_NAME = "orgConfig";
	
	//	--- Regular Expressions for Validation ---
	
	public static final String MAC_PATTERN = "((([0-9a-fA-F]){2}[:]){5}([0-9a-fA-F]){2})";
	public static final String MAC_PATTERN1 = "^[0-9a-fA-F]{1,2}$";
	public static final String HEXA_PATTERN = "[0-9a-fA-F]{10}|[0-9a-fA-F]{26}|[0-9a-fA-F]{32}";
	public static final String PIN_PATTERN = "^[0-9]{8,32}$";	
	
	public static final String SET_CMD_PREFIX = "set ";
	public static final String GET_CMD_PREFIX = "get ";
	public static final String SELECT_TITLE = "Select";
	public static final String SUCCESS = "success";
	public static final String WPS_PIN_TITLE = "Enter WPS PIN";
	public static final String OUT_GTR_RANGE = "should be >= 600";
	public static final String OUT_RANGE = "Out of Range";
	public static final String INVALID_ENTRY = "Invalid Entry";
	
	//	--- Preference keys --- 	
	public static final String WPS_KEY = "wps_state";
	public static final String CHNL_KEY = "channel";
	public static final String AUTH_MODE_KEY = "auth_algs";
	public static final String IGNORE_BROAD_SSID_KEY = "ignore_broadcast_ssid";
	public static final String CONFIG_KEY = "config_methods";
	public static final String HW_MODE_KEY = "hw_mode";	
	public static final String DATA_RATE_KEY = "data_rate";
	public static final String COUNTRY_KEY = "country_code";
	public static final String SEC_MODE_KEY = "security_mode";	
	public static final String RSN_PAIR_KEY = "rsn_pairwise";
	public static final String WPA_PAIR_KEY = "wpa_pairwise";
	public static final String ENABLE_SOFTAP = "enable_softap";
	public static final String SSID_BROADCAST_KEY = "broadcast_ssid";
	public static final String SSID_KEY = "ssid";
	public static final String WPA_PASSPHRASE_KEY = "wpa_passphrase";
	public static final String WPA_GRP_KEY = "wpa_group_rekey";
	public static final String WSS_PREF_CATEG_KEY = "wss_wpapsk_catag";
	public static final String WIFI_AP_CHECK="status";
	
	public static final String ALLOW = "allow";
	public static final String DENY = "deny";
	
	//	--- Dialog IDs ---
		
	public static final int DIALOG_WPS = 4;
	public static final int DIALOG_OFF = 0, DIALOG_ON = 1, DIALOG_RESET = 2, 
						DIALOG_SAVE = 3, DIALOG_INITIAL = 99;
	
	public static final int MINUTE = 2*60*1000;	
	public static final int EVENT_ID = 1;	
	public static final int MAX_LENGTH = 15;	
	public static final String VAL_ZERO = "0";
	public static final String VAL_ONE = "1";	
	public static final String VAL_TWO = "2";
	public static final String VAL_THREE = "3";
	public static final String VAL_FOUR = "4";	
	public static final String STATION_102 = "102";
	public static final String STATION_103 = "103";
	
	//	--- Network Mode List Values ---
	
	public static final String SM_N_ONLY = "n_only";
	public static final String SM_N = "n";
	public static final String SM_G_ONLY = "g_only";
	public static final String SM_G = "g";
	public static final String SM_B = "b";
	
	//	--- Security Mode List Values ---
	
	public static final String OPEN = "Open"; 
	public static final String WEP = "WEP";
	public static final String WPA_PSK = "WPA-PSK";
	public static final String WPA2_PSK = "WPA2-PSK";
	public static final String WPA_MIXED = "WPA-WPA2 Mixed";
	public static final String SM_EXTRA_KEY = "SecurityMode";		
	public static final String WPA_ALG_TKIP = "TKIP";

	public static final String ERROR_NULL = "Can not be Null";
	public static final String ERROR_OUT_RANGE = "is out of range";
	public static final String ERROR_BELOW_RANGE = "is below range";
}