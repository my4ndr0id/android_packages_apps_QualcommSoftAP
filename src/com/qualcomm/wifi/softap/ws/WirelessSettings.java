package com.qualcomm.wifi.softap.ws;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.qualcomm.wifi.softap.L10NConstants;
import com.qualcomm.wifi.softap.R;

/**
 * This class provides an option to configure various flavors of Wireless Settings ie Basic Wireless settings,
 * Wireless security settings and Advanced wireless settings
 *
 *{@link com.qualcomm.wifi.softap.ws.BasicWirelessSettings}
 *{@link com.qualcomm.wifi.softap.ws.WirelessSecuritySettings}
 *{@link com.qualcomm.wifi.softap.ws.AdvancedWireless}
 */

public class WirelessSettings extends Activity {
	private ListView wsList;
	private String[] wsLstvalue;
	Intent i;
	/**
	 * This method inflates the UI view from <i>ws.xml</i><br> and provides selection to the user
	 * 
	 * @param icicle If the activity is being re-initialized after previously being shut down 
	 * then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle)
	 */	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.ws);

		wsLstvalue = getResources().getStringArray(R.array.wslist);
		wsList = (ListView) findViewById(R.id.wslstview);
		wsList.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, wsLstvalue));
		
		// User is provided with selection options to click
		wsList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View view, int position,
					long id) {
				if (wsList.getItemAtPosition(position).equals(
						"Basic Wireless Settings")) {
					Log.d(L10NConstants.TAG_WS, "onItemClick - Basic Wireless Settings");
					i = new Intent(WirelessSettings.this, BasicWirelessSettings.class);
				} else if (wsList.getItemAtPosition(position).equals(
						"Wireless Security Settings")) {
					Log.d(L10NConstants.TAG_WS, "onItemClick - Wireless Security Settings");
					i = new Intent(WirelessSettings.this, WirelessSecuritySettings.class);
				} else if (wsList.getItemAtPosition(position).equals(
						"Advanced Wireless Settings")) {
					Log.d(L10NConstants.TAG_WS, "onItemClick - Advanced Wireless Settings");
					i = new Intent(WirelessSettings.this, AdvancedWireless.class);
				}
				startActivity(i);
			}
		});
	}
}