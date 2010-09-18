
package com.qualcomm.wifi.softap;
/**
 * This interface declares EventHandler method to handle events. 
 */
interface QWiFiSoftApEvent
{
	public void EventHandler(String evt);
}
/**
 * This class declares all the native methods used in softAP and also loads the library
 * where native methods are implemented.
 */
public class QWiFiSoftApCfg {
	@SuppressWarnings("unused")
	private QWiFiSoftApEvent mEventCallback;	
	public native String SapSendCommand(String cmd);
	public native boolean SapOpenNetlink();
	public native String SapWaitForEvent();
	public native void SapCloseNetlink();
	/**
	 * Its a class initializer, which loads the shared library. 
	 */
	public QWiFiSoftApCfg( Object caller){
		System.loadLibrary("QWiFiSoftApCfg");
		mEventCallback = (QWiFiSoftApEvent)caller;
		ReceiveBroadcast.event=(QWiFiSoftApEvent)caller;
	}	
}

