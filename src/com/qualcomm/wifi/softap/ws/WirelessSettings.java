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

import com.qualcomm.wifi.softap.R;

public class WirelessSettings extends Activity {
	private ListView wsList;	
	private String[] wsLstvalue;	
	private static final String TAG = "QCSOFTAP_GUI_WS";	
	
	@Override
	public void onCreate(Bundle icicle)	{
		super.onCreate(icicle);		
		setContentView(R.layout.ws);		
		
		wsLstvalue  = getResources().getStringArray(R.array.wslist);		
		wsList = (ListView) findViewById(R.id.wslstview);
		wsList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , wsLstvalue));		
		wsList.setTextFilterEnabled(true);		
		wsList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View view, int position, long id) {					
				if( wsList.getItemAtPosition(position).equals("Basic Wireless Settings") ) {
					Log.d(TAG,"onItemClick - Basic Wireless Settings");					
					Intent i = new Intent( WirelessSettings.this, BasicWirelessSettings.class);					
					startActivity(i);
					
				}else if( wsList.getItemAtPosition(position).equals("Wireless Security Settings") ) {
					Log.d(TAG,"onItemClick - Wireless Security Settings");					
					Intent i = new Intent( WirelessSettings.this, WirelessSecuritySettings.class);					
					startActivity(i);
					
				}else if( wsList.getItemAtPosition(position).equals("Advanced Wireless Settings") ) {
					Log.d(TAG,"onItemClick - Advanced Wireless Settings");					
					Intent i = new Intent( WirelessSettings.this, AdvancedWireless.class);					
					startActivity(i);
				}
			}
		});		
	}
}
