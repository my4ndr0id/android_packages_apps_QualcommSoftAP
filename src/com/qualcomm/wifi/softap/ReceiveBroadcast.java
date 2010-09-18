package com.qualcomm.wifi.softap;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;

/**
 * 
 * This class implements Broadcast Receiver to receive intents broadcasted
 * via sendBroadcast and passes the received message to EventHandler method
 *
 */
public class ReceiveBroadcast extends BroadcastReceiver{
	public static QWiFiSoftApEvent event=null;
	/**
	 * This method receives any intents broadcasted via sendBroadcast and dispatches
	 * the event string to EventHandler method.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		event.EventHandler(intent.getStringExtra("event"));
	}
}
