package com.qualcomm.wifi.softap;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class APEventMonitor implements Runnable
{
	private final String ACTION_RECEIVER="qualcomm.action.SEND";
	String eventstr;
	QWiFiSoftApCfg qcsoftapcfg;
	Context context;
	Thread thread;
	boolean KILLED;
	
	public APEventMonitor(QWiFiSoftApCfg ref,Context context){
		qcsoftapcfg=ref;
		this.context=context;
		thread = new Thread(this);
		thread.start();		
	}

	public void run() {
		Log.d("APEventMonitor","Thread Started");
		if(qcsoftapcfg.SapOpenNetlink()){
			Log.d("APEventMonitor", "Connection success");			
				while(!KILLED)
				{					
					Log.e("APEventMonitor","Waiting For Broadcast");
					eventstr=qcsoftapcfg.SapWaitForEvent();
					if(KILLED) break;
					if (eventstr == null) {
						Log.e("APEventMonitor","Null Event Received");						
						continue;
					}					
					Intent intent=new Intent();
					intent.setAction(ACTION_RECEIVER);
					intent.putExtra("event", eventstr);
					context.sendBroadcast(intent);
					Log.e("APEventMonitor","Event Received, broadcasting it");
					
				}
				Log.e("APEventMonitor","Killing Thread");
				qcsoftapcfg.SapCloseNetlink();		
				
		} else {
			Log.d("APEventMonitor","Connection Failed");
		}
	
	}
	
}