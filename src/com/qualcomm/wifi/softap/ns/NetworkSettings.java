package com.qualcomm.wifi.softap.ns;

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
 * This class provides navigation control to MAC filter settings class 
 * which provides adding/removing Allow/Deny list of MAC addresses
 * 
 *{@link com.qualcomm.wifi.softap.ns.MACFilterSettings}
 */
public class NetworkSettings extends Activity
{
	private ListView nwList;
	private String[] nwLstvalue;

	/**
	 * This method inflates MAC Filter Setting View on the screen from <b>network.xml</b>
	 * 
	 * @param icicle, This could be null or some state information previously saved 
	 * by the onSaveInstanceState method 
	 */	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.d(L10NConstants.TAG_NS, "onCreate - NetworkSettings");
		setContentView(R.layout.network);
		nwLstvalue  = getResources().getStringArray(R.array.nwslist);
		nwList = (ListView) findViewById(R.id.nwlstview);
		nwList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nwLstvalue));		
		nwList.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> a, View view, int position, long id) 
			{			
				if(nwList.getItemAtPosition(position).equals("MAC Filter Settings") ) {															
					startActivity(new Intent(NetworkSettings.this, MACFilterSettings.class));
				}
			}
		});
	}
}