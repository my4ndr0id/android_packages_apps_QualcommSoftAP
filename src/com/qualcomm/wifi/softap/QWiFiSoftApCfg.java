
package com.qualcomm.wifi.softap;

import android.content.ContextWrapper;

import android.util.Log;


interface QWiFiSoftApEvent
{
	public void EventHandler(String evt);
}

public class QWiFiSoftApCfg
{
	private QWiFiSoftApEvent mEventCallback;	
	public native String SapSendCommand(String cmd);
	public native boolean SapOpenNetlink();
	public native String SapWaitForEvent();
	public native void SapCloseNetlink();

	public QWiFiSoftApCfg( Object caller)
	{
		System.loadLibrary("QWiFiSoftApCfg");
		mEventCallback = (QWiFiSoftApEvent)caller;
		ReceiveBroadcast.event=(QWiFiSoftApEvent)caller;
		Log.d(MainMenuSettings.TAG,"Launching APEventMonitor Thread");
				
	
	}	
}

