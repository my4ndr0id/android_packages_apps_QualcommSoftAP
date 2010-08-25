package com.qualcomm.wifi.softap.ns;

import com.qualcomm.wifi.softap.R;
import com.qualcomm.wifi.softap.MainMenuSettings;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class NetworkSettings extends Activity
{
	private ListView nwList;	

	private String[] nwLstvalue;

	private static final String TAG = "QCSOFTAP_GUI_NS";	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.d(TAG,"onCreate - NetworkSettings");
		setContentView(R.layout.network);		
		nwLstvalue  = getResources().getStringArray(R.array.nwslist);
		nwList = (ListView) findViewById(R.id.nwlstview);
		nwList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , nwLstvalue));
		nwList.setTextFilterEnabled(true);
		nwList.setOnItemClickListener(new OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> a, View view, int position, long id) 
			{			
				if(nwList.getItemAtPosition(position).equals("MAC Filter Settings") )
				{
					Log.d(TAG,"onItemClick - MAC Filter Settings");
					Intent i = new Intent( NetworkSettings.this, MACFilterSettings.class);					
					startActivity(i);
				}
			}
		});
	}
}
