package com.qualcomm.wifi.softap;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * 
 * This class implements access point monitor thread, which waits for
 * an incoming events via native method calls and broadcasts it.
 *
 */
public class APEventMonitor implements Runnable
{
	private final String ACTION_RECEIVER="qualcomm.action.SEND";
	private String eventstr;
	private QWiFiSoftApCfg qcsoftapcfg;
	private Context context;
	private Thread thread;
	static boolean KILLED = true;
	
	/**
	 *  Its an initialization part of APEventMonitor class.
	 *  It also starts the APEventMonitor thread.     
	 */
	public APEventMonitor(QWiFiSoftApCfg ref,Context context){
		qcsoftapcfg=ref;
		this.context=context;
		thread = new Thread(this);
		thread.start();	
	}
	/**
	 * Its a thread body, which calls native methods to receive events and
	 * broadcasts it.
	 */
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
	Log.d("APEventMonitor","Returning from APEventMonitor");
	}
	
}