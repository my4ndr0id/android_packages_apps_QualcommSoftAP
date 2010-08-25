package com.qualcomm.wifi.softap;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ReceiveBroadcast extends BroadcastReceiver{
	public static QWiFiSoftApEvent event=null;
	@Override
	public void onReceive(Context context, Intent intent) {
		event.EventHandler(intent.getStringExtra("event"));
	}
}
